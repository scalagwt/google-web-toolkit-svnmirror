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

import com.google.gwt.autobean.server.AutoBeanFactoryMagic;
import com.google.gwt.autobean.server.Configuration;
import com.google.gwt.autobean.server.impl.TypeUtils;
import com.google.gwt.autobean.shared.AutoBean;
import com.google.gwt.autobean.shared.AutoBeanCodex;
import com.google.gwt.autobean.shared.AutoBeanUtils;
import com.google.gwt.autobean.shared.AutoBeanVisitor;
import com.google.gwt.autobean.shared.Splittable;
import com.google.gwt.autobean.shared.ValueCodex;
import com.google.gwt.requestfactory.shared.BaseProxy;
import com.google.gwt.requestfactory.shared.EntityProxyId;
import com.google.gwt.requestfactory.shared.InstanceRequest;
import com.google.gwt.requestfactory.shared.Request;
import com.google.gwt.requestfactory.shared.ServerFailure;
import com.google.gwt.requestfactory.shared.WriteOperation;
import com.google.gwt.requestfactory.shared.impl.BaseProxyCategory;
import com.google.gwt.requestfactory.shared.impl.Constants;
import com.google.gwt.requestfactory.shared.impl.EntityCodex;
import com.google.gwt.requestfactory.shared.impl.EntityProxyCategory;
import com.google.gwt.requestfactory.shared.impl.SimpleProxyId;
import com.google.gwt.requestfactory.shared.impl.ValueProxyCategory;
import com.google.gwt.requestfactory.shared.messages.IdMessage.Strength;
import com.google.gwt.requestfactory.shared.messages.InvocationMessage;
import com.google.gwt.requestfactory.shared.messages.MessageFactory;
import com.google.gwt.requestfactory.shared.messages.OperationMessage;
import com.google.gwt.requestfactory.shared.messages.RequestMessage;
import com.google.gwt.requestfactory.shared.messages.ResponseMessage;
import com.google.gwt.requestfactory.shared.messages.ServerFailureMessage;
import com.google.gwt.requestfactory.shared.messages.ViolationMessage;
import com.google.gwt.user.server.Base64Utils;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;

/**
 * Processes request payloads from a RequestFactory client. This implementation
 * is stateless. A single instance may be reused and is thread-safe.
 */
public class SimpleRequestProcessor {
  /**
   * This parameterization is so long, it improves readability to have a
   * specific type.
   */
  @SuppressWarnings("serial")
  static class IdToEntityMap extends
      HashMap<SimpleProxyId<?>, AutoBean<? extends BaseProxy>> {
  }

  /**
   * Allows the creation of properly-configured AutoBeans without having to
   * create an AutoBeanFactory with the desired annotations.
   */
  static final Configuration CONFIGURATION = new Configuration.Builder().setCategories(
      EntityProxyCategory.class, ValueProxyCategory.class,
      BaseProxyCategory.class).setNoWrap(EntityProxyId.class).build();

  /**
   * Vends message objects.
   */
  static final MessageFactory FACTORY = AutoBeanFactoryMagic.create(MessageFactory.class);

  static String fromBase64(String encoded) {
    try {
      return new String(Base64Utils.fromBase64(encoded), "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new UnexpectedException(e);
    }
  }

  static String toBase64(String data) {
    try {
      return Base64Utils.toBase64(data.getBytes("UTF-8"));
    } catch (UnsupportedEncodingException e) {
      throw new UnexpectedException(e);
    }
  }

  private ExceptionHandler exceptionHandler = new DefaultExceptionHandler();
  private final ServiceLayer service;

  public SimpleRequestProcessor(ServiceLayer serviceLayer) {
    this.service = serviceLayer;
  }

  /**
   * Process a payload sent by a RequestFactory client.
   * 
   * @param payload the payload sent by the client
   * @return a payload to return to the client
   */
  public String process(String payload) {
    RequestMessage req = AutoBeanCodex.decode(FACTORY, RequestMessage.class,
        payload).as();
    AutoBean<ResponseMessage> responseBean = FACTORY.response();
    try {
      process(req, responseBean.as());
    } catch (ReportableException e) {
      e.printStackTrace();
      // Create a new response envelope, since the state is unknown
      responseBean = FACTORY.response();
      responseBean.as().setGeneralFailure(createFailureMessage(e).as());
    }
    // Return a JSON-formatted payload
    return AutoBeanCodex.encode(responseBean).getPayload();
  }

  public void setExceptionHandler(ExceptionHandler exceptionHandler) {
    this.exceptionHandler = exceptionHandler;
  }

  /**
   * Encode a list of objects into a self-contained message that can be used for
   * out-of-band communication.
   */
  <T> Splittable createOobMessage(List<T> domainValues) {
    RequestState state = new RequestState(service);

    List<Splittable> encodedValues = new ArrayList<Splittable>(
        domainValues.size());
    for (T domainValue : domainValues) {
      Object clientValue;
      if (domainValue == null) {
        clientValue = null;
      } else {
        Class<?> clientType = service.resolveClientType(domainValue.getClass(),
            BaseProxy.class, true);
        clientValue = state.getResolver().resolveClientValue(domainValue,
            clientType, Collections.<String> emptySet());
      }
      encodedValues.add(EntityCodex.encode(state, clientValue));
    }

    IdToEntityMap map = new IdToEntityMap();
    map.putAll(state.beans);
    List<OperationMessage> operations = new ArrayList<OperationMessage>();
    createReturnOperations(operations, state, map);

    InvocationMessage invocation = FACTORY.invocation().as();
    invocation.setParameters(encodedValues);

    AutoBean<RequestMessage> bean = FACTORY.request();
    RequestMessage resp = bean.as();
    resp.setInvocations(Collections.singletonList(invocation));
    resp.setOperations(operations);
    return AutoBeanCodex.encode(bean);
  }

  /**
   * Decode an out-of-band message.
   */
  <T> List<T> decodeOobMessage(Class<T> domainClass, Splittable payload) {
    Class<?> proxyType = service.resolveClientType(domainClass,
        BaseProxy.class, true);
    RequestState state = new RequestState(service);
    RequestMessage message = AutoBeanCodex.decode(FACTORY,
        RequestMessage.class, payload).as();
    processOperationMessages(state, message);
    List<Object> decoded = decodeInvocationArguments(state,
        message.getInvocations().get(0).getParameters(),
        new Class<?>[]{proxyType}, new Type[]{domainClass});

    @SuppressWarnings("unchecked")
    List<T> toReturn = (List<T>) decoded;
    return toReturn;
  }

  /**
   * Main processing method.
   */
  void process(RequestMessage req, ResponseMessage resp) {
    final RequestState source = new RequestState(service);
    // Apply operations
    processOperationMessages(source, req);

    // Validate entities
    List<ViolationMessage> errorMessages = validateEntities(source);

    if (!errorMessages.isEmpty()) {
      resp.setViolations(errorMessages);
      return;
    }

    RequestState returnState = new RequestState(source);

    // Invoke methods
    List<Splittable> invocationResults = new ArrayList<Splittable>();
    List<Boolean> invocationSuccess = new ArrayList<Boolean>();
    List<InvocationMessage> invlist = req.getInvocations();
    processInvocationMessages(source, invlist, invocationResults,
        invocationSuccess, returnState);

    // Store return objects
    List<OperationMessage> operations = new ArrayList<OperationMessage>();
    IdToEntityMap toProcess = new IdToEntityMap();
    toProcess.putAll(source.beans);
    toProcess.putAll(returnState.beans);
    createReturnOperations(operations, returnState, toProcess);

    resp.setInvocationResults(invocationResults);
    resp.setStatusCodes(invocationSuccess);
    if (!operations.isEmpty()) {
      resp.setOperations(operations);
    }
  }

  private AutoBean<ServerFailureMessage> createFailureMessage(
      ReportableException e) {
    ServerFailure failure = exceptionHandler.createServerFailure(e.getCause());
    AutoBean<ServerFailureMessage> bean = FACTORY.failure();
    ServerFailureMessage msg = bean.as();
    msg.setExceptionType(failure.getExceptionType());
    msg.setMessage(failure.getMessage());
    msg.setStackTrace(failure.getStackTraceString());
    msg.setFatal(failure.isFatal());
    return bean;
  }

  private void createReturnOperations(List<OperationMessage> operations,
      RequestState returnState, IdToEntityMap toProcess) {
    for (Map.Entry<SimpleProxyId<?>, AutoBean<? extends BaseProxy>> entry : toProcess.entrySet()) {
      SimpleProxyId<?> id = entry.getKey();

      AutoBean<? extends BaseProxy> bean = entry.getValue();
      Object domainObject = bean.getTag(Constants.DOMAIN_OBJECT);
      WriteOperation writeOperation;

      if (id.isEphemeral()) {
        // See if the entity has been persisted in the meantime
        returnState.getResolver().resolveClientValue(domainObject,
            id.getProxyClass(), Collections.<String> emptySet());
      }

      if (id.isEphemeral() || id.isSynthetic() || domainObject == null) {
        // If the object isn't persistent, there's no reason to send an update
        writeOperation = null;
      } else if (!service.isLive(domainObject)) {
        writeOperation = WriteOperation.DELETE;
      } else if (id.wasEphemeral()) {
        writeOperation = WriteOperation.PERSIST;
      } else {
        writeOperation = WriteOperation.UPDATE;
      }

      Splittable version = null;
      if (writeOperation == WriteOperation.PERSIST
          || writeOperation == WriteOperation.UPDATE) {
        /*
         * If we're sending an operation, the domain object must be persistent.
         * This means that it must also have a non-null version.
         */
        Object domainVersion = service.getVersion(domainObject);
        if (domainVersion == null) {
          throw new UnexpectedException("The persisted entity with id "
              + service.getId(domainObject) + " has a null version", null);
        }
        version = returnState.flatten(domainVersion);
      }

      boolean inResponse = bean.getTag(Constants.IN_RESPONSE) != null;

      /*
       * Don't send any data back to the client for an update on an object that
       * isn't part of the response payload when the client's version matches
       * the domain version.
       */
      if (WriteOperation.UPDATE.equals(writeOperation) && !inResponse) {
        String previousVersion = bean.<String> getTag(Constants.VERSION_PROPERTY_B64);
        if (version != null && previousVersion != null
            && version.equals(fromBase64(previousVersion))) {
          continue;
        }
      }

      OperationMessage op = FACTORY.operation().as();

      /*
       * Send a client id if the id is ephemeral or was previously associated
       * with a client id.
       */
      if (id.wasEphemeral()) {
        op.setClientId(id.getClientId());
      }

      op.setOperation(writeOperation);

      // Only send properties for entities that are part of the return graph
      if (inResponse) {
        Map<String, Splittable> propertyMap = new LinkedHashMap<String, Splittable>();
        // Add all non-null properties to the serialized form
        Map<String, Object> diff = AutoBeanUtils.getAllProperties(bean);
        for (Map.Entry<String, Object> d : diff.entrySet()) {
          Object value = d.getValue();
          if (value != null) {
            propertyMap.put(d.getKey(), EntityCodex.encode(returnState, value));
          }
        }
        op.setPropertyMap(propertyMap);
      }

      if (!id.isEphemeral() && !id.isSynthetic()) {
        // Send the server address only for persistent objects
        op.setServerId(toBase64(id.getServerId()));
      }

      if (id.isSynthetic()) {
        op.setStrength(Strength.SYNTHETIC);
        op.setSyntheticId(id.getSyntheticId());
      } else if (id.isEphemeral()) {
        op.setStrength(Strength.EPHEMERAL);
      }

      op.setTypeToken(service.resolveTypeToken(id.getProxyClass()));
      if (version != null) {
        op.setVersion(toBase64(version.getPayload()));
      }

      operations.add(op);
    }
  }

  /**
   * Decode the arguments to pass into the domain method. If the domain method
   * is not static, the instance object will be in the 0th position.
   */
  private List<Object> decodeInvocationArguments(RequestState source,
      InvocationMessage invocation, Method contextMethod) {
    boolean isStatic = Request.class.isAssignableFrom(contextMethod.getReturnType());
    int baseLength = contextMethod.getParameterTypes().length;
    int length = baseLength + (isStatic ? 0 : 1);
    int offset = isStatic ? 0 : 1;
    Class<?>[] contextArgs = new Class<?>[length];
    Type[] genericArgs = new Type[length];

    if (!isStatic) {
      genericArgs[0] = TypeUtils.getSingleParameterization(
          InstanceRequest.class, contextMethod.getGenericReturnType());
      contextArgs[0] = TypeUtils.ensureBaseType(genericArgs[0]);
    }
    System.arraycopy(contextMethod.getParameterTypes(), 0, contextArgs, offset,
        baseLength);
    System.arraycopy(contextMethod.getGenericParameterTypes(), 0, genericArgs,
        offset, baseLength);

    List<Object> args = decodeInvocationArguments(source,
        invocation.getParameters(), contextArgs, genericArgs);
    return args;
  }

  /**
   * Handles instance invocations as the instance at the 0th parameter.
   */
  private List<Object> decodeInvocationArguments(RequestState source,
      List<Splittable> parameters, Class<?>[] contextArgs, Type[] genericArgs) {
    if (parameters == null) {
      // Can't return Collections.emptyList() because this must be mutable
      return new ArrayList<Object>();
    }

    assert parameters.size() == contextArgs.length;
    List<Object> args = new ArrayList<Object>(contextArgs.length);
    for (int i = 0, j = contextArgs.length; i < j; i++) {
      Class<?> type = contextArgs[i];
      Class<?> elementType = null;
      Splittable split;
      if (Collection.class.isAssignableFrom(type)) {
        elementType = TypeUtils.ensureBaseType(TypeUtils.getSingleParameterization(
            Collection.class, genericArgs[i]));
        split = parameters.get(i);
      } else {
        split = parameters.get(i);
      }
      Object arg = EntityCodex.decode(source, type, elementType, split);
      arg = source.getResolver().resolveDomainValue(arg,
          !EntityProxyId.class.equals(contextArgs[i]));
      args.add(arg);
    }

    return args;
  }

  private void processInvocationMessages(RequestState state,
      List<InvocationMessage> invlist, List<Splittable> results,
      List<Boolean> success, RequestState returnState) {
    for (InvocationMessage invocation : invlist) {
      try {
        // Find the Method
        String[] operation = invocation.getOperation().split("::");
        Method contextMethod = service.resolveRequestContextMethod(
            operation[0], operation[1]);
        if (contextMethod == null) {
          throw new UnexpectedException("Cannot resolve operation "
              + invocation.getOperation(), null);
        }
        Method domainMethod = service.resolveDomainMethod(contextMethod);
        if (domainMethod == null) {
          throw new UnexpectedException("Cannot resolve domain method "
              + invocation.getOperation(), null);
        }

        // Compute the arguments
        List<Object> args = decodeInvocationArguments(state, invocation,
            contextMethod);
        // Possibly use a ServiceLocator
        if (service.requiresServiceLocator(contextMethod, domainMethod)) {
          Object serviceInstance = service.createServiceInstance(contextMethod,
              domainMethod);
          args.add(0, serviceInstance);
        }
        // Invoke it
        Object returnValue = service.invoke(domainMethod, args.toArray());

        // Convert domain object to client object
        Type requestReturnType = service.getRequestReturnType(contextMethod);
        returnValue = state.getResolver().resolveClientValue(returnValue,
            requestReturnType, invocation.getPropertyRefs());

        // Convert the client object to a string
        results.add(EntityCodex.encode(returnState, returnValue));
        success.add(true);
      } catch (ReportableException e) {
        results.add(AutoBeanCodex.encode(createFailureMessage(e)));
        success.add(false);
      }
    }
  }

  private void processOperationMessages(final RequestState state,
      RequestMessage req) {
    List<OperationMessage> operations = req.getOperations();
    if (operations == null) {
      return;
    }

    List<AutoBean<? extends BaseProxy>> beans = state.getBeansForPayload(operations);
    assert operations.size() == beans.size();

    Iterator<OperationMessage> itOp = operations.iterator();
    for (AutoBean<? extends BaseProxy> bean : beans) {
      OperationMessage operation = itOp.next();
      // Save the client's version information to reduce payload size later
      bean.setTag(Constants.VERSION_PROPERTY_B64, operation.getVersion());

      // Load the domain object with properties, if it exists
      final Object domain = bean.getTag(Constants.DOMAIN_OBJECT);
      if (domain != null) {
        // Apply any property updates
        final Map<String, Splittable> flatValueMap = operation.getPropertyMap();
        if (flatValueMap != null) {
          bean.accept(new AutoBeanVisitor() {
            @Override
            public boolean visitReferenceProperty(String propertyName,
                AutoBean<?> value, PropertyContext ctx) {
              // containsKey to distinguish null from unknown
              if (flatValueMap.containsKey(propertyName)) {
                Class<?> elementType = ctx instanceof CollectionPropertyContext
                    ? ((CollectionPropertyContext) ctx).getElementType() : null;
                Object newValue = EntityCodex.decode(state, ctx.getType(),
                    elementType, flatValueMap.get(propertyName));
                Object resolved = state.getResolver().resolveDomainValue(
                    newValue, false);
                service.setProperty(domain, propertyName,
                    service.resolveDomainClass(ctx.getType()), resolved);
              }
              return false;
            }

            @Override
            public boolean visitValueProperty(String propertyName,
                Object value, PropertyContext ctx) {
              if (flatValueMap.containsKey(propertyName)) {
                Splittable split = flatValueMap.get(propertyName);
                Object newValue = ValueCodex.decode(ctx.getType(), split);
                Object resolved = state.getResolver().resolveDomainValue(
                    newValue, false);
                service.setProperty(domain, propertyName, ctx.getType(),
                    resolved);
              }
              return false;
            }
          });
        }
      }
    }
  }

  /**
   * Validate all of the entities referenced in a RequestState.
   */
  private List<ViolationMessage> validateEntities(RequestState source) {
    List<ViolationMessage> errorMessages = new ArrayList<ViolationMessage>();
    for (Map.Entry<SimpleProxyId<?>, AutoBean<? extends BaseProxy>> entry : source.beans.entrySet()) {
      AutoBean<? extends BaseProxy> bean = entry.getValue();
      Object domainObject = bean.getTag(Constants.DOMAIN_OBJECT);

      // The object could have been deleted
      if (domainObject != null) {
        Set<ConstraintViolation<Object>> errors = service.validate(domainObject);
        if (errors != null && !errors.isEmpty()) {
          SimpleProxyId<?> id = entry.getKey();
          for (ConstraintViolation<Object> error : errors) {
            ViolationMessage message = FACTORY.violation().as();
            message.setClientId(id.getClientId());
            message.setMessage(error.getMessage());
            message.setPath(error.getPropertyPath().toString());
            if (id.isEphemeral()) {
              message.setClientId(id.getClientId());
              message.setStrength(Strength.EPHEMERAL);
            } else {
              message.setServerId(toBase64(id.getServerId()));
            }
            message.setTypeToken(service.resolveTypeToken(id.getProxyClass()));
            errorMessages.add(message);
          }
        }
      }
    }
    return errorMessages;
  }
}
