/*
 * Copyright 2006 Google Inc.
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
package com.google.gwt.core.ext.typeinfo;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides type-related information about a set of source files, including doc
 * comment metadata.
 * <p>
 * All type objects exposed, such as
 * {@link com.google.gwt.core.ext.typeinfo.JClassType} and others, have a stable
 * identity relative to this type oracle instance. Consequently, you can
 * reliably compare object identity of any objects this type oracle produces.
 * For example, the following code relies on this stable identity guarantee:
 * 
 * <pre>
 * JClassType o = typeOracle.getJavaLangObject();
 * JClassType s1 = typeOracle.getType(&quot;java.lang.String&quot;);
 * JClassType s2 = typeOracle.getType(&quot;java.lang.String&quot;);
 * assert(s1 == s2);
 * assert(o == s1.getSuperclass());
 * JParameterizedType ls = typeOracle.parse(&quot;java.util.List&lt;java.lang.String&gt;&quot;);
 * assert(ls.getTypeArgs()[0] == s1);
 * </pre>
 * 
 * </p>
 */
public class TypeOracle {

  /**
   * A reserved metadata tag to indicates that a field type, method return type
   * or method parameter type is intended to be parameterized. Note that
   * constructor type parameters are not supported at present.
   */
  public static final String TAG_TYPEARGS = "gwt.typeArgs";

  static final int MOD_ABSTRACT = 0x00000001;
  static final int MOD_FINAL = 0x00000002;
  static final int MOD_NATIVE = 0x00000004;
  static final int MOD_PRIVATE = 0x00000008;
  static final int MOD_PROTECTED = 0x00000010;
  static final int MOD_PUBLIC = 0x00000020;
  static final int MOD_STATIC = 0x00000040;
  static final int MOD_TRANSIENT = 0x00000080;
  static final int MOD_VOLATILE = 0x00000100;

  static final JClassType[] NO_JCLASSES = new JClassType[0];
  static final JConstructor[] NO_JCTORS = new JConstructor[0];
  static final JField[] NO_JFIELDS = new JField[0];
  static final JMethod[] NO_JMETHODS = new JMethod[0];
  static final JPackage[] NO_JPACKAGES = new JPackage[0];
  static final JParameter[] NO_JPARAMS = new JParameter[0];
  static final JType[] NO_JTYPES = new JType[0];
  static final String[][] NO_STRING_ARR_ARR = new String[0][];
  static final String[] NO_STRINGS = new String[0];

  static String combine(String[] strings, int startIndex) {
    StringBuffer sb = new StringBuffer();
    for (int i = startIndex; i < strings.length; i++) {
      String s = strings[i];
      sb.append(s);
    }
    return sb.toString();
  }

  static String[] modifierBitsToNames(int bits) {
    List strings = new ArrayList();

    // The order is based on the order in which we want them to appear.
    //
    if (0 != (bits & MOD_PUBLIC)) {
      strings.add("public");
    }

    if (0 != (bits & MOD_PRIVATE)) {
      strings.add("private");
    }

    if (0 != (bits & MOD_PROTECTED)) {
      strings.add("protected");
    }

    if (0 != (bits & MOD_STATIC)) {
      strings.add("static");
    }

    if (0 != (bits & MOD_ABSTRACT)) {
      strings.add("abstract");
    }

    if (0 != (bits & MOD_FINAL)) {
      strings.add("final");
    }

    if (0 != (bits & MOD_NATIVE)) {
      strings.add("native");
    }

    if (0 != (bits & MOD_TRANSIENT)) {
      strings.add("transient");
    }

    if (0 != (bits & MOD_VOLATILE)) {
      strings.add("volatile");
    }

    return (String[]) strings.toArray(NO_STRINGS);
  }

  private final Map arrayTypes = new IdentityHashMap();

  private JClassType javaLangObject;

  private final Map packages = new HashMap();

  private final Map parameterizedTypes = new HashMap();

  private int reloadCount = 0;

  private final Map typesByCup = new IdentityHashMap();

  public TypeOracle() {
    // Always create the default package.
    //
    getOrCreatePackage("");
  }

  /**
   * Attempts to find a package by name. All requests for the same package
   * return the same package object.
   * 
   * @return <code>null</code> if the package could not be found
   */
  public JPackage findPackage(String pkgName) {
    return (JPackage) packages.get(pkgName);
  }

  /**
   * Finds a class or interface given its fully-qualified name. For nested
   * classes, use its source name rather than its binary name (that is, use a
   * "." rather than a "$").
   * 
   * @return <code>null</code> if the type is not found
   */
  public JClassType findType(String name) {
    // Try the dotted pieces, right to left.
    //
    int i = name.length() - 1;
    while (i >= 0) {
      int dot = name.lastIndexOf('.', i);
      String pkgName = "";
      String typeName = name;
      if (dot != -1) {
        pkgName = name.substring(0, dot);
        typeName = name.substring(dot + 1);
        i = dot - 1;
      } else {
        i = -1;
      }
      JClassType result = findType(pkgName, typeName);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  /**
   * Finds a type given its package-relative name. For nested classes, use its
   * source name rather than its binary name (that is, use a "." rather than a
   * "$").
   * 
   * @return <code>null</code> if the type is not found
   */
  public JClassType findType(String pkgName, String typeName) {
    JPackage pkg = findPackage(pkgName);
    if (pkg != null) {
      JClassType type = pkg.findType(typeName);
      if (type != null) {
        return type;
      }
    }
    return null;
  }

  /**
   * Gets the type object that represents an array of the specified type. The
   * returned type always has a stable identity so as to guarantee that all
   * calls to this method with the same argument return the same object.
   * 
   * @param componentType the component type of the array, which can itself be
   *          an array type
   * @return a type object representing an array of the component type
   */
  public JArrayType getArrayType(JType componentType) {
    JArrayType arrayType = (JArrayType) arrayTypes.get(componentType);
    if (arrayType == null) {
      arrayType = new JArrayType(componentType);
      arrayTypes.put(componentType, arrayType);
    }
    return arrayType;
  }

  /**
   * Gets a reference to the type object representing
   * <code>java.lang.Object</code>.
   */
  public JClassType getJavaLangObject() {
    return javaLangObject;
  }

  /**
   * Ensure that a package with the specified name exists as well as its parent
   * packages.
   */
  public JPackage getOrCreatePackage(String name) {
    int i = name.lastIndexOf('.');
    if (i != -1) {
      // Ensure the parent package is also created.
      //
      getOrCreatePackage(name.substring(0, i));
    }

    JPackage pkg = (JPackage) packages.get(name);
    if (pkg == null) {
      pkg = new JPackage(name);
      packages.put(name, pkg);
    }
    return pkg;
  }

  /**
   * Gets a package by name. All requests for the same package return the same
   * package object.
   * 
   * @return the package object associated with the specified name
   */
  public JPackage getPackage(String pkgName) throws NotFoundException {
    JPackage result = findPackage(pkgName);
    if (result == null) {
      throw new NotFoundException(pkgName);
    }
    return result;
  }

  /**
   * Gets an array of all packages known to this type oracle.
   * 
   * @return an array of packages, possibly of zero-length
   */
  public JPackage[] getPackages() {
    return (JPackage[]) packages.values().toArray(NO_JPACKAGES);
  }

  /**
   * Gets the parameterized type object that represents the combination of a
   * specified raw type and a set of type arguments. The returned type always
   * has a stable identity so as to guarantee that all calls to this method with
   * the same arguments return the same object.
   * 
   * @param rawType the raw type of the array, which must be a class or
   *          interface type and cannot be a primitive, array, or another
   *          parameterized type
   * @param typeArgs the type arguments bound to the specified raw type
   * @return a type object representing this particular binding of type
   *         arguments to the specified raw type
   */
  public JType getParameterizedType(JClassType rawType, JType[] typeArgs) {
    // Uses the generated string signature to intern parameterized types.
    //
    JParameterizedType parameterized = new JParameterizedType(rawType);
    for (int i = 0; i < typeArgs.length; i++) {
      parameterized.addTypeArg(typeArgs[i]);
    }
    String sig = parameterized.getQualifiedSourceName();
    JParameterizedType existing = (JParameterizedType) parameterizedTypes.get(sig);
    if (existing == null) {
      parameterizedTypes.put(sig, parameterized);
      existing = parameterized;
    }
    return existing;
  }

  public long getReloadCount() {
    return reloadCount;
  }

  /**
   * Finds a type given its fully qualified name. For nested classes, use its
   * source name rather than its binary name (that is, use a "." rather than a
   * "$").
   * 
   * @return the specified type
   */
  public JClassType getType(String name) throws NotFoundException {
    JClassType type = findType(name);
    if (type == null) {
      throw new NotFoundException(name);
    }
    return type;
  }

  /**
   * Finds a type given its package-relative name. For nested classes, use its
   * source name rather than its binary name (that is, use a "." rather than a
   * "$").
   * 
   * @return the specified type
   */
  public JClassType getType(String pkgName, String topLevelTypeSimpleName)
      throws NotFoundException {
    JClassType type = findType(pkgName, topLevelTypeSimpleName);
    if (type == null) {
      throw new NotFoundException(pkgName + "." + topLevelTypeSimpleName);
    }
    return type;
  }

  /**
   * Gets all types, both top-level and nested.
   * 
   * @return an array of types, possibly of zero length
   */
  public JClassType[] getTypes() {
    Set allTypes = new HashSet();
    JPackage[] pkgs = getPackages();
    for (int i = 0; i < pkgs.length; i++) {
      JPackage pkg = pkgs[i];
      JClassType[] types = pkg.getTypes();
      for (int j = 0; j < types.length; j++) {
        JClassType type = types[j];
        buildAllTypesImpl(allTypes, type);
      }
    }
    return (JClassType[]) allTypes.toArray(NO_JCLASSES);
  }

  public JClassType[] getTypesInCompilationUnit(CompilationUnitProvider cup) {
    JClassType[] types = (JClassType[]) typesByCup.get(cup);
    if (types != null) {
      return types;
    } else {
      return NO_JCLASSES;
    }
  }

  /**
   * Parses the string form of a type to produce the corresponding type object.
   * The types that can be parsed include primitives, class and interface names,
   * simple parameterized types (those without wildcards or bounds), and arrays
   * of the preceding.
   * <p>
   * Examples of types that can be parsed by this method.
   * <ul>
   * <li><code>int</code></li>
   * <li><code>java.lang.Object</code></li>
   * <li><code>java.lang.String[]</code></li>
   * <li><code>char[][]</code></li>
   * <li><code>void</code></li>
   * <li><code>List&lt;Shape&gt;</code></li>
   * <li><code>List&lt;List&lt;Shape&gt;&gt;</code></li>
   * </ul>
   * </p>
   * 
   * @param type a type signature to be parsed
   * @return the type object corresponding to the parse type
   */
  public JType parse(String type) throws TypeOracleException {
    // Remove all internal and external whitespace.
    //
    type = type.replaceAll("\\\\s", "");

    // Recursively parse.
    //
    return parseImpl(type);
  }

  /**
   * Convenience method to sort class types in a consistent way. Note that the
   * order is subject to change and is intended to generate an "aesthetically
   * pleasing" order rather than a computationally reliable order.
   */
  public void sort(JClassType[] types) {
    Arrays.sort(types, new Comparator() {
      public int compare(Object type1, Object type2) {
        String name1 = ((JClassType) type1).getQualifiedSourceName();
        String name2 = ((JClassType) type2).getQualifiedSourceName();
        return name1.compareTo(name2);
      }
    });
  }

  /**
   * Convenience method to sort constructors in a consistent way. Note that the
   * order is subject to change and is intended to generate an "aesthetically
   * pleasing" order rather than a computationally reliable order.
   */
  public void sort(JConstructor[] ctors) {
    Arrays.sort(ctors, new Comparator() {
      public int compare(Object o1, Object o2) {
        // Nothing for now; could enhance to sort based on parameter list
        return 0;
      }
    });
  }

  /**
   * Convenience method to sort fields in a consistent way. Note that the order
   * is subject to change and is intended to generate an "aesthetically
   * pleasing" order rather than a computationally reliable order.
   */
  public void sort(JField[] fields) {
    Arrays.sort(fields, new Comparator() {
      public int compare(Object o1, Object o2) {
        final JField f1 = ((JField) o1);
        final JField f2 = ((JField) o2);
        String name1 = f1.getName();
        String name2 = f2.getName();
        return name1.compareTo(name2);
      }
    });
  }

  /**
   * Convenience method to sort methods in a consistent way. Note that the order
   * is subject to change and is intended to generate an "aesthetically
   * pleasing" order rather than a computationally reliable order.
   */
  public void sort(JMethod[] methods) {
    Arrays.sort(methods, new Comparator() {
      public int compare(Object o1, Object o2) {
        final JMethod m1 = ((JMethod) o1);
        final JMethod m2 = ((JMethod) o2);
        String name1 = m1.getName();
        String name2 = m2.getName();
        return name1.compareTo(name2);
      }
    });
  }

  void incrementReloadCount() {
    reloadCount++;
  }

  Set invalidateTypesInCompilationUnit(CompilationUnitProvider cup) {
    Set invalidTypes = new HashSet();
    JClassType[] types = (JClassType[]) typesByCup.get(cup);
    if (types == null) {
      return invalidTypes;
    }
    for (int i = 0; i < types.length; i++) {
      JPackage jp = types[i].getPackage();
      invalidTypes.add(types[i].getQualifiedSourceName());
      jp.remove(types[i]);
    }
    typesByCup.remove(cup);
    return invalidTypes;
  }

  void recordTypeInCompilationUnit(CompilationUnitProvider cup, JClassType type) {
    JClassType[] types = (JClassType[]) typesByCup.get(cup);
    if (types == null) {
      types = new JClassType[] {type};
    } else {
      JClassType[] temp = new JClassType[types.length + 1];
      System.arraycopy(types, 0, temp, 0, types.length);
      temp[types.length] = type;
      types = temp;
    }
    typesByCup.put(cup, types);
  }

  /**
   * Updates relationships within this type oracle. Should be called after any
   * changes are made.
   * 
   * <p>
   * Throws <code>TypeOracleException</code> thrown if fundamental baseline
   * correctness criteria are violated, most notably the absence of
   * "java.lang.Object"
   * </p>
   */
  void refresh(TreeLogger logger) throws NotFoundException {
    if (javaLangObject == null) {
      javaLangObject = findType("java.lang.Object");
      if (javaLangObject == null) {
        throw new NotFoundException("java.lang.Object");
      }
    }
    computeHierarchyRelationships();
    consumeTypeArgMetaData(logger);
  }

  private void buildAllTypesImpl(Set allTypes, JClassType type) {
    boolean didAdd = allTypes.add(type);
    assert (didAdd);
    JClassType[] nestedTypes = type.getNestedTypes();
    for (int i = 0; i < nestedTypes.length; i++) {
      JClassType nestedType = nestedTypes[i];
      buildAllTypesImpl(allTypes, nestedType);
    }
  }

  private void computeHierarchyRelationships() {
    // For each type, walk up its hierarchy chain and tell each supertype
    // about its subtype.
    //
    JClassType[] allTypes = getTypes();
    for (int i = 0; i < allTypes.length; i++) {
      JClassType type = allTypes[i];
      type.notifySuperTypes();
    }
  }

  private void consumeTypeArgMetaData(TreeLogger logger) {
    logger = logger.branch(TreeLogger.DEBUG, "Examining " + TAG_TYPEARGS
        + " tags", null);
    consumeTypeArgMetaData(logger, getTypes());
  }

  private void consumeTypeArgMetaData(TreeLogger logger, JClassType[] types) {
    for (int i = 0; i < types.length; i++) {
      JClassType type = types[i];
      // CTORS not supported yet

      TreeLogger branch = logger.branch(TreeLogger.DEBUG, "Type "
          + type.getQualifiedSourceName(), null);

      consumeTypeArgMetaData(branch, type.getMethods());
      consumeTypeArgMetaData(branch, type.getFields());
    }
  }

  private void consumeTypeArgMetaData(TreeLogger logger, JField[] fields) {
    TreeLogger branch;
    for (int i = 0; i < fields.length; i++) {
      JField field = fields[i];

      String[][] tokensArray = field.getMetaData(TAG_TYPEARGS);
      if (tokensArray.length == 0) {
        // No tag.
        continue;
      }

      try {
        String msg = "Field " + field.getName();
        branch = logger.branch(TreeLogger.TRACE, msg, null);

        if (tokensArray.length > 1) {
          // Too many.
          branch.log(TreeLogger.WARN, "Metadata error on field '"
              + field.getName() + "' in type '" + field.getEnclosingType()
              + "': expecting at most one " + TAG_TYPEARGS
              + " (the last one will be used)", null);
        }

        // (1) Parse it.
        // (2) Update the field's type.
        // If it wasn't a valid parameterized type, parse() would've thrown.
        //
        JType fieldType = field.getType();
        String[] token = tokensArray[tokensArray.length - 1];
        JType resultingType = determineActualType(branch, fieldType, token, 0);
        field.setType(resultingType);
      } catch (UnableToCompleteException e) {
        // Continue; the problem will have been logged.
        //
      }
    }
  }

  private void consumeTypeArgMetaData(TreeLogger logger, JMethod[] methods) {
    TreeLogger branch;
    for (int i = 0; i < methods.length; i++) {
      JMethod method = methods[i];

      String[][] tokensArray = method.getMetaData(TAG_TYPEARGS);
      if (tokensArray.length == 0) {
        // No tag.
        continue;
      }
      try {
        String msg = "Method " + method.getReadableDeclaration();
        branch = logger.branch(TreeLogger.TRACE, msg, null);

        // Okay, parse each one and correlate it to a part of the decl.
        //
        boolean returnTypeHandled = false;
        Set paramsAlreadySet = new HashSet();
        for (int j = 0; j < tokensArray.length; j++) {
          String[] tokens = tokensArray[j];
          // It is either referring to the return type or a parameter type.
          //
          if (tokens.length == 0) {
            // Expecting at least something.
            //
            branch.log(TreeLogger.WARN,
                "Metadata error: expecting tokens after " + TAG_TYPEARGS, null);
            throw new UnableToCompleteException();
          }

          // See if the first token is a parameter name.
          //
          JParameter param = method.findParameter(tokens[0]);
          if (param != null) {
            if (!paramsAlreadySet.contains(param)) {
              // These are type args for a param.
              //
              JType resultingType = determineActualType(branch,
                  param.getType(), tokens, 1);
              param.setType(resultingType);
              paramsAlreadySet.add(param);
            } else {
              // This parameter type has already been set.
              //
              msg = "Metadata error: duplicate attempt to specify type args for parameter '"
                  + param.getName() + "'";
              branch.log(TreeLogger.WARN, msg, null);
              throw new UnableToCompleteException();
            }
          } else {
            // It's either referring to the return type or a bad param name.
            //
            if (!returnTypeHandled) {
              JType resultingType = determineActualType(branch,
                  method.getReturnType(), tokens, 0);
              method.setReturnType(resultingType);
              returnTypeHandled = true;
            } else {
              // The return type has already been set.
              //
              msg = "Metadata error: duplicate attempt to specify type args for the return type";
              branch.log(TreeLogger.WARN, msg, null);
            }
          }
        }
      } catch (UnableToCompleteException e) {
        // Continue; will already have been logged.
        //
      }
    }
  }

  /*
   * Given a declared type and some number of type arguments determine what the
   * actual type should be.
   */
  private JType determineActualType(TreeLogger logger, JType declType,
      String[] tokens, int startIndex) throws UnableToCompleteException {
    // These are type args for a param.
    //
    JType leafType = declType.getLeafType();
    String typeName = leafType.getQualifiedSourceName();
    JType resultingType = parseTypeArgTokens(logger, typeName, tokens,
        startIndex);
    JArrayType arrayType = declType.isArray();
    if (arrayType != null) {
      arrayType.setLeafType(resultingType);

      return declType;
    }

    return resultingType;
  }

  private JType parseImpl(String type) throws NotFoundException,
      ParseException, BadTypeArgsException {
    if (type.endsWith("[]")) {
      String remainder = type.substring(0, type.length() - 2);
      JType componentType = parseImpl(remainder);
      return getArrayType(componentType);
    }

    if (type.endsWith(">")) {
      int bracket = type.indexOf('<');
      if (bracket == -1) {
        throw new ParseException(
            "Mismatched brackets; expected '<' to match subsequent '>'");
      }

      // Resolve the raw type.
      //
      String rawTypeName = type.substring(0, bracket);
      JType rawType = parseImpl(rawTypeName);
      if (rawType.isParameterized() != null) {
        // The raw type cannot itself be parmeterized.
        //
        throw new BadTypeArgsException(
            "Only non-parameterized classes and interface can be parameterized");
      } else if (rawType.isClassOrInterface() == null) {
        // The raw type must be a class or interface
        // (not an array or primitive).
        //
        throw new BadTypeArgsException(
            "Only classes and interface can be parameterized, so "
                + rawType.getQualifiedSourceName()
                + " cannot be used in this context");
      }

      // Resolve each type argument.
      //
      String typeArgGuts = type.substring(bracket + 1, type.length() - 1);
      String[] typeArgNames = typeArgGuts.split(",");
      JType[] typeArgs = new JType[typeArgNames.length];
      for (int i = 0; i < typeArgNames.length; i++) {
        typeArgs[i] = parseImpl(typeArgNames[i]);
        if (typeArgs[i].isPrimitive() != null) {
          // Cannot be primitive.
          //
          throw new BadTypeArgsException(
              "Type arguments cannot be primitive, so "
                  + typeArgs[i].getQualifiedSourceName()
                  + " cannot be used in this context");
        }
      }

      // Intern this type.
      //
      return getParameterizedType(rawType.isClassOrInterface(), typeArgs);
    }

    JType result = JPrimitiveType.valueOf(type);
    if (result != null) {
      return result;
    }

    result = findType(type);
    if (result != null) {
      return result;
    }

    throw new NotFoundException(type);
  }

  private JType parseTypeArgTokens(TreeLogger logger, String maybeRawType,
      String[] tokens, int startIndex) throws UnableToCompleteException {
    String munged = combine(tokens, startIndex).trim();
    String toParse = maybeRawType + munged;
    JType parameterizedType;
    try {
      parameterizedType = parse(toParse);
    } catch (TypeOracleException e) {
      String msg = "Unable to recognize '" + toParse
          + "' as a type name (is it fully qualified?)";
      logger.log(TreeLogger.WARN, msg, null);
      throw new UnableToCompleteException();
    }
    return parameterizedType;
  }
}
