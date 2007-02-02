package com.google.gwt.core.ext.typeinfo;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.dev.jdt.StaticCompilationUnitProvider;
import com.google.gwt.dev.jdt.TypeOracleBuilder;
import com.google.gwt.dev.jdt.URLCompilationUnitProvider;

import junit.framework.TestCase;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class JClassTypeTest extends TestCase {

  public void testGetAllMethods() throws UnableToCompleteException,
      TypeOracleException {
    TreeLogger logger = TreeLogger.NULL;
    TypeOracle typeOracle = buildOracleFromTestPackage(logger);

    String[] noParams = new String[0];
    String[] intObjectParams = new String[] {"int", "java.lang.Object"};

    // Verify IA.
    {
      JClassType type = typeOracle.getType("com.google.gwt.core.ext.typeinfo.test.IA");
      JMethod[] leafMethods = type.getAllMethods();
      assertEquals(3, leafMethods.length);

      assertLeafMethodFoundIn(typeOracle,
          "com.google.gwt.core.ext.typeinfo.test.IA",
          "com.google.gwt.core.ext.typeinfo.test.IA", "ia", noParams);

      assertLeafMethodFoundIn(typeOracle,
          "com.google.gwt.core.ext.typeinfo.test.IA",
          "com.google.gwt.core.ext.typeinfo.test.IA", "ia", intObjectParams);

      assertLeafMethodFoundIn(typeOracle,
          "com.google.gwt.core.ext.typeinfo.test.IA",
          "com.google.gwt.core.ext.typeinfo.test.IA", "foo", noParams);
    }

    // Verify IB.
    {
      JClassType type = typeOracle.getType("com.google.gwt.core.ext.typeinfo.test.IB");
      JMethod[] leafMethods = type.getAllMethods();
      assertEquals(5, leafMethods.length);

      assertLeafMethodFoundIn(typeOracle,
          "com.google.gwt.core.ext.typeinfo.test.IA",
          "com.google.gwt.core.ext.typeinfo.test.IB", "ia", noParams);

      assertLeafMethodFoundIn(typeOracle,
          "com.google.gwt.core.ext.typeinfo.test.IA",
          "com.google.gwt.core.ext.typeinfo.test.IB", "ia", intObjectParams);

      assertLeafMethodFoundIn(typeOracle,
          "com.google.gwt.core.ext.typeinfo.test.IB",
          "com.google.gwt.core.ext.typeinfo.test.IB", "ib", noParams);

      assertLeafMethodFoundIn(typeOracle,
          "com.google.gwt.core.ext.typeinfo.test.IB",
          "com.google.gwt.core.ext.typeinfo.test.IB", "ib", intObjectParams);

      assertLeafMethodFoundIn(typeOracle,
          "com.google.gwt.core.ext.typeinfo.test.IB",
          "com.google.gwt.core.ext.typeinfo.test.IB", "foo", noParams);
    }

    // Verify IC.
    {
      JClassType type = typeOracle.getType("com.google.gwt.core.ext.typeinfo.test.IC");
      JMethod[] leafMethods = type.getAllMethods();
      assertEquals(7, leafMethods.length);

      assertLeafMethodFoundIn(typeOracle,
          "com.google.gwt.core.ext.typeinfo.test.IA",
          "com.google.gwt.core.ext.typeinfo.test.IC", "ia", noParams);

      assertLeafMethodFoundIn(typeOracle,
          "com.google.gwt.core.ext.typeinfo.test.IA",
          "com.google.gwt.core.ext.typeinfo.test.IC", "ia", intObjectParams);

      assertLeafMethodFoundIn(typeOracle,
          "com.google.gwt.core.ext.typeinfo.test.IC",
          "com.google.gwt.core.ext.typeinfo.test.IC", "ib", noParams);

      assertLeafMethodFoundIn(typeOracle,
          "com.google.gwt.core.ext.typeinfo.test.IC",
          "com.google.gwt.core.ext.typeinfo.test.IC", "ib", intObjectParams);

      assertLeafMethodFoundIn(typeOracle,
          "com.google.gwt.core.ext.typeinfo.test.IC",
          "com.google.gwt.core.ext.typeinfo.test.IC", "ic", noParams);

      assertLeafMethodFoundIn(typeOracle,
          "com.google.gwt.core.ext.typeinfo.test.IC",
          "com.google.gwt.core.ext.typeinfo.test.IC", "ic", intObjectParams);

      assertLeafMethodFoundIn(typeOracle,
          "com.google.gwt.core.ext.typeinfo.test.IB",
          "com.google.gwt.core.ext.typeinfo.test.IC", "foo", noParams);
    }

    // Both overloads of ia are only declared in IA, so all searches should find
    // them there.
    {
      assertLeafMethodFoundIn(typeOracle,
          "com.google.gwt.core.ext.typeinfo.test.IA",
          "com.google.gwt.core.ext.typeinfo.test.CA", "ia", noParams);

      assertLeafMethodFoundIn(typeOracle,
          "com.google.gwt.core.ext.typeinfo.test.IA",
          "com.google.gwt.core.ext.typeinfo.test.CB", "ia", noParams);

      assertLeafMethodFoundIn(typeOracle,
          "com.google.gwt.core.ext.typeinfo.test.IA",
          "com.google.gwt.core.ext.typeinfo.test.CC", "ia", noParams);

      assertLeafMethodFoundIn(typeOracle,
          "com.google.gwt.core.ext.typeinfo.test.IA",
          "com.google.gwt.core.ext.typeinfo.test.CA", "ia", intObjectParams);

      assertLeafMethodFoundIn(typeOracle,
          "com.google.gwt.core.ext.typeinfo.test.IA",
          "com.google.gwt.core.ext.typeinfo.test.CB", "ia", intObjectParams);

      assertLeafMethodFoundIn(typeOracle,
          "com.google.gwt.core.ext.typeinfo.test.IA",
          "com.google.gwt.core.ext.typeinfo.test.CC", "ia", intObjectParams);
    }

    // Both overloads of ib are declared in both IB and IC, so
    // - searching for ib in CB will return IB
    // - searching for ib in CC will return IC
    {
      assertLeafMethodFoundIn(typeOracle,
          "com.google.gwt.core.ext.typeinfo.test.IB",
          "com.google.gwt.core.ext.typeinfo.test.CB", "ib", noParams);

      assertLeafMethodFoundIn(typeOracle,
          "com.google.gwt.core.ext.typeinfo.test.IC",
          "com.google.gwt.core.ext.typeinfo.test.CC", "ib", noParams);

      assertLeafMethodFoundIn(typeOracle,
          "com.google.gwt.core.ext.typeinfo.test.IB",
          "com.google.gwt.core.ext.typeinfo.test.CB", "ib", intObjectParams);

      assertLeafMethodFoundIn(typeOracle,
          "com.google.gwt.core.ext.typeinfo.test.IC",
          "com.google.gwt.core.ext.typeinfo.test.CC", "ib", intObjectParams);
    }

    // Both overloads of ic are declared only in IC, but ic() is also declared
    // in CB, so
    // - searching for ic() in CB will return CB
    // - searching for ic() in CC will return CB
    // - searching for ic(int, Object) in CC will return IC
    {
      assertLeafMethodFoundIn(typeOracle,
          "com.google.gwt.core.ext.typeinfo.test.CB",
          "com.google.gwt.core.ext.typeinfo.test.CB", "ic", noParams);

      assertLeafMethodFoundIn(typeOracle,
          "com.google.gwt.core.ext.typeinfo.test.CB",
          "com.google.gwt.core.ext.typeinfo.test.CC", "ic", noParams);

      assertLeafMethodFoundIn(typeOracle,
          "com.google.gwt.core.ext.typeinfo.test.IC",
          "com.google.gwt.core.ext.typeinfo.test.CC", "ic", intObjectParams);
    }

    // Both IA and IB define foo(), and searching for foo() on IC should return
    // IB.foo(). This matters because IC also extends IA, so a naive algorithm
    // for getLeafMethods() might prefer IA.foo() to IB.foo().
    {
      assertLeafMethodFoundIn(typeOracle,
          "com.google.gwt.core.ext.typeinfo.test.IB",
          "com.google.gwt.core.ext.typeinfo.test.IC", "foo", noParams);
    }
  }

  private void addCompilationUnitsInPath(TypeOracleBuilder builder,
      File sourcePathEntry, String pkgName) throws UnableToCompleteException,
      MalformedURLException {
    File pkgPath = new File(sourcePathEntry, pkgName.replace('.', '/'));
    File[] files = pkgPath.listFiles();
    for (int i = 0; i < files.length; i++) {
      File file = files[i];
      if (file.isFile()) {
        // If it's a source file, slurp it in.
        if (file.getName().endsWith(".java")) {
          URL location = file.toURL();
          CompilationUnitProvider cup = new URLCompilationUnitProvider(
              location, pkgName);
          builder.addCompilationUnit(cup);
        }
      } else {
        // Recurse into subpackages.
        addCompilationUnitsInPath(builder, sourcePathEntry, pkgName
            + file.getName());
      }
    }
  }

  private void assertLeafMethodFoundIn(TypeOracle oracle,
      String expectedTypeName, String searchTypeName, String methodName,
      String[] paramTypeNames) throws TypeOracleException {
    JClassType expectedType = oracle.getType(expectedTypeName);
    JClassType searchType = oracle.getType(searchTypeName);
    JType[] paramTypes = new JType[paramTypeNames.length];
    for (int i = 0; i < paramTypeNames.length; i++) {
      String paramTypeName = paramTypeNames[i];
      paramTypes[i] = oracle.parse(paramTypeName);
    }

    JMethod[] leafMethods = searchType.getAllMethods();
    for (int i = 0; i < leafMethods.length; i++) {
      JMethod method = leafMethods[i];
      if (method.getName().equals(methodName)) {
        if (method.hasParamTypes(paramTypes)) {
          String typeName = method.getEnclosingType().getQualifiedSourceName();
          assertEquals(expectedTypeName, typeName);
          return;
        }
      }
    }
    fail("Did not find expected method at all");
  }

  /**
   * Looks in the package containing this class and uses it as an anchor for
   * including all the classes under the "test" subpackage. TODO: This is not
   * generalized yet, but it could be made reusable and put into
   * TypeOracleBuilder.
   * 
   * @return
   * @throws URISyntaxException
   * @throws UnableToCompleteException
   * @throws MalformedURLException
   */
  private TypeOracle buildOracleFromTestPackage(TreeLogger logger)
      throws UnableToCompleteException {
    Throwable caught;
    try {
      // Find the source path using this class as an anchor.
      String className = getClass().getName();
      String resName = className.replace('.', '/') + ".java";
      URL location = getClass().getClassLoader().getResource(resName);
      assertNotNull("Ensure that source is in classpath for: " + resName,
          location);
      String absPath = new File(new URI(location.toString())).getAbsolutePath();
      int sourcePathEntryLen = absPath.length() - resName.length();
      File sourcePathEntry = new File(absPath.substring(0, sourcePathEntryLen));

      // Determine the starting package name.
      int lastDot = className.lastIndexOf('.');
      String pkgName = (lastDot < 0 ? "test" : className.substring(0, lastDot)
          + ".test");

      // Create the builder to be filled in.
      TypeOracleBuilder builder = new TypeOracleBuilder();

      // Add java.lang.Object.
      builder.addCompilationUnit(new StaticCompilationUnitProvider("java.lang",
          "Object", "package java.lang; public class Object { }".toCharArray()));

      // Recursively walk the directories.
      addCompilationUnitsInPath(builder, sourcePathEntry, pkgName);
      return builder.build(logger);
    } catch (URISyntaxException e) {
      caught = e;
    } catch (MalformedURLException e) {
      caught = e;
    }
    logger.log(TreeLogger.ERROR, "Failed to build type oracle", caught);
    throw new UnableToCompleteException();
  }
}
