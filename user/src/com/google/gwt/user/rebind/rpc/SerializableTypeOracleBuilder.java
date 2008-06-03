/*
 * Copyright 2008 Google Inc.
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
package com.google.gwt.user.rebind.rpc;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.typeinfo.JArrayType;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.JGenericType;
import com.google.gwt.core.ext.typeinfo.JParameterizedType;
import com.google.gwt.core.ext.typeinfo.JRawType;
import com.google.gwt.core.ext.typeinfo.JRealClassType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.JTypeParameter;
import com.google.gwt.core.ext.typeinfo.JWildcardType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.dev.util.log.PrintWriterTreeLogger;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.rebind.rpc.TypeParameterExposureComputer.TypeParameterFlowInfo;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

/**
 * Builds a {@link SerializableTypeOracle} for a given set of root types.
 * 
 * <p>
 * There are two goals for this builder. First, discover the set of serializable
 * types that can be serialized if you serialize one of the root types. Second,
 * to make sure that all root types can actually be serialized by GWT.
 * </p>
 * 
 * <p>
 * To find the serializable types, it includes the root types, and then it
 * iteratively traverses the type hierarchy and the fields of any type already
 * deemed serializable. To improve the accuracy of the traversal there is a
 * computations of the exposure of type parameters. When the traversal reaches a
 * parameterized type, these exposure values are used to determine how to treat
 * the arguments.
 * </p>
 * 
 * <p>
 * A type qualifies for serialization if it or one of its subtypes is
 * automatically or manually serializable. Automatic serialization is selected
 * if the type is assignable to {@link IsSerializable} or {@link Serializable}
 * or if the type is a primitive type such as int, boolean, etc. Manual
 * serialization is selected if there exists another type with the same fully
 * qualified name concatenated with "_CustomFieldSerializer". If a type
 * qualifies for both manual and automatic serialization, manual serialization
 * is preferred.
 * </p>
 */
public class SerializableTypeOracleBuilder {
  private interface Path {
    Path getParent();

    String toString();
  }

  private enum TypeState {
    /**
     * The instantiability of a type has been determined.
     */
    CHECK_DONE("Check succeeded"),
    /**
     * The instantiability of a type is being checked.
     */
    CHECK_IN_PROGRESS("Check in progress"),
    /**
     * The instantiability of a type has not been checked.
     */
    NOT_CHECKED("Not checked");

    private final String message;

    TypeState(String message) {
      this.message = message;
    }

    @Override
    public String toString() {
      return message;
    }
  }

  private class TypeInfoComputed {

    /**
     * <code>true</code> if the type is assignable to {@link IsSerializable}
     * or {@link java.io.Serializable Serializable}.
     */
    private final boolean autoSerializable;

    /**
     * <code>true</code> if the this type directly implements one of the
     * marker interfaces.
     */
    private final boolean directlyImplementsMarker;

    /**
     * <code>true</code> if the type is automatically or manually serializable
     * and the corresponding checks succeed.
     */
    private boolean fieldSerializable = false;

    /**
     * <code>true</code> if this type might be instantiated.
     */
    private boolean instantiable = false;

    /**
     * <code>true</code> if there are instantiable subtypes assignable to this
     * one.
     */
    private boolean instantiableSubtypes;

    /**
     * Custom field serializer or <code>null</code> if there isn't one.
     */
    private final JClassType manualSerializer;

    /**
     * Path used to discover this type.
     */
    private final Path path;

    /**
     * The state that this type is currently in.
     */
    private TypeState state = TypeState.NOT_CHECKED;

    /**
     * {@link JClassType} associated with this metadata.
     */
    private final JClassType type;

    public TypeInfoComputed(JClassType type, Path path) {
      this.type = type;
      this.path = path;
      autoSerializable = SerializableTypeOracleBuilder.isAutoSerializable(type);
      manualSerializer = findCustomFieldSerializer(typeOracle, type);
      directlyImplementsMarker = directlyImplementsMarkerInterface(type);
    }

    public JClassType getManualSerializer() {
      return manualSerializer;
    }

    public Path getPath() {
      return path;
    }

    public JClassType getType() {
      return type;
    }

    public boolean hasInstantiableSubtypes() {
      return isInstantiable() || instantiableSubtypes;
    }

    public boolean isAutoSerializable() {
      return autoSerializable;
    }

    public boolean isDeclaredSerializable() {
      return autoSerializable || isManuallySerializable();
    }

    public boolean isDirectlySerializable() {
      return directlyImplementsMarker || isManuallySerializable();
    }

    public boolean isDone() {
      return state == TypeState.CHECK_DONE;
    }

    public boolean isFieldSerializable() {
      return fieldSerializable;
    }

    public boolean isInstantiable() {
      return instantiable;
    }

    public boolean isManuallySerializable() {
      return manualSerializer != null;
    }

    public boolean isPendingInstantiable() {
      return state == TypeState.CHECK_IN_PROGRESS;
    }

    public void setFieldSerializable() {
      fieldSerializable = true;
    }

    public void setInstantiable(boolean instantiable) {
      this.instantiable = instantiable;
      if (instantiable) {
        fieldSerializable = true;
      }
      state = TypeState.CHECK_DONE;
    }

    public void setInstantiableSubytpes(boolean instantiableSubtypes) {
      this.instantiableSubtypes = instantiableSubtypes;
    }

    public void setPendingInstantiable() {
      state = TypeState.CHECK_IN_PROGRESS;
    }
  }

  /**
   * Compares {@link JType}s according to their qualified source names.
   */
  static final Comparator<JType> JTYPE_COMPARATOR = new Comparator<JType>() {
    public int compare(JType t1, JType t2) {
      return t1.getQualifiedSourceName().compareTo(t2.getQualifiedSourceName());
    }
  };

  /**
   * No type filtering by default..
   */
  private static final TypeFilter DEFAULT_TYPE_FILTER = new TypeFilter() {
    public String getName() {
      return "Default";
    }

    public boolean isAllowed(JClassType type) {
      return true;
    }
  };

  private static final JClassType[] NO_JCLASSES = new JClassType[0];

  static boolean canBeInstantiated(TreeLogger logger, JClassType type,
      TreeLogger.Type logLevel) {
    if (type.isEnum() == null) {
      if (type.isAbstract()) {
        // Abstract types will be picked up if there is an instantiable
        // subtype.
        return false;
      }

      if (!type.isDefaultInstantiable() && !isManuallySerializable(type)) {
        // Warn and return false.
        logger.log(
            logLevel,
            "Was not default instantiable (it must have a zero-argument constructor or no constructors at all)",
            null);
        return false;
      }
    } else {
      /*
       * Enums are always instantiable regardless of abstract or default
       * instantiability.
       */
    }

    return true;
  }

  /**
   * Finds the custom field serializer for a given type.
   * 
   * @param typeOracle
   * @param type
   * @return the custom field serializer for a type or <code>null</code> if
   *         there is not one
   */
  static JClassType findCustomFieldSerializer(TypeOracle typeOracle, JType type) {
    JClassType classOrInterface = type.isClassOrInterface();
    if (classOrInterface == null) {
      return null;
    }

    String customFieldSerializerName = type.getQualifiedSourceName()
        + "_CustomFieldSerializer";
    JClassType customSerializer = typeOracle.findType(customFieldSerializerName);
    if (customSerializer == null) {
      // If the type is in the java.lang or java.util packages then it will be
      // mapped into com.google.gwt.user.client.rpc.core package
      customSerializer = typeOracle.findType("com.google.gwt.user.client.rpc.core."
          + customFieldSerializerName);
    }

    return customSerializer;
  }

  static boolean isAutoSerializable(JClassType type) {
    try {
      JClassType isSerializable = getIsSerializableMarkerInterface(type);
      JClassType serializable = getSerializableMarkerInterface(type);
      return type.isAssignableTo(isSerializable)
          || type.isAssignableTo(serializable);
    } catch (NotFoundException e) {
      return false;
    }
  }

  /**
   * Returns <code>true</code> if this type is part of the standard java
   * packages.
   * 
   * NOTE: This code is copied from
   * {@link com.google.gwt.dev.shell.CompilingClassLoader CompilingClassLoader};
   * don't change one without changing the other.
   */
  static boolean isInStandardJavaPackage(String className) {
    if (className.startsWith("java.")) {
      return true;
    }

    if (className.startsWith("javax.")) {
      return true;
    }

    return false;
  }

  static void recordTypeParametersIn(JType type, Set<JTypeParameter> params) {
    JTypeParameter isTypeParameter = type.isTypeParameter();
    if (isTypeParameter != null) {
      params.add(isTypeParameter);
    }

    JArrayType isArray = type.isArray();
    if (isArray != null) {
      recordTypeParametersIn(isArray.getComponentType(), params);
    }

    JWildcardType isWildcard = type.isWildcard();
    if (isWildcard != null) {
      for (JClassType bound : isWildcard.getUpperBounds()) {
        recordTypeParametersIn(bound, params);
      }
    }

    JParameterizedType isParameterized = type.isParameterized();
    if (isParameterized != null) {
      for (JClassType arg : isParameterized.getTypeArgs()) {
        recordTypeParametersIn(arg, params);
      }
    }
  }

  /**
   * Return <code>true</code> if a class's fields should be considered for
   * serialization. If it returns <code>false</code> then none of the fields
   * of this class should be serialized.
   */
  static boolean shouldConsiderFieldsForSerialization(TreeLogger logger,
      JClassType type, boolean isSpeculative, TypeFilter filter) {
    if (!isAllowedByFilter(logger, filter, type, isSpeculative)) {
      return false;
    }

    if (!isDeclaredSerializable(type)) {
      logger.branch(TreeLogger.DEBUG, "Type '"
          + type.getParameterizedQualifiedSourceName()
          + "' is not assignable to '" + IsSerializable.class.getName()
          + "' or '" + Serializable.class.getName()
          + "' nor does it have a custom field serializer", null);
      return false;
    }

    if (isManuallySerializable(type)) {
      JClassType manualSerializer = findCustomFieldSerializer(type.getOracle(),
          type);
      assert (manualSerializer != null);

      List<String> problems = CustomFieldSerializerValidator.validate(
          manualSerializer, type);
      if (!problems.isEmpty()) {
        for (String problem : problems) {
          logger.branch(getLogLevel(isSpeculative), problem, null);
        }
        return false;
      }
    } else {
      assert (isAutoSerializable(type));

      /*
       * Speculative paths log at DEBUG level, non-speculative ones log at WARN
       * level.
       */
      TreeLogger.Type logLevel = isSpeculative ? TreeLogger.DEBUG
          : TreeLogger.WARN;

      if (!isAccessibleToSerializer(type)) {
        // Class is not visible to a serializer class in the same package
        logger.branch(
            logLevel,
            type.getParameterizedQualifiedSourceName()
                + " is not accessible from a class in its same package; it will be excluded from the set of serializable types",
            null);
        return false;
      }

      if (type.isLocalType()) {
        // Local types cannot be serialized
        logger.branch(
            logLevel,
            type.getParameterizedQualifiedSourceName()
                + " is a local type; it will be excluded from the set of serializable types",
            null);
        return false;
      }

      if (type.isMemberType() && !type.isStatic()) {
        // Non-static member types cannot be serialized
        logger.branch(
            logLevel,
            type.getParameterizedQualifiedSourceName()
                + " is nested but not static; it will be excluded from the set of serializable types",
            null);
        return false;
      }
    }

    return true;
  }

  /**
   * Returns <code>true</code> if the field qualifies for serialization
   * without considering its type.
   */
  static boolean shouldConsiderForSerialization(TreeLogger logger, JField field) {
    if (field.isStatic() || field.isTransient()) {
      return false;
    }

    if (field.isFinal()) {
      logger.branch(TreeLogger.DEBUG, "Field '" + field.toString()
          + "' will not be serialized because it is final", null);
      return false;
    }

    return true;
  }

  private static Path createArrayComponentPath(final JArrayType arrayType,
      final Path parent) {
    return new Path() {
      public Path getParent() {
        return parent;
      }

      @Override
      public String toString() {
        return "Type '"
            + arrayType.getComponentType().getParameterizedQualifiedSourceName()
            + "' is reachable from array type '"
            + arrayType.getParameterizedQualifiedSourceName() + "'";
      }
    };
  }

  private static Path createFieldPath(final Path parent, final JField field) {
    return new Path() {
      public Path getParent() {
        return parent;
      }

      @Override
      public String toString() {
        JType type = field.getType();
        JClassType enclosingType = field.getEnclosingType();
        return "'" + type.getParameterizedQualifiedSourceName()
            + "' is reachable from field '" + field.getName() + "' of type '"
            + enclosingType.getParameterizedQualifiedSourceName() + "'";
      }
    };
  }

  private static Path createRootPath(final JType type) {
    return new Path() {
      public Path getParent() {
        return null;
      }

      @Override
      public String toString() {
        return "Started from '" + type.getParameterizedQualifiedSourceName()
            + "'";
      }
    };
  }

  private static Path createSubtypePath(final Path parent, final JType type,
      final JClassType supertype) {
    return new Path() {
      public Path getParent() {
        return parent;
      }

      @Override
      public String toString() {
        return "'" + type.getParameterizedQualifiedSourceName()
            + "' is reachable as a subtype of type '" + supertype + "'";
      }
    };
  }

  private static Path createTypeArgumentPath(final Path parent,
      final JClassType type, final int typeArgIndex, final JClassType typeArg) {
    return new Path() {
      public Path getParent() {
        return parent;
      }

      @Override
      public String toString() {
        return "'" + typeArg.getParameterizedQualifiedSourceName()
            + "' is reachable from type argument " + typeArgIndex
            + " of type '" + type.getParameterizedQualifiedSourceName() + "'";
      }
    };
  }

  private static boolean directlyImplementsMarkerInterface(JClassType type) {
    try {
      return TypeHierarchyUtils.directlyImplementsInterface(type,
          getIsSerializableMarkerInterface(type))
          || TypeHierarchyUtils.directlyImplementsInterface(type,
              getSerializableMarkerInterface(type));
    } catch (NotFoundException e) {
      return false;
    }
  }

  private static JArrayType getArrayType(TypeOracle typeOracle, int rank,
      JType component) {
    assert (rank > 0);

    JArrayType array = null;
    JType currentComponent = component;
    for (int i = 0; i < rank; ++i) {
      array = typeOracle.getArrayType(currentComponent);
      currentComponent = array;
    }

    return array;
  }

  private static JClassType getIsSerializableMarkerInterface(JClassType type)
      throws NotFoundException {
    return type.getOracle().getType(IsSerializable.class.getName());
  }

  private static Type getLogLevel(boolean isSpeculative) {
    return isSpeculative ? TreeLogger.WARN : TreeLogger.ERROR;
  }

  private static JClassType getSerializableMarkerInterface(JClassType type)
      throws NotFoundException {
    return type.getOracle().getType(Serializable.class.getName());
  }

  /**
   * Returns <code>true</code> if a serializer class could access this type.
   */
  private static boolean isAccessibleToSerializer(JClassType type) {
    if (type.isPrivate() || type.isLocalType()) {
      return false;
    }

    if (isInStandardJavaPackage(type.getQualifiedSourceName())) {
      if (!type.isPublic()) {
        return false;
      }
    }

    if (type.isMemberType()) {
      return isAccessibleToSerializer(type.getEnclosingType());
    }

    return true;
  }

  private static boolean isAllowedByFilter(TreeLogger logger,
      TypeFilter filter, JClassType classType, boolean isSpeculative) {
    if (!filter.isAllowed(classType)) {
      logger.log(getLogLevel(isSpeculative), "Excluded by type filter ");
      return false;
    }

    return true;
  }

  private static boolean isDeclaredSerializable(JClassType type) {
    return isAutoSerializable(type) || isManuallySerializable(type);
  }

  private static boolean isManuallySerializable(JClassType type) {
    return findCustomFieldSerializer(type.getOracle(), type) != null;
  }

  private static void logSerializableTypes(TreeLogger logger,
      Set<JClassType> fieldSerializableTypes) {
    TreeLogger localLogger = logger.branch(TreeLogger.DEBUG, "Identified "
        + fieldSerializableTypes.size() + " serializable type"
        + ((fieldSerializableTypes.size() == 1) ? "" : "s"), null);

    for (JClassType fieldSerializableType : fieldSerializableTypes) {
      localLogger.branch(TreeLogger.DEBUG,
          fieldSerializableType.getParameterizedQualifiedSourceName(), null);
    }
  }

  private boolean alreadyCheckedObject;

  /**
   * Cache of the {@link JClassType} for {@link Collection}.
   */
  private final JClassType collectionClass;

  private OutputStream logOutputStream;

  /**
   * Cache of the {@link JClassType} for {@link Map}.
   */
  private final JClassType mapClass;

  private final Map<JClassType, TreeLogger> rootTypes = new LinkedHashMap<JClassType, TreeLogger>();

  private TypeFilter typeFilter = DEFAULT_TYPE_FILTER;

  private final TypeOracle typeOracle;

  private final TypeParameterExposureComputer typeParameterExposureComputer = new TypeParameterExposureComputer(
      typeFilter);

  /**
   * The set of type parameters that appear in one of the root types.
   * TODO(spoon): It would be cleaner to delete this field, and instead to have
   * {@link #addRootType(TreeLogger, JType)} replace parameters with wildcard
   * types. Then the root types would not contain any parameters.
   */
  private Set<JTypeParameter> typeParametersInRootTypes = new HashSet<JTypeParameter>();

  /**
   * Map of {@link JType} to {@link TypeInfoComputed}.
   */
  private final Map<JType, TypeInfoComputed> typeToTypeInfoComputed = new HashMap<JType, TypeInfoComputed>();

  /**
   * Constructs a builder.
   * 
   * @param logger
   * @param typeOracle
   * 
   * @throws UnableToCompleteException if we fail to find one of our special
   *           types
   */
  public SerializableTypeOracleBuilder(TreeLogger logger, TypeOracle typeOracle)
      throws UnableToCompleteException {
    this.typeOracle = typeOracle;

    try {
      collectionClass = typeOracle.getType(Collection.class.getName());
      mapClass = typeOracle.getType(Map.class.getName());
    } catch (NotFoundException e) {
      logger.log(TreeLogger.ERROR, null, e);
      throw new UnableToCompleteException();
    }
  }

  public void addRootType(TreeLogger logger, JType type) {
    if (type.isPrimitive() != null) {
      return;
    }

    JClassType clazz = (JClassType) type;
    if (!rootTypes.containsKey(clazz)) {
      recordTypeParametersIn(type, typeParametersInRootTypes);

      rootTypes.put(clazz, logger);
    } else {
      logger.log(TreeLogger.TRACE, clazz.getParameterizedQualifiedSourceName()
          + " is already a root type.");
    }
  }

  /**
   * Builds a {@link SerializableTypeOracle} for a given set of root types.
   * 
   * @param logger
   * 
   * @return a {@link SerializableTypeOracle} for the specified set of root
   *         types
   * 
   * @throws UnableToCompleteException if there was not at least one
   *           instantiable type assignable to each of the specified root types
   */
  public SerializableTypeOracle build(TreeLogger logger)
      throws UnableToCompleteException {
    alreadyCheckedObject = false;

    boolean allSucceeded = true;
    for (Entry<JClassType, TreeLogger> entry : rootTypes.entrySet()) {
      allSucceeded &= checkTypeInstantiable(entry.getValue(), entry.getKey(),
          false, createRootPath(entry.getKey()));
    }

    if (!allSucceeded) {
      // the validation code has already logged why
      throw new UnableToCompleteException();
    }

    pruneUnreachableTypes();

    logReachableTypes(logger);

    Set<JClassType> possiblyInstantiatedTypes = new TreeSet<JClassType>(
        JTYPE_COMPARATOR);

    Set<JClassType> fieldSerializableTypes = new TreeSet<JClassType>(
        JTYPE_COMPARATOR);

    for (TypeInfoComputed tic : typeToTypeInfoComputed.values()) {
      JClassType type = tic.getType();

      type = type.getErasedType();

      if (tic.isInstantiable()) {
        assert (!type.isAbstract() || type.isEnum() != null);

        possiblyInstantiatedTypes.add(type);
      }

      if (tic.isFieldSerializable()) {
        assert (type.isInterface() == null);

        fieldSerializableTypes.add(type);
      }
    }

    logSerializableTypes(logger, fieldSerializableTypes);

    return new SerializableTypeOracleImpl(typeOracle, fieldSerializableTypes,
        possiblyInstantiatedTypes);
  }

  /**
   * Set the {@link OutputStream} which will receive a detailed log of the types
   * which were examined in order to determine serializability.
   */
  public void setLogOutputStream(OutputStream logOutputStream) {
    this.logOutputStream = logOutputStream;
  }

  public void setTypeFilter(TypeFilter typeFilter) {
    this.typeFilter = typeFilter;
    typeParameterExposureComputer.setTypeFilter(typeFilter);
  }

  /**
   * This method determines whether a type can be serialized by GWT. To do so,
   * it must traverse all subtypes as well as all field types of those types,
   * transitively.
   * 
   * It returns a boolean indicating whether this type or any of its subtypes
   * are instantiable.
   * 
   * As a side effect, all types needed--plus some--to serialize this type are
   * accumulated in {@link #typeToTypeInfoComputed}.
   * 
   * The method is exposed using default access to enable testing.
   */
  final boolean checkTypeInstantiable(TreeLogger logger, JType type,
      boolean isSpeculative, final Path path) {
    return checkTypeInstantiable(logger, type, isSpeculative, path, null);
  }

  /**
   * Same as
   * {@link #checkTypeInstantiable(TreeLogger, JType, boolean, com.google.gwt.user.rebind.rpc.SerializableTypeOracleBuilder.Path)},
   * except that returns the set of instantiable subtypes.
   */
  boolean checkTypeInstantiable(TreeLogger logger, JType type,
      boolean isSpeculative, final Path path, Set<JClassType> instSubtypes) {
    assert (type != null);
    if (type.isPrimitive() != null) {
      return true;
    }

    assert (type instanceof JClassType);

    JClassType classType = (JClassType) type;

    TreeLogger localLogger = logger.branch(TreeLogger.DEBUG,
        classType.getParameterizedQualifiedSourceName(), null);

    if (!isAllowedByFilter(localLogger, classType, isSpeculative)) {
      return false;
    }

    JTypeParameter isTypeParameter = classType.isTypeParameter();
    if (isTypeParameter != null) {
      if (typeParametersInRootTypes.contains(isTypeParameter)) {
        return checkTypeInstantiable(localLogger,
            isTypeParameter.getFirstBound(), isSpeculative, path);
      }

      /*
       * This type parameter was not in a root type and therefore it is the
       * caller's responsibility to deal with it. We assume that it is
       * instantiable here.
       */
      return true;
    }

    JWildcardType isWildcard = classType.isWildcard();
    if (isWildcard != null) {
      boolean success = true;
      for (JClassType bound : isWildcard.getUpperBounds()) {
        success &= checkTypeInstantiable(localLogger, bound, isSpeculative,
            path);
      }
      return success;
    }

    TypeInfoComputed tic = getTypeInfoComputed(classType, path);
    if (tic.isPendingInstantiable()) {
      // just early out and pretend we will succeed
      return true;
    } else if (tic.isDone()) {
      return tic.hasInstantiableSubtypes();
    }
    tic.setPendingInstantiable();

    JArrayType isArray = classType.isArray();
    if (isArray != null) {
      return checkArrayInstantiable(localLogger, tic, isSpeculative, path);
    }

    if (classType == typeOracle.getJavaLangObject()) {
      /*
       * Report an error if the type is or erases to Object since this violates
       * our restrictions on RPC.
       */
      localLogger.branch(
          getLogLevel(isSpeculative),
          "In order to produce smaller client-side code, 'Object' is not allowed; consider using a more specific type",
          null);
      tic.setInstantiable(false);
      return false;
    }

    if (classType.isRawType() != null) {
      TreeLogger rawTypeLogger = localLogger.branch(
          TreeLogger.DEBUG,
          "Type '"
              + classType.getQualifiedSourceName()
              + "' should be parameterized to help the compiler produce the smallest code size possible for your module",
          null);

      if (classType.isAssignableTo(collectionClass)
          || classType.isAssignableTo(mapClass)) {
        /*
         * Backwards compatibility. Raw collections or maps force all object
         * subtypes to be considered. Fall through to the normal class handling.
         */
        checkAllSubtypesOfObject(rawTypeLogger, path);
      }
    }

    JClassType originalType = (JClassType) type;

    JRealClassType baseType;
    if (type.isRawType() != null) {
      baseType = type.isRawType().getBaseType();
    } else if (type.isParameterized() != null) {
      baseType = type.isParameterized().getBaseType();
    } else {
      baseType = (JRealClassType) originalType;
    }

    if (isSpeculative && tic.isDirectlySerializable()) {
      isSpeculative = false;
    }

    JClassType[] typeArgs = NO_JCLASSES;
    JParameterizedType isParameterized = originalType.isParameterized();
    JGenericType baseAsGenericType = baseType.isGenericType();

    if (isParameterized != null) {
      typeArgs = isParameterized.getTypeArgs();
    } else if (baseAsGenericType != null) {
      List<JClassType> arguments = new ArrayList<JClassType>();
      for (JTypeParameter typeParameter : baseAsGenericType.getTypeParameters()) {
        arguments.add(typeParameter.getFirstBound());
      }
      typeArgs = arguments.toArray(NO_JCLASSES);
    }

    boolean parametersOkay = true;
    JRawType isRaw = originalType.isRawType();
    if (isParameterized != null || isRaw != null) {
      assert (baseAsGenericType != null);
      int numDeclaredParams = baseAsGenericType.getTypeParameters().length;
      if (numDeclaredParams == typeArgs.length) {
        for (int i = 0; i < numDeclaredParams; ++i) {
          JClassType typeArg = typeArgs[i];
          parametersOkay &= checkTypeArgument(localLogger, baseAsGenericType,
              i, typeArg, isSpeculative, path);
        }
      } else {
        /*
         * TODO: Does anyone actually depend on this odious behavior?
         * 
         * Backwards compatibility. The number of parameterization arguments
         * specified via gwt.typeArgs could exceed the number of formal
         * parameters declared on the generic type. Therefore have to explicitly
         * visit them here and they must all be instantiable.
         */
        for (int i = 0; i < numDeclaredParams; ++i) {
          JClassType typeArg = typeArgs[i];
          parametersOkay &= checkTypeInstantiable(localLogger, typeArg,
              isSpeculative, path);
        }
      }
    }

    // Check whether the type is field serializable
    // TODO: Should we really continue if parametersOkay is false?
    boolean isFieldSerializable = parametersOkay
        && shouldConsiderFieldsForSerialization(localLogger, baseType,
            isSpeculative)
        && checkFields(localLogger, baseType, isSpeculative, path);

    // TODO: Figure out logLevel for canBeInstantiated
    boolean isInstantiable = isFieldSerializable
        && canBeInstantiated(localLogger, baseType, TreeLogger.WARN);

    boolean anySubtypes = false;
    if (parametersOkay && baseType.getSubtypes().length > 0) {
      TreeLogger subtypesLogger = localLogger.branch(TreeLogger.DEBUG,
          "Analyzing subclasses:", null);
      anySubtypes = checkSubtypes(subtypesLogger, originalType, baseType,
          instSubtypes, typeArgs, path, baseType, false, isFieldSerializable);
    }

    anySubtypes |= isInstantiable;

    tic.setInstantiable(isInstantiable);
    tic.setInstantiableSubytpes(anySubtypes);

    if (!anySubtypes && !isSpeculative) {
      // No instantiable types were found
      localLogger.branch(getLogLevel(isSpeculative), "Type '"
          + classType.getParameterizedQualifiedSourceName()
          + "' was not serializable and has no concrete serializable subtypes",
          null);
    }

    return tic.hasInstantiableSubtypes();
  }

  int getTypeParameterExposure(JGenericType type, int index) {
    return getFlowInfo(type, index).getExposure();
  }

  /**
   * Returns <code>true</code> if the fields of the type should be considered
   * for serialization.
   * 
   * Default access to allow for testing.
   */
  boolean shouldConsiderFieldsForSerialization(TreeLogger logger,
      JClassType type, boolean isSpeculative) {
    return shouldConsiderFieldsForSerialization(logger, type, isSpeculative,
        typeFilter);
  }

  /**
   * Consider any subtype of java.lang.Object which qualifies for serialization.
   * 
   * @param logger
   */
  private void checkAllSubtypesOfObject(TreeLogger logger, Path parent) {
    if (alreadyCheckedObject) {
      return;
    }
    alreadyCheckedObject = true;

    /*
     * This will pull in the world and the set of serializable types will be
     * larger than it needs to be. We exclude types that do not qualify for
     * serialization to avoid generating false errors due to types that do not
     * qualify for serialization and have no serializable subtypes.
     */
    TreeLogger localLogger = logger.branch(TreeLogger.WARN,
        "Checking all subtypes of Object which qualify for serialization", null);
    JClassType[] allTypes = typeOracle.getJavaLangObject().getSubtypes();
    for (JClassType cls : allTypes) {
      if (getTypeInfoComputed(cls, parent).isDeclaredSerializable()) {
        checkTypeInstantiable(localLogger, cls, true, parent);
      }
    }
  }

  private boolean checkArrayInstantiable(TreeLogger localLogger,
      TypeInfoComputed tic, boolean isSpeculative, final Path path) {
    JArrayType isArray = tic.getType().isArray();
    assert (isArray != null);

    JType leafType = isArray.getLeafType();
    JClassType leafClass = leafType.isClassOrInterface();
    JTypeParameter isLeafTypeParameter = leafType.isTypeParameter();
    if (isLeafTypeParameter != null
        && !typeParametersInRootTypes.contains(isLeafTypeParameter)) {
      // Don't deal with non root type parameters
      tic.setInstantiable(false);
      tic.setInstantiableSubytpes(true);
      return true;
    }

    TreeLogger branch = localLogger.branch(TreeLogger.DEBUG,
        "Analyzing component type:", null);
    Set<JClassType> instantiableTypes = new HashSet<JClassType>();

    boolean succeeded = checkTypeInstantiable(branch,
        isArray.getComponentType(), isSpeculative, createArrayComponentPath(
            isArray, path), instantiableTypes);
    if (succeeded && leafClass != null) {
      /*
       * Compute covariant arrays for arrays of reference types.
       */
      for (JClassType instantiableType : TypeHierarchyUtils.getAllTypesBetweenRootTypeAndLeaves(
          leafClass, instantiableTypes)) {
        if (!isAccessibleToSerializer(instantiableType)) {
          // Skip types that are not accessible from a serializer
          continue;
        }

        for (int rank = 1; rank <= isArray.getRank(); ++rank) {
          JArrayType covariantArray = getArrayType(typeOracle, rank,
              instantiableType);

          // TODO: Check the choice of path
          TypeInfoComputed covariantArrayTic = getTypeInfoComputed(
              covariantArray, path);
          covariantArrayTic.setInstantiable(true);
        }
      }
    }

    tic.setInstantiable(succeeded);
    return succeeded;
  }

  /**
   * Returns <code>true</code> if the declared fields of this type are all
   * instantiable. As a side-effect it fills in {@link TypeInfoComputed} for all
   * necessary types.
   */
  private boolean checkDeclaredFields(TreeLogger logger,
      TypeInfoComputed typeInfo, boolean isSpeculative, Path parent) {

    JClassType classOrInterface = typeInfo.getType();
    if (classOrInterface.isEnum() != null) {
      // The fields of an enum are never serialized; they are always okay.
      return true;
    }

    // All fields on a manual serializable are considered speculative.
    isSpeculative |= typeInfo.isManuallySerializable();

    boolean allSucceeded = true;
    JField[] fields = classOrInterface.getFields();
    if (fields.length > 0) {
      TreeLogger localLogger = logger.branch(TreeLogger.DEBUG,
          "Analyzing the fields of type '"
              + classOrInterface.getParameterizedQualifiedSourceName()
              + "' that qualify for serialization", null);

      for (JField field : fields) {
        if (!shouldConsiderForSerialization(localLogger, field)) {
          continue;
        }

        TreeLogger fieldLogger = localLogger.branch(TreeLogger.DEBUG,
            field.toString(), null);
        JType fieldType = field.getType();

        Path path = createFieldPath(parent, field);
        if (typeInfo.isManuallySerializable()
            && fieldType.getLeafType() == typeOracle.getJavaLangObject()) {
          checkAllSubtypesOfObject(fieldLogger.branch(TreeLogger.WARN,
              "Object was reached from a manually serializable type", null),
              path);
        } else {
          allSucceeded &= checkTypeInstantiable(fieldLogger, fieldType,
              isSpeculative, path);
        }
      }
    }

    boolean succeeded = allSucceeded || typeInfo.isManuallySerializable();
    if (succeeded) {
      getTypeInfoComputed(classOrInterface, parent).setFieldSerializable();
    }

    return succeeded;
  }

  private boolean checkFields(TreeLogger logger, JClassType classOrInterface,
      boolean isSpeculative, Path parent) {
    TypeInfoComputed typeInfo = getTypeInfoComputed(classOrInterface, parent);
    if (classOrInterface.isEnum() != null) {
      // The fields of an enum are never serialized; they are always okay.
      return true;
    }

    // Check all super type fields first (recursively).
    JClassType superType = classOrInterface.getSuperclass();
    if (superType != null
        && getTypeInfoComputed(superType, parent).isDeclaredSerializable()) {
      boolean superTypeOk = checkFields(logger, superType, isSpeculative,
          parent);
      /*
       * If my super type did not check out, then I am not instantiable and we
       * should error out... UNLESS I am *directly* serializable myself, in
       * which case it's ok for me to be the root of a new instantiable
       * hierarchy.
       */
      if (!superTypeOk && !typeInfo.isDirectlySerializable()) {
        return false;
      }
    }

    return checkDeclaredFields(logger, typeInfo, isSpeculative, parent);
  }

  /**
   * Returns <code>true</code> if this type or one of its subtypes is
   * instantiable relative to a known base type.
   */
  private boolean checkSubtypes(TreeLogger localLogger,
      JClassType originalType, JRealClassType baseType,
      Set<JClassType> instSubtypes, JClassType[] typeArgs, final Path path,
      JClassType currentSubtype, boolean checkSuperFields,
      boolean superIsFieldSerializable) {
    assert (currentSubtype instanceof JRealClassType || currentSubtype.isGenericType() != null);

    Path subtypePath = createSubtypePath(path, currentSubtype, originalType);
    TypeInfoComputed typeInfo = getTypeInfoComputed(currentSubtype, subtypePath);

    TreeLogger subtypeLogger = localLogger.branch(TreeLogger.DEBUG,
        currentSubtype.getParameterizedQualifiedSourceName(), null);

    /*
     * If super does not qualify and I am not directly serializable then I can't
     * be serializable.
     */
    boolean fieldSerializable = shouldConsiderFieldsForSerialization(
        subtypeLogger, currentSubtype, true);
    if (!superIsFieldSerializable) {
      fieldSerializable &= typeInfo.isDirectlySerializable();
    }

    if (fieldSerializable) {
      if (checkSuperFields) {
        fieldSerializable &= checkFields(subtypeLogger, typeInfo.getType(),
            true, subtypePath);
      } else {
        fieldSerializable &= checkDeclaredFields(subtypeLogger, typeInfo, true,
            subtypePath);
      }
    }

    boolean anySubtypes = false;
    List<JClassType> immediateSubtypes = TypeHierarchyUtils.getImmediateSubtypes(currentSubtype);
    for (JClassType immediateSubtype : immediateSubtypes) {
      if (immediateSubtype.isRawType() != null) {
        immediateSubtype = immediateSubtype.isRawType().getBaseType();
      }

      checkSuperFields = currentSubtype.isInterface() != null
          && immediateSubtype.isClass() != null;
      anySubtypes |= checkSubtypes(localLogger, originalType, baseType,
          instSubtypes, typeArgs, path, immediateSubtype, checkSuperFields,
          fieldSerializable);
    }

    if (!fieldSerializable) {
      // If the subtype is not instantiable do not check the arguments.
      return anySubtypes;
    }

    if (currentSubtype == baseType) {
      return anySubtypes;
    }

    JGenericType genericSub = currentSubtype.isGenericType();
    if (genericSub != null) {
      TreeLogger paramsLogger = subtypeLogger.branch(TreeLogger.DEBUG,
          "Checking parameters of '"
              + genericSub.getParameterizedQualifiedSourceName() + "'");
      Map<JTypeParameter, Set<JTypeParameter>> subParamsConstrainedBy = subParamsConstrainedBy(
          baseType, genericSub);
      for (int i = 0; i < genericSub.getTypeParameters().length; i++) {
        JTypeParameter param = genericSub.getTypeParameters()[i];
        TreeLogger paramLogger = paramsLogger.branch(TreeLogger.DEBUG,
            "Checking param '" + param.getParameterizedQualifiedSourceName()
                + "'");
        Set<JTypeParameter> constBy = subParamsConstrainedBy.get(param);
        if (constBy == null) {
          fieldSerializable &= checkTypeArgument(paramLogger, genericSub, i,
              param.getFirstBound(), true, path);
        } else {
          boolean paramOK = false;
          for (JTypeParameter constrained : constBy) {
            paramOK |= checkTypeArgument(paramLogger, genericSub, i,
                typeArgs[constrained.getOrdinal()], true, path);
          }
          fieldSerializable &= paramOK;
        }
      }
    } else {
      JParameterizedType isParameterized = originalType.isParameterized();

      /*
       * The subtype is not generic so it must be a concrete parameterization of
       * the query type. If the query type is also a concrete parameterization
       * then we should be able to exclude the subtype based on an assignability
       * check. If the query type does contain type parameters then we assume
       * that the subtype should be included. In order to be certain, we would
       * need to perform a full unification between the query type and subtype.
       */
      if (isParameterized != null) {
        HashSet<JTypeParameter> typeParamsInQueryType = new HashSet<JTypeParameter>();
        recordTypeParametersIn(isParameterized, typeParamsInQueryType);

        if (typeParamsInQueryType.isEmpty()) {
          if (!isParameterized.isAssignableFrom(currentSubtype)) {
            subtypeLogger.log(TreeLogger.DEBUG, "Excluding type '"
                + currentSubtype.getParameterizedQualifiedSourceName()
                + "' because it is not assignable to '"
                + isParameterized.getParameterizedQualifiedSourceName() + "'");
            fieldSerializable = false;
          }
        }
      }
    }

    if (fieldSerializable
        && canBeInstantiated(subtypeLogger, currentSubtype, TreeLogger.DEBUG)) {
      if (instSubtypes != null) {
        instSubtypes.add(currentSubtype);
      }

      typeInfo.setInstantiable(true);
      anySubtypes = true;
    }

    return anySubtypes;
  }

  /**
   * Check the argument to a parameterized type to see if it will make the type
   * it is applied to be serializable. As a side effect, populates
   * {@link #typeToTypeInfoComputed} in the same way as
   * {@link #checkTypeInstantiable(TreeLogger, JType, boolean)}.
   * 
   * @param logger
   * @param baseType - The generic type the parameter is on
   * @param paramIndex - The index of the parameter in the generic type
   * @param typeArg - An upper bound on the actual argument being applied to the
   *          generic type
   * @param isSpeculative
   * 
   * @return Whether the a parameterized type can be serializable if
   *         <code>baseType</code> is the base type and the
   *         <code>paramIndex</code>th type argument is a subtype of
   *         <code>typeArg</code>.
   */
  private boolean checkTypeArgument(TreeLogger logger, JGenericType baseType,
      int paramIndex, JClassType typeArg, boolean isSpeculative, Path parent) {
    JArrayType typeArgAsArray = typeArg.isArray();
    if (typeArgAsArray != null) {
      JTypeParameter parameterOfTypeArgArray = typeArgAsArray.getLeafType().isTypeParameter();
      if (parameterOfTypeArgArray != null) {
        JGenericType declaringClass = parameterOfTypeArgArray.getDeclaringClass();
        if (declaringClass != null) {
          TypeParameterFlowInfo flowInfoForArrayParam = getFlowInfo(
              declaringClass, parameterOfTypeArgArray.getOrdinal());
          TypeParameterFlowInfo otherFlowInfo = getFlowInfo(baseType,
              paramIndex);
          if (otherFlowInfo.getExposure() >= 0
              && flowInfoForArrayParam.infiniteArrayExpansionPathBetween(otherFlowInfo)) {
            logger.branch(
                getLogLevel(isSpeculative),
                "Cannot serialize type '"
                    + baseType.getParameterizedQualifiedSourceName()
                    + "' when given an argument of type '"
                    + typeArg.getParameterizedQualifiedSourceName()
                    + "' because it appears to require serializing arrays of unbounded dimension");
            return false;
          }
        }
      }
    }

    Path path = createTypeArgumentPath(parent, baseType, paramIndex, typeArg);
    int exposure = getTypeParameterExposure(baseType, paramIndex);
    switch (exposure) {
      case TypeParameterExposureComputer.EXPOSURE_DIRECT: {
        TreeLogger branch = logger.branch(
            TreeLogger.DEBUG,
            "Checking type argument "
                + paramIndex
                + " of type '"
                + baseType.getParameterizedQualifiedSourceName()
                + "' because it is directly exposed in this type or in one of its subtypes");
        return checkTypeInstantiable(branch, typeArg, true, path)
            || mightNotBeExposed(baseType, paramIndex);
      }
      case TypeParameterExposureComputer.EXPOSURE_NONE:
        // Ignore this argument
        logger.branch(TreeLogger.DEBUG, "Ignoring type argument " + paramIndex
            + " of type '" + baseType.getParameterizedQualifiedSourceName()
            + "' because it is not exposed in this or any subtype");
        return true;

      default: {
        assert (exposure >= TypeParameterExposureComputer.EXPOSURE_MIN_BOUNDED_ARRAY);
        TreeLogger branch = logger.branch(
            TreeLogger.DEBUG,
            "Checking type argument "
                + paramIndex
                + " of type '"
                + baseType.getParameterizedQualifiedSourceName()
                + "' because it is exposed as an array with a maximum dimension of "
                + exposure + " in this type or one of its subtypes");
        return checkTypeInstantiable(branch, getArrayType(typeOracle, exposure,
            typeArg), true, path)
            || mightNotBeExposed(baseType, paramIndex);
      }
    }
  }

  private TypeParameterFlowInfo getFlowInfo(JGenericType type, int index) {
    return typeParameterExposureComputer.computeTypeParameterExposure(type,
        index);
  }

  private TypeInfoComputed getTypeInfoComputed(JClassType type, Path path) {
    TypeInfoComputed tic = typeToTypeInfoComputed.get(type);
    if (tic == null) {
      tic = new TypeInfoComputed(type, path);
      typeToTypeInfoComputed.put(type, tic);
    }
    return tic;
  }

  private boolean isAllowedByFilter(TreeLogger logger, JClassType classType,
      boolean isSpeculative) {
    return isAllowedByFilter(logger, typeFilter, classType, isSpeculative);
  }

  private void logPath(TreeLogger logger, Path path) {
    if (path == null) {
      return;
    }

    logger.log(TreeLogger.INFO, path.toString());
    logPath(logger, path.getParent());
  }

  private void logReachableTypes(TreeLogger logger) {
    PrintWriter printWriter = null;
    if (logOutputStream != null) {
      // Route the TreeLogger output to an output stream.
      printWriter = new PrintWriter(logOutputStream);
      PrintWriterTreeLogger printWriterTreeLogger = new PrintWriterTreeLogger(
          printWriter);
      printWriterTreeLogger.setMaxDetail(TreeLogger.ALL);
      logger = printWriterTreeLogger;
    }

    logger.log(TreeLogger.INFO, "Reachable types computed on: "
        + new Date().toString());
    Set<JType> keySet = typeToTypeInfoComputed.keySet();
    JType[] types = keySet.toArray(new JType[0]);
    Arrays.sort(types, JTYPE_COMPARATOR);

    for (JType type : types) {
      TypeInfoComputed tic = typeToTypeInfoComputed.get(type);
      assert (tic != null);

      TreeLogger typeLogger = logger.branch(TreeLogger.INFO,
          tic.getType().getParameterizedQualifiedSourceName());
      TreeLogger serializationStatus = typeLogger.branch(TreeLogger.INFO,
          "Serialization status");
      if (tic.isInstantiable()) {
        serializationStatus.branch(TreeLogger.INFO, "Instantiable");
      } else {
        if (tic.isFieldSerializable()) {
          serializationStatus.branch(TreeLogger.INFO, "Field serializable");
        } else {
          serializationStatus.branch(TreeLogger.INFO, "Not serializable");
        }
      }

      TreeLogger pathLogger = typeLogger.branch(TreeLogger.INFO, "Path");

      logPath(pathLogger, tic.getPath());
      logger.log(TreeLogger.INFO, "");
    }

    if (printWriter != null) {
      printWriter.flush();
    }
  }

  private boolean mightNotBeExposed(JGenericType baseType, int paramIndex) {
    TypeParameterFlowInfo flowInfo = getFlowInfo(baseType, paramIndex);
    return flowInfo.getMightNotBeExposed() || isManuallySerializable(baseType);
  }

  /**
   * Remove serializable types that were visited due to speculative paths but
   * are not really needed for serialization.
   * 
   * NOTE: This is currently much more limited than it should be. For example, a
   * path sensitive prune could remove instantiable types also.
   */
  private void pruneUnreachableTypes() {
    /*
     * Record all supertypes of any instantiable type, whether or not they are
     * field serialziable.
     */
    Set<JType> supersOfInstantiableTypes = new LinkedHashSet<JType>();
    for (TypeInfoComputed tic : typeToTypeInfoComputed.values()) {
      if (tic.isInstantiable()) {
        JClassType type = tic.getType().getErasedType();
        JClassType sup = type;
        while (sup != null) {
          supersOfInstantiableTypes.add(sup.getErasedType());
          sup = sup.getErasedType().getSuperclass();
        }
      }
    }

    /*
     * Record any field serializable type that is not in the supers of any
     * instantiable type.
     */
    Set<JType> toKill = new LinkedHashSet<JType>();
    for (TypeInfoComputed tic : typeToTypeInfoComputed.values()) {
      if (tic.isFieldSerializable()
          && !supersOfInstantiableTypes.contains(tic.getType().getErasedType())) {
        toKill.add(tic.getType());
      }
    }

    /*
     * Remove any field serializable supers that cannot be reached from an
     * instantiable type.
     */
    for (JType type : toKill) {
      typeToTypeInfoComputed.remove(type);
    }
  }

  /**
   * Returns a map from each parameter in the subtype to the set of parameters,
   * if any, in the supertype which constrain that parameter.
   */
  private Map<JTypeParameter, Set<JTypeParameter>> subParamsConstrainedBy(
      JClassType superclass, JGenericType subclass) {

    Map<JTypeParameter, Set<JTypeParameter>> newTypeParameters = new LinkedHashMap<JTypeParameter, Set<JTypeParameter>>();

    JGenericType isGenericSuper = superclass.isGenericType();
    if (isGenericSuper != null) {
      JParameterizedType parameterization = subclass.asParameterizationOf(isGenericSuper);
      JClassType[] paramArgs = parameterization.getTypeArgs();
      for (int i = 0; i < paramArgs.length; ++i) {
        Set<JTypeParameter> typeParamsInParamArg = new HashSet<JTypeParameter>();
        recordTypeParametersIn(paramArgs[i], typeParamsInParamArg);

        for (JTypeParameter arg : typeParamsInParamArg) {
          Set<JTypeParameter> constBy = newTypeParameters.get(arg);
          if (constBy == null) {
            constBy = new LinkedHashSet<JTypeParameter>();
            newTypeParameters.put(arg, constBy);
          }

          constBy.add(isGenericSuper.getTypeParameters()[i]);
        }
      }
    }

    return newTypeParameters;
  }
}
