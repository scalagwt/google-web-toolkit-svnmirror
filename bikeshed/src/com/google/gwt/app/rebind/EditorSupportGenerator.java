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
package com.google.gwt.app.rebind;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JParameterizedType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.TakesValue;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.PrintWriterManager;
import com.google.gwt.user.rebind.SourceWriter;
import com.google.gwt.valuestore.shared.Property;
import com.google.gwt.valuestore.ui.RecordEditView;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * Generates implementations of
 * {@link com.google.gwt.requestfactory.shared.RequestFactory RequestFactory}
 * and its nested interfaces.
 */
public class EditorSupportGenerator extends Generator {

  @Override
  public String generate(TreeLogger logger, GeneratorContext generatorContext,
      String interfaceName) throws UnableToCompleteException {
    // The TypeOracle knows about all types in the type system
    TypeOracle typeOracle = generatorContext.getTypeOracle();

    // Get a reference to the type that the generator should implement
    JClassType interfaceType = typeOracle.findType(interfaceName);

    // Ensure that the requested type exists
    if (interfaceType == null) {
      logger.log(TreeLogger.ERROR, "Could not find requested typeName: "
          + interfaceName);
      throw new UnableToCompleteException();
    }
    if (interfaceType.isInterface() == null) {
      // The incoming type wasn't a plain interface, we don't support
      // abstract base classes
      logger.log(TreeLogger.ERROR, interfaceType.getQualifiedSourceName()
          + " is not an interface.", null);
      throw new UnableToCompleteException();
    }

    // grandparent of the type being generated
    Map<String, JClassType> auxiliaryTypes = getAuxiliaryTypes(interfaceType,
        logger);
    String implName = getImplName(auxiliaryTypes, logger);
    String packageName = interfaceType.getPackage().getName();
    PrintWriterManager printWriters = new PrintWriterManager(generatorContext,
        logger, packageName);
    PrintWriter out = printWriters.tryToMakePrintWriterFor(implName);

    // If an implementation already exists, we don't need to do any work
    if (out != null) {
      generateOnce(logger, generatorContext, out, interfaceType, packageName,
          implName, auxiliaryTypes);
      printWriters.commit();
    }

    return packageName + "." + implName;
  }

  /**
   * returns true if the change handlers are to be generated.
   */
  private boolean generateChangeHandlers(JClassType type,
      JClassType takesValueType) {
    if (type.isAssignableTo(takesValueType)) {
      return true;
    }
    return false;
  }

  private void generateOnce(TreeLogger logger,
      GeneratorContext generatorContext, PrintWriter out,
      JClassType interfaceType, String packageName, String implName,
      Map<String, JClassType> auxiliaryTypes) throws UnableToCompleteException {

    logger = logger.branch(TreeLogger.DEBUG, String.format(
        "Generating implementation of %s", interfaceType.getName()));

    ClassSourceFileComposerFactory f = new ClassSourceFileComposerFactory(
        packageName, implName);
    f.addImport(ValueChangeEvent.class.getName());
    f.addImport(ValueChangeHandler.class.getName());
    f.addImport(auxiliaryTypes.get("recordType").getQualifiedSourceName());
    f.addImport(Property.class.getName());
    f.addImport(RecordEditView.class.getName());

    f.addImport(HashSet.class.getName());
    f.addImport(Set.class.getName());
    f.addImplementedInterface(interfaceType.getName());

    SourceWriter sw = f.createSourceWriter(generatorContext, out);
    sw.println();

    JClassType takesValueType = generatorContext.getTypeOracle().findType(
        TakesValue.class.getName());
    JClassType recordType = auxiliaryTypes.get("recordType");
    JClassType viewType = auxiliaryTypes.get("viewType");
    writeGetPropertiesMethod(sw, recordType);

    Set<JField> uiPropertyFields = getUiPropertyFields(viewType, recordType);
    writeInit(sw, viewType, recordType, uiPropertyFields, takesValueType,
        logger);
    writeSetEnabledMethod(sw, viewType, uiPropertyFields, takesValueType);
    writeSetValueMethod(sw, recordType, viewType, uiPropertyFields,
        generatorContext, logger);

    sw.outdent();
    sw.println("}");
  }

  private Set<String> getAccessibleFields(JClassType classType) {
    boolean isInterface = false;
    if (classType.isInterface() != null) {
      isInterface = true;
    }
    Map<String, JField> fieldsByName = new HashMap<String, JField>();
    LinkedList<JClassType> classesToBeProcessed = new LinkedList<JClassType>();
    classesToBeProcessed.add(classType);
    JClassType tempClassType = null;
    while (classesToBeProcessed.peek() != null) {
      tempClassType = classesToBeProcessed.remove();
      JField declaredFields[] = tempClassType.getFields();
      for (JField field : declaredFields) {
        if (field.isPrivate()
            || !(field.getType().getQualifiedSourceName().equals(Property.class.getName()))) {
          continue;
        }
        JField existing = fieldsByName.put(field.getName(), field);
        if (existing != null) {
          if (existing.getEnclosingType().isAssignableTo(
              field.getEnclosingType())) {
            fieldsByName.put(field.getName(), existing);
          }
        }
      }
      if (isInterface) {
        classesToBeProcessed.addAll(Arrays.asList(tempClassType.getImplementedInterfaces()));
      } else {
        classesToBeProcessed.add(tempClassType.getSuperclass());
      }
    }
    return fieldsByName.keySet();
  }

  private Collection<JMethod> getAccessibleMethods(JClassType classType) {
    boolean isInterface = false;
    if (classType.isInterface() != null) {
      isInterface = true;
    }
    Map<String, JMethod> methodsBySignature = new HashMap<String, JMethod>();
    LinkedList<JClassType> classesToBeProcessed = new LinkedList<JClassType>();
    classesToBeProcessed.add(classType);
    JClassType tempClassType = null;
    while (classesToBeProcessed.peek() != null) {
      tempClassType = classesToBeProcessed.remove();
      JMethod declaredMethods[] = tempClassType.getMethods();
      for (JMethod method : declaredMethods) {
        if (method.isPrivate()) {
          continue;
        }
        String signature = method.getJsniSignature();
        JMethod existing = methodsBySignature.put(signature, method);
        if (existing != null) {
          // decide which implementation to keep
          if (existing.getEnclosingType().isAssignableTo(
              method.getEnclosingType())) {
            methodsBySignature.put(signature, existing);
          }
        }
      }
      if (isInterface) {
        classesToBeProcessed.addAll(Arrays.asList(tempClassType.getImplementedInterfaces()));
      } else {
        classesToBeProcessed.add(tempClassType.getSuperclass());
      }
    }
    return methodsBySignature.values();
  }

  private Map<String, JClassType> getAuxiliaryTypes(JClassType interfaceType,
      TreeLogger logger) throws UnableToCompleteException {
    JClassType superInterfaces[] = interfaceType.getImplementedInterfaces();
    JClassType superinterfaceType = superInterfaces[0];
    if (superinterfaceType.isInterface() == null
        || !(superinterfaceType instanceof JParameterizedType)) {
      logger.log(TreeLogger.ERROR, "The superclass of "
          + superinterfaceType.getQualifiedSourceName()
          + " is either not an interface or not a generic type");
      throw new UnableToCompleteException();
    }

    // TODO: clean up this code.
    Map<String, JClassType> auxiliaryTypes = new HashMap<String, JClassType>();
    JParameterizedType parameterizedType = (JParameterizedType) superinterfaceType;
    JClassType typeParameters[] = parameterizedType.getTypeArgs();
    auxiliaryTypes.put("rawType", parameterizedType.getRawType());
    auxiliaryTypes.put("recordType", typeParameters[0]);
    auxiliaryTypes.put("viewType", typeParameters[1]);
    return auxiliaryTypes;
  }

  /**
   * returns the name of the Impl class.
   */
  private String getImplName(Map<String, JClassType> auxiliaryTypes,
      TreeLogger logger) throws UnableToCompleteException {
    return auxiliaryTypes.get("viewType").getName() + "_DataBinderImpl";
  }

  private JMethod getPropertyFunction(JClassType recordType,
      String propertyFunctionName, TypeOracle typeOracle, TreeLogger logger)
      throws UnableToCompleteException {

    for (JMethod method : getAccessibleMethods(recordType)) {
      if (method.getName().equals(propertyFunctionName)
          && (method.getParameters() == null || method.getParameters().length == 0)) {
        return method;
      }
    }
    return null;
  }

  private String getPropertyFunctionName(String name, TreeLogger logger)
      throws UnableToCompleteException {
    if (name == null || name.length() < 1) {
      logger.log(TreeLogger.ERROR, "UiField name " + name
          + " is either null or less than a character long");
      throw new UnableToCompleteException();
    }
    return "get" + name.substring(0, 1).toUpperCase()
        + name.substring(1, name.length());
  }

  /**
   * Handle non-integer return types.
   */
  private String getSuffix(JMethod method, JClassType stringType) {
    JClassType returnType = (JClassType) method.getReturnType();
    if (returnType.isAssignableTo(stringType)) {
      return "";
    }
    return ".toString()";
  }

  private Set<JField> getUiPropertyFields(JClassType viewType,
      JClassType recordType) {
    Set<String> recordFieldNames = getAccessibleFields(recordType);
    Set<JField> uiPropertyFields = new HashSet<JField>();
    for (JField field : viewType.getFields()) {
      if (field.getAnnotation(UiField.class) != null
          && recordFieldNames.contains(field.getName())) {
        uiPropertyFields.add(field);
      }
    }
    return uiPropertyFields;
  }

  /**
   * Write the implementation for the getProperties() method.
   */
  private void writeGetPropertiesMethod(SourceWriter sw, JClassType recordType) {
    sw.indent();
    sw.println("public Set<Property<?>> getProperties() {");
    sw.indent();
    sw.println("Set<Property<?>> rtn = new HashSet<Property<?>>();");
    for (JField field : recordType.getFields()) {
      if (field.getType().getQualifiedSourceName().equals(
          Property.class.getName())) {
        sw.println("rtn.add(" + recordType.getName() + "." + field.getName()
            + ");");
      }
    }
    sw.println("return rtn;");
    sw.outdent();
    sw.println("}");
    sw.outdent();
  }

  private void writeInit(SourceWriter sw, JClassType viewType,
      JClassType recordType, Set<JField> uiPropertyFields,
      JClassType takesValueType, TreeLogger logger)
      throws UnableToCompleteException {
    sw.indent();
    sw.println("public void init(final " + viewType.getName() + " view) {");
    sw.indent();
    for (JField uiField : uiPropertyFields) {
      if (!generateChangeHandlers((JClassType) uiField.getType(),
          takesValueType)) {
        continue;
      }
      sw.println("view." + uiField.getName()
          + ".addValueChangeHandler(new ValueChangeHandler<String>() {");
      sw.indent();
      sw.println("public void onValueChange(ValueChangeEvent<String> event) {");
      sw.indent();
      JField recordField = recordType.getField(uiField.getName());
      if (recordField == null) {
        logger.log(TreeLogger.DEBUG, "Unable to find field name "
            + uiField.getName() + " in " + recordType.getQualifiedSourceName());
        continue;
      }
      sw.println("view.getDeltaValueStore().set(" + recordType.getName() + "."
          + recordField.getName() + ", view.getValue(),");
      sw.indent();
      sw.println("event.getValue());");
      sw.outdent();
      sw.outdent();
      sw.println("}");
      sw.outdent();
      sw.println("});");
    }
    sw.outdent();
    sw.println("}");
    sw.outdent();
  }

  private void writeSetEnabledMethod(SourceWriter sw, JClassType viewType,
      Set<JField> uiPropertyFields, JClassType takesValueType) {
    sw.indent();
    sw.println("public void setEnabled(" + viewType.getName()
        + " view, boolean enabled) {");
    sw.indent();
    sw.println("// Note that we require package protection, just like UiBinder does.");
    for (JField uiField : uiPropertyFields) {
      if (!((JClassType) uiField.getType()).isAssignableTo(takesValueType)) {
        continue;
      }
      sw.println("view." + uiField.getName() + ".setEnabled(enabled);");
    }
    sw.outdent();
    sw.println("}");
    sw.outdent();
  }

  private void writeSetValueMethod(SourceWriter sw, JClassType recordType,
      JClassType viewType, Set<JField> uiPropertyFields,
      GeneratorContext generatorContext, TreeLogger logger)
      throws UnableToCompleteException {
    JClassType hasTextType = generatorContext.getTypeOracle().findType(
        HasText.class.getName());
    JClassType takesValueType = generatorContext.getTypeOracle().findType(
        HasValue.class.getName());
    JClassType stringType = generatorContext.getTypeOracle().findType(
        "java.lang.String");
    sw.indent();
    sw.println("public void setValue(" + viewType.getName() + " view, "
        + recordType.getName() + " record) {");
    sw.indent();

    for (JField uiField : uiPropertyFields) {
      JType type = uiField.getType();
      if (!(type instanceof JClassType)) {
        continue;
      }
      JClassType classType = (JClassType) type;
      String propertyFunctionName = getPropertyFunctionName(uiField.getName(),
          logger);
      JMethod propertyFunction = getPropertyFunction(recordType,
          propertyFunctionName, generatorContext.getTypeOracle(), logger);
      if (propertyFunction == null) {
        logger.log(TreeLogger.WARN,
            "Not generating setValue/setText call for field " + uiField);
        continue;
      }
      JType paramTypes[] = new JType[1];
      paramTypes[0] = propertyFunction.getReturnType();
      JMethod setValueMethod = classType.findMethod("setValue", paramTypes);
      String suffix = "";
      String functionName = "";
      if (setValueMethod != null) {
        // the setValue method works!, no need to change suffix
        functionName = "setValue";
      } else {
        if (classType.isAssignableTo(takesValueType)) {
          functionName = "setValue";
        } else {
          if (classType.isAssignableTo(hasTextType)) {
            functionName = "setText";
          } else {
            functionName = "setValue";
          }
        }
        suffix = getSuffix(propertyFunction, stringType);
      }
      sw.println("view." + uiField.getName() + "." + functionName + "(record."
          + propertyFunctionName + "()" + suffix + ");");
    }
    sw.outdent();
    sw.println("}");
    sw.outdent();
  }

}
