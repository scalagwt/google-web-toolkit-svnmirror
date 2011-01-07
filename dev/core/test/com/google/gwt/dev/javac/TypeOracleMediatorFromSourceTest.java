
package com.google.gwt.dev.javac;

import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.core.ext.typeinfo.TypeOracleException;
import java.io.IOException;

public class TypeOracleMediatorFromSourceTest extends
    TypeOracleMediatorTestBase {

  protected CheckedJavaResource CU_HasSyntaxErrors = new CheckedJavaResource(
      "test", "HasSyntaxErrors", "NoSyntaxErrors") {
    @Override
    public void check(JClassType classInfo) {
      fail("This class should have been removed");
    }

    @Override
    public String getSource() {
      StringBuffer sb = new StringBuffer();
      sb.append("package test;\n");
      sb.append("class NoSyntaxErrors { }\n");
      sb.append("public class HasSyntaxErrors { a syntax error }\n");
      return sb.toString();
    }
  };

  protected CheckedJavaResource CU_HasUnresolvedSymbols = new CheckedJavaResource(
      "test", "Invalid", "Valid") {
    @Override
    public void check(JClassType classInfo) {
      fail("Both classes should have been removed");
    }

    @Override
    public String getSource() {
      StringBuffer sb = new StringBuffer();
      sb.append("package " + getPackageName() + ";\n");
      sb.append("public class Invalid extends NoSuchClass { }\n");
      sb.append("class Valid extends Object { }\n");
      return sb.toString();
    }
  };

  protected CheckedJavaResource CU_RefsInfectedCompilationUnit = new CheckedJavaResource(
      "test", "RefsInfectedCompilationUnit") {
    @Override
    public void check(JClassType classInfo) {
      fail("This class should should have been removed because it refers to a class in another compilation unit that had problems");
    }

    @Override
    public String getSource() {
      StringBuffer sb = new StringBuffer();
      sb.append("package test;\n");
      sb.append("public class RefsInfectedCompilationUnit extends Valid { }\n");
      return sb.toString();
    }
  };

   /**
   * Tests that refreshing with a unit that has errors does not cause new units
   * that reference unchanged units to be removed. The strategy is to add some
   * good units that reference each other and build a {@link TypeOracle}. Then
   * we add some new units that have errors as well as some units that reference
   * old units which did not have errors. This ensures that the correct units
   * are pruned from the type oracle in the case where we encounter units with
   * errors.
   *
   * @throws UnableToCompleteException
   * @throws IOException
   */
  public void testRefreshWithErrors() throws TypeOracleException {
    // Add Object
    StringBuffer sb = new StringBuffer();
    sb.append("package java.lang;");
    sb.append("public class Object { }");
    addResource("java.lang.Object", sb);

    // Add UnmodifiedClass that will never change.
    sb = new StringBuffer();
    sb.append("package test.refresh.with.errors;");
    sb.append("public class UnmodifiedClass { }");
    addResource("test.refresh.with.errors.UnmodifiedClass", sb);

    // Add GoodClass that references a class that will go bad.
    sb = new StringBuffer();
    sb.append("package test.refresh.with.errors;\n");
    sb.append("public class GoodClass {\n");
    sb.append("  ClassThatWillGoBad ctwgb;\n");
    sb.append("}\n");
    addResource("test.refresh.with.errors.GoodClass", sb);

    // Add ClassThatWillGoBad that goes bad on the next refresh.
    MutableJavaResource unitThatWillGoBad = new MutableJavaResource(
        "test.refresh.with.errors",
        "test.refresh.with.errors.ClassThatWillGoBad") {

      private String source = "package test.refresh.with.errors;\n"
          + "public class ClassThatWillGoBad { }\n";

      @Override
      public String getSource() {
        return source;
      }

      @Override
      public void touch() {
        super.touch();
        source = "This will cause a syntax error.";
      }
    };

    resources.add(unitThatWillGoBad);

    buildTypeOracle();

    assertNotNull(typeOracle.findType("test.refresh.with.errors.UnmodifiedClass"));
    assertNotNull(typeOracle.findType("test.refresh.with.errors.GoodClass"));
    assertNotNull(typeOracle.findType("test.refresh.with.errors.ClassThatWillGoBad"));

    // Add AnotherGoodClass that references a
    // class that was not recompiled.
    sb = new StringBuffer();
    sb.append("package test.refresh.with.errors;\n");
    sb.append("public class AnotherGoodClass {\n");
    sb.append("  UnmodifiedClass uc; // This will cause the runaway pruning.\n");
    sb.append("}\n");
    addResource("test.refresh.with.errors.AnotherGoodClass", sb);

    // Add BadClass that has errors and originally
    // forced issue 2238.
    sb = new StringBuffer();
    sb.append("package test.refresh.with.errors;\n");
    sb.append("public class BadClass {\n");
    sb.append("  This will trigger a syntax error.\n");
    sb.append("}\n");
    addResource("test.refresh.with.errors.BadClass", sb);

    // Now this cup should cause errors.
    unitThatWillGoBad.touch();

    buildTypeOracle();

    assertNotNull(typeOracle.findType("test.refresh.with.errors.UnmodifiedClass"));
    assertNotNull(typeOracle.findType("test.refresh.with.errors.AnotherGoodClass"));
    assertNull(typeOracle.findType("test.refresh.with.errors.BadClass"));
    assertNull(typeOracle.findType("test.refresh.with.errors.ClassThatWillGoBad"));
    assertNull(typeOracle.findType("test.refresh.with.errors.GoodClass"));
  }

  public void testSyntaxErrors() throws TypeOracleException {
    resources.add(CU_Object);
    resources.add(CU_HasSyntaxErrors);
    buildTypeOracle();
    JClassType[] types = typeOracle.getTypes();
    // Only java.lang.Object should remain.
    //
    assertEquals(1, types.length);
    assertEquals("java.lang.Object", types[0].getQualifiedSourceName());
  }

  public void testUnresolvedSymbols() throws TypeOracleException {
    resources.add(CU_Object);
    resources.add(CU_HasUnresolvedSymbols);
    resources.add(CU_RefsInfectedCompilationUnit);
    buildTypeOracle();
    JClassType[] types = typeOracle.getTypes();
    // Only java.lang.Object should remain.
    //
    assertEquals(1, types.length);
    assertEquals("java.lang.Object", types[0].getQualifiedSourceName());
  }

  @Override
  protected void buildTypeOracle() throws TypeOracleException {
    typeOracle = TypeOracleTestingUtils.buildTypeOracle(createTreeLogger(),
        resources);
    checkTypes(typeOracle.getTypes());
  }
}
