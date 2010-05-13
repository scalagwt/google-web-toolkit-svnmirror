/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.requestfactory.server;

import com.google.gwt.requestfactory.shared.RequestFactory;
import com.google.gwt.requestfactory.shared.ServerType;
import com.google.gwt.requestfactory.shared.RequestFactory.Config;
import com.google.gwt.requestfactory.shared.RequestFactory.RequestDefinition;
import com.google.gwt.requestfactory.shared.RequestFactory.WriteOperation;
import com.google.gwt.requestfactory.shared.impl.RequestDataManager;
import com.google.gwt.valuestore.shared.Property;
import com.google.gwt.valuestore.shared.Record;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

/**
 * Handles GWT RequestFactory JSON requests. Configured via servlet context
 * param <code>servlet.serverOperation</code>, which must be set to the name of
 * a default instantiable class implementing
 * com.google.gwt.requestfactory.shared.RequestFactory.Config.
 * <p>
 * e.g.
 * 
 * <pre>  &lt;context-param>
    &lt;param-name>servlet.serverOperation&lt;/param-name>
    &lt;param-value>com.myco.myapp.MyAppServerSideOperations&lt;/param-value>
  &lt;/context-param>

 * </pre>
 */
@SuppressWarnings("serial")
public class RequestFactoryServlet extends HttpServlet {

  /**
   * A class representing the pair of a domain entity and its corresponding
   * record class on the client side.
   */
  protected static class EntityRecordPair {
    public final Class<?> entity;
    public final Class<? extends Record> record;

    EntityRecordPair(Class<?> entity, Class<? extends Record> record) {
      this.entity = entity;
      this.record = record;
    }
  }

  private static final Set<String> BLACK_LIST = initBlackList();

  private static final String SERVER_OPERATION_CONTEXT_PARAM = "servlet.serverOperation";

  private static Set<String> initBlackList() {
    Set<String> blackList = new HashSet<String>();
    for (String str : new String[] {"password"}) {
      blackList.add(str);
    }
    return Collections.unmodifiableSet(blackList);
  }

  private Config config = null;

  protected Map<String, EntityRecordPair> tokenToEntityRecord;

  @SuppressWarnings("unchecked")
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    ensureConfig();

    RequestDefinition operation = null;
    try {
      response.setStatus(HttpServletResponse.SC_OK);
      PrintWriter writer = response.getWriter();
      JSONObject topLevelJsonObject = new JSONObject(getContent(request));
      String operationName = topLevelJsonObject.getString(RequestDataManager.OPERATION_TOKEN);
      if (operationName.equals(RequestFactory.SYNC)) {
        sync(topLevelJsonObject.getString(RequestDataManager.CONTENT_TOKEN),
            writer);
      } else {
        operation = getOperation(operationName);
        Class<?> domainClass = Class.forName(operation.getDomainClassName());
        Method domainMethod = domainClass.getMethod(
            operation.getDomainMethodName(), operation.getParameterTypes());
        if (!Modifier.isStatic(domainMethod.getModifiers())) {
          throw new IllegalArgumentException("the " + domainMethod.getName()
              + " is not static");
        }
        Object args[] = RequestDataManager.getObjectsFromParameterMap(
            getParameterMap(topLevelJsonObject),
            domainMethod.getParameterTypes());
        Object result = invokeStaticDomainMethod(domainMethod, args);

        if ((result instanceof List<?>) != operation.isReturnTypeList()) {
          throw new IllegalArgumentException(String.format(
              "Type mismatch, expected %s%s, but %s returns %s",
              operation.isReturnTypeList() ? "list of " : "",
              operation.getReturnType(), domainMethod,
              domainMethod.getReturnType()));
        }

        if (result instanceof List<?>) {
          JSONArray jsonArray = getJsonArray((List<?>) result,
              (Class<? extends Record>) operation.getReturnType());
          writer.print(jsonArray.toString());
        } else if (result instanceof Number) {
          writer.print(result.toString());
        } else {
          JSONObject jsonObject = getJsonObject(result,
              (Class<? extends Record>) operation.getReturnType());
          writer.print("(" + jsonObject.toString() + ")");
        }
      }
      writer.flush();
      // TODO: clean exception handling code below.
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException(e);
    } catch (IllegalAccessException e) {
      throw new IllegalArgumentException(e);
    } catch (InvocationTargetException e) {
      throw new IllegalArgumentException(e);
    } catch (SecurityException e) {
      throw new IllegalArgumentException(e);
    } catch (JSONException e) {
      throw new IllegalArgumentException(e);
    } catch (NoSuchMethodException e) {
      throw new IllegalArgumentException(e);
    } catch (IllegalArgumentException e) {
      throw new RuntimeException(e);
    } catch (InstantiationException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Persist a recordObject of token "recordToken" and return useful information
   * as a JSONObject to return back.
   * <p>
   * Example: recordToken = "Employee", entity = Employee.class, record =
   * EmployeeRecord.class
   *<p>
   * Steps:
   * <ol>
   * <li>assert that each property is present in "EmployeeRecord"
   * <li>invoke "findEmployee (id)" OR new Employee()
   * <li>set various fields on the attached entity and persist OR remove()
   * <li>return data
   * </ol>
   */
  JSONObject updateRecordInDataStore(String recordToken,
      JSONObject recordObject, WriteOperation writeOperation) {

    try {
      Class<?> entity = tokenToEntityRecord.get(recordToken).entity;
      Class<? extends Record> record = tokenToEntityRecord.get(recordToken).record;
      Map<String, Class<?>> propertiesInRecord = getPropertiesFromRecord(record);
      validateKeys(recordObject, propertiesInRecord.keySet());
      updatePropertyTypes(propertiesInRecord, entity);

      // get entityInstance
      Object entityInstance = getEntityInstance(writeOperation, entity,
          recordObject.get("id"), propertiesInRecord.get("id"));

      // persist
      Set<ConstraintViolation<Object>> violations = null;
      if (writeOperation == WriteOperation.DELETE) {
        entity.getMethod("remove").invoke(entityInstance);
      } else {
        Iterator<?> keys = recordObject.keys();
        while (keys.hasNext()) {
          String key = (String) keys.next();
          Class<?> propertyType = propertiesInRecord.get(key);
          if (writeOperation == WriteOperation.CREATE && ("id".equals(key))) {
            // ignored. id is assigned by default.
          } else {
            Object propertyValue = getPropertyValueFromRequest(recordObject,
                key, propertyType);
            propertyValue = getSwizzledObject(propertyValue, propertyType);
            entity.getMethod(getMethodNameFromPropertyName(key, "set"),
                propertyType).invoke(entityInstance, propertyValue);
          }
        }

        // validations check..
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        Validator validator = validatorFactory.getValidator();

        violations = validator.validate(entityInstance);
        if (violations.isEmpty()) {
          entity.getMethod("persist").invoke(entityInstance);
        }
      }

      // return data back.
      return getReturnRecord(writeOperation, entityInstance, recordObject,
          violations);
    } catch (Exception ex) {
      return getReturnRecordForException(writeOperation, recordObject, ex);
    }
  }

  private Collection<Property<?>> allProperties(Class<? extends Record> clazz) {
    Set<Property<?>> rtn = new HashSet<Property<?>>();
    for (Field f : clazz.getFields()) {
      if (Modifier.isStatic(f.getModifiers())
          && Property.class.isAssignableFrom(f.getType())) {
        try {
          rtn.add((Property<?>) f.get(null));
        } catch (IllegalAccessException e) {
          throw new RuntimeException(e);
        }
      }
    }
    return rtn;
  }

  @SuppressWarnings("unchecked")
  private void ensureConfig() {
    if (config == null) {
      synchronized (this) {
        if (config != null) {
          return;
        }
        try {
          final String serverOperation = getServletContext().getInitParameter(
              SERVER_OPERATION_CONTEXT_PARAM);
          if (null == serverOperation) {
            failConfig();
          }
          Class<?> clazz = Class.forName(serverOperation);
          if (Config.class.isAssignableFrom(clazz)) {
            config = ((Class<? extends Config>) clazz).newInstance();

            // initialize tokenToEntity map
            tokenToEntityRecord = new HashMap<String, EntityRecordPair>();
            for (Class<? extends Record> recordClass : config.recordTypes()) {
              ServerType serverType = recordClass.getAnnotation(ServerType.class);
              String token = (String) recordClass.getField("TOKEN").get(null);
              if (token == null) {
                throw new IllegalStateException("TOKEN field on "
                    + recordClass.getName() + " can not be null");
              }
              EntityRecordPair previousValue = tokenToEntityRecord.get(token);
              if (previousValue != null) {
                throw new IllegalStateException(
                    "TOKEN fields have to be unique. TOKEN fields for both "
                        + recordClass.getName() + " and "
                        + previousValue.record.getName()
                        + " have the same value, value = " + token);
              }
              tokenToEntityRecord.put(token, new EntityRecordPair(
                  serverType.type(), recordClass));
            }
          }

        } catch (ClassNotFoundException e) {
          failConfig(e);
        } catch (InstantiationException e) {
          failConfig(e);
        } catch (IllegalAccessException e) {
          failConfig(e);
        } catch (SecurityException e) {
          failConfig(e);
        } catch (ClassCastException e) {
          failConfig(e);
        } catch (NoSuchFieldException e) {
          failConfig(e);
        }
      }
    }
  }

  private void failConfig() {
    failConfig(null);
  }

  private void failConfig(Throwable e) {
    final String message = String.format("Context parameter \"%s\" must name "
        + "a default instantiable configuration class implementing %s",
        SERVER_OPERATION_CONTEXT_PARAM, RequestFactory.Config.class.getName());

    throw new IllegalStateException(message, e);
  }

  private String getContent(HttpServletRequest request) throws IOException {
    int contentLength = request.getContentLength();
    byte contentBytes[] = new byte[contentLength];
    BufferedInputStream bis = new BufferedInputStream(request.getInputStream());
    try {
      int contentBytesOffset = 0;
      int readLen;
      while ((readLen = bis.read(contentBytes, contentBytesOffset,
          contentLength - contentBytesOffset)) > 0) {
        contentBytesOffset += readLen;
      }
      // TODO: encoding issues?
      return new String(contentBytes);
    } finally {
      bis.close();
    }
  }

  private Object getEntityInstance(WriteOperation writeOperation,
      Class<?> entity, Object idValue, Class<?> idType)
      throws SecurityException, InstantiationException, IllegalAccessException,
      InvocationTargetException, NoSuchMethodException {

    if (writeOperation == WriteOperation.CREATE) {
      return entity.getConstructor().newInstance();
    }
    // TODO: check "version" validity.
    return entity.getMethod("find" + entity.getSimpleName(), idType).invoke(
        null, getSwizzledObject(idValue, idType));
  }

  /**
   * Converts the returnValue of a 'get' method to a JSONArray.
   * 
   * @param resultObject object returned by a 'get' method, must be of type
   *          List<?>
   * @return the JSONArray
   */
  private JSONArray getJsonArray(List<?> resultList,
      Class<? extends Record> entityKeyClass) throws IllegalArgumentException,
      SecurityException, IllegalAccessException, JSONException,
      NoSuchMethodException, InvocationTargetException {
    JSONArray jsonArray = new JSONArray();
    if (resultList.size() == 0) {
      return jsonArray;
    }

    for (Object entityElement : resultList) {
      jsonArray.put(getJsonObject(entityElement, entityKeyClass));
    }
    return jsonArray;
  }

  private JSONObject getJsonObject(Object entityElement,
      Class<? extends Record> entityKeyClass) throws JSONException,
      NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    JSONObject jsonObject = new JSONObject();
    for (Property<?> p : allProperties(entityKeyClass)) {

      if (requestedProperty(p)) {
        String propertyName = p.getName();
        jsonObject.put(propertyName, getPropertyValueFromDataStore(
            entityElement, propertyName));
      }
    }
    return jsonObject;
  }

  /**
   * Returns methodName corresponding to the propertyName that can be invoked on
   * an entity.
   * 
   * Example: "userName" returns prefix + "UserName". "version" returns prefix +
   * "Version"
   */
  private String getMethodNameFromPropertyName(String propertyName,
      String prefix) {
    if (propertyName == null) {
      throw new NullPointerException("propertyName must not be null");
    }

    StringBuffer methodName = new StringBuffer(prefix);
    methodName.append(propertyName.substring(0, 1).toUpperCase());
    methodName.append(propertyName.substring(1));
    return methodName.toString();
  }

  private RequestDefinition getOperation(String operationName) {
    RequestDefinition operation;
    operation = config.requestDefinitions().get(operationName);
    if (null == operation) {
      throw new IllegalArgumentException("Unknown operation " + operationName);
    }
    return operation;
  }

  /**
   * @param jsonObject
   * @return
   * @throws JSONException
   */
  private Map<String, String> getParameterMap(JSONObject jsonObject)
      throws JSONException {
    Map<String, String> parameterMap = new HashMap<String, String>();
    Iterator<?> keys = jsonObject.keys();
    while (keys.hasNext()) {
      String key = keys.next().toString();
      if (key.startsWith(RequestDataManager.PARAM_TOKEN)) {
        parameterMap.put(key, jsonObject.getString(key));
      }
    }
    return parameterMap;
  }

  /**
   * Returns the property fields (name => type) for a record.
   */
  private Map<String, Class<?>> getPropertiesFromRecord(
      Class<? extends Record> record) throws SecurityException,
      IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    Map<String, Class<?>> properties = new HashMap<String, Class<?>>();
    for (Field f : record.getFields()) {
      if (Property.class.isAssignableFrom(f.getType())) {
        Class<?> propertyType = (Class<?>) f.getType().getMethod("getType").invoke(
            f.get(null));
        properties.put(f.getName(), propertyType);
      }
    }
    return properties;
  }

  /**
   * Returns the propertyValue in the right type, from the DataStore. The value
   * is sent into the response.
   */
  private Object getPropertyValueFromDataStore(Object entityElement,
      String propertyName) throws SecurityException, NoSuchMethodException,
      IllegalAccessException, InvocationTargetException {
    String methodName = getMethodNameFromPropertyName(propertyName, "get");
    Method method = entityElement.getClass().getMethod(methodName);
    Object returnValue = method.invoke(entityElement);
    /*
     * TODO: make these conventions more prominent. 1. encoding long as String
     * 2. encoding Date as Double
     */
    if (returnValue instanceof java.lang.Long) {
      return returnValue.toString();
    }
    if (returnValue instanceof java.util.Date) {
      return new Double(((java.util.Date) returnValue).getTime());
    }
    return returnValue;
  }

  /**
   * Returns the property value, in the specified type, from the request object.
   * The value is put in the DataStore.
   */
  private Object getPropertyValueFromRequest(JSONObject recordObject,
      String key, Class<?> propertyType) throws JSONException {
    if (propertyType == java.lang.Integer.class) {
      return new Integer(recordObject.getInt(key));
    }
    /*
     * 1. decode String to long. 2. decode Double to Date.
     */
    if (propertyType == java.lang.Long.class) {
      return Long.valueOf(recordObject.getString(key));
    }
    if (propertyType == java.util.Date.class) {
      return new Date((long) recordObject.getDouble(key));
    }
    return recordObject.get(key);
  }

  private JSONObject getReturnRecord(WriteOperation writeOperation,
      Object entityInstance, JSONObject recordObject,
      Set<ConstraintViolation<Object>> violations) throws SecurityException,
      JSONException, IllegalAccessException, InvocationTargetException,
      NoSuchMethodException {
    // id/futureId, the identifying field is sent back from the incoming record.
    JSONObject returnObject = new JSONObject();
    final boolean hasViolations = violations != null && !violations.isEmpty();
    if (hasViolations) {
      returnObject.put("violations", getViolationsAsJson(violations));
    }
    switch (writeOperation) {
      case CREATE:
        returnObject.put("futureId", recordObject.getString("id"));
        if (!hasViolations) {
          returnObject.put("id", getPropertyValueFromDataStore(entityInstance,
              "id"));
          returnObject.put("version", getPropertyValueFromDataStore(
              entityInstance, "version"));
        }
        break;
      case DELETE:
        returnObject.put("id", recordObject.getString("id"));
        break;
      case UPDATE:
        returnObject.put("id", recordObject.getString("id"));
        if (!hasViolations) {
          returnObject.put("version", getPropertyValueFromDataStore(
              entityInstance, "version"));
        }
        break;
    }
    return returnObject;
  }

  private JSONObject getReturnRecordForException(WriteOperation writeOperation,
      JSONObject recordObject, Exception ex) {
    JSONObject returnObject = new JSONObject();
    try {
      if (writeOperation == WriteOperation.DELETE
          || writeOperation == WriteOperation.UPDATE) {
        returnObject.put("id", recordObject.getString("id"));
      } else {
        returnObject.put("futureId", recordObject.getString("id"));
      }
      // expecting violations to be a JSON object.
      JSONObject violations = new JSONObject();
      if (ex instanceof NumberFormatException) {
        violations.put("Expected a number instead of String", ex.getMessage());
      } else {
        violations.put(ex.toString(), ex.getMessage());
      }
      returnObject.put("violations", violations);
    } catch (JSONException e) {
      // ignore.
      e.printStackTrace();
    }
    return returnObject;
  }

  /**
   * Swizzle an idValue received from the client to the type expected by the
   * server. Return the object of the new type.
   */
  private Object getSwizzledObject(Object idValue, Class<?> idType) {
    if (idValue.getClass() == idType) {
      return idValue;
    }
    // swizzle from String to Long
    if (idValue.getClass() == String.class && idType == Long.class) {
      return new Long((String) idValue);
    }
    if (idType == Double.class) {
      if (idValue.getClass() == Integer.class) {
        return new Double((Integer) idValue);
      }
      if (idValue.getClass() == Long.class) {
        return new Double((Long) idValue);
      }
      if (idValue.getClass() == Float.class) {
        return new Double((Float) idValue);
      }
    }
    throw new IllegalArgumentException("id is of type: " + idValue.getClass()
        + ",  expected type: " + idType);
  }

  private JSONObject getViolationsAsJson(
      Set<ConstraintViolation<Object>> violations) throws JSONException {
    JSONObject violationsAsJson = new JSONObject();
    for (ConstraintViolation<Object> violation : violations) {
      violationsAsJson.put(violation.getPropertyPath().toString(),
          violation.getMessage());
    }
    return violationsAsJson;
  }

  private Object invokeStaticDomainMethod(Method domainMethod, Object args[])
      throws IllegalAccessException, InvocationTargetException {
    return domainMethod.invoke(null, args);
  }

  /**
   * returns true if the property has been requested. TODO: use the properties
   * that should be coming with the request.
   * 
   * @param p the field of entity ref
   * @return has the property value been requested
   */
  private boolean requestedProperty(Property<?> p) {
    return !BLACK_LIST.contains(p.getName());
  }

  private void sync(String content, PrintWriter writer)
      throws SecurityException, NoSuchMethodException, IllegalAccessException,
      InvocationTargetException, InstantiationException {

    try {
      JSONObject jsonObject = new JSONObject(content);
      JSONObject returnJsonObject = new JSONObject();
      for (WriteOperation writeOperation : WriteOperation.values()) {
        if (!jsonObject.has(writeOperation.name())) {
          continue;
        }
        JSONArray reportArray = new JSONArray(
            jsonObject.getString(writeOperation.name()));
        JSONArray returnArray = new JSONArray();

        int length = reportArray.length();
        if (length == 0) {
          throw new IllegalArgumentException("No json array for "
              + writeOperation.name() + " should have been sent");
        }
        for (int i = 0; i < length; i++) {
          JSONObject recordWithSchema = reportArray.getJSONObject(i);
          Iterator<?> iterator = recordWithSchema.keys();
          String recordToken = (String) iterator.next();
          if (iterator.hasNext()) {
            throw new IllegalArgumentException(
                "There cannot be more than one record token");
          }
          JSONObject recordObject = recordWithSchema.getJSONObject(recordToken);
          JSONObject returnObject = updateRecordInDataStore(recordToken,
              recordObject, writeOperation);
          returnArray.put(returnObject);
        }
        returnJsonObject.put(writeOperation.name(), returnArray);
      }
      writer.print(returnJsonObject.toString());
    } catch (JSONException e) {
      throw new IllegalArgumentException("sync failed: ", e);
    }
  }

  /**
   * Update propertiesInRecord based on the types of entity.
   */
  private void updatePropertyTypes(Map<String, Class<?>> propertiesInRecord,
      Class<?> entity) {
    for (Field field : entity.getDeclaredFields()) {
      Class<?> fieldType = propertiesInRecord.get(field.getName());
      if (fieldType != null) {
        propertiesInRecord.put(field.getName(), field.getType());
      }
    }
  }

  private void validateKeys(JSONObject recordObject,
      Set<String> declaredProperties) {
    Iterator<?> keys = recordObject.keys();
    while (keys.hasNext()) {
      String key = (String) keys.next();
      if (!declaredProperties.contains(key)) {
        throw new IllegalArgumentException("key " + key
            + " is not permitted to be set");
      }
    }
  }
}
