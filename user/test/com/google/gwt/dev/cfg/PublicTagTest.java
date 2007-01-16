// Copyright 2006 Google Inc. All Rights Reserved.
package com.google.gwt.dev.cfg;

import com.google.gwt.dev.GWTCompiler;
import com.google.gwt.dev.util.Util;

import junit.framework.TestCase;

import java.io.File;

/**
 * Tests various permutations of the GWT module's &amp;public&amp; tag,
 * specifically its ant-like inclusion support.
 */
public class PublicTagTest extends TestCase {

  public void testPublicTag() {
    // Find the current directory
    String userDir = System.getProperty("user.dir");
    assertNotNull(userDir);
    File curDir = new File(userDir);
    assertTrue(curDir.isDirectory());

    // Our module name is the same as this class's name
    String moduleName = PublicTagTest.class.getName();

    // Find our module output directory and delete it
    File moduleDir = new File(curDir, moduleName);
    if (moduleDir.exists()) {
      Util.recursiveDelete(moduleDir, false);
    }
    assertFalse(moduleDir.exists());

    // Compile the dummy app; suppress output to stdout
    GWTCompiler.main(new String[]{moduleName, "-logLevel", "ERROR"});

    // Check the output folder
    assertTrue(new File(moduleDir, "good0.html").exists());
    assertTrue(new File(moduleDir, "good1.html").exists());
    assertTrue(new File(moduleDir, "bar/good.html").exists());
    assertTrue(new File(moduleDir, "good2.html").exists());
    assertTrue(new File(moduleDir, "good3.html").exists());
    assertTrue(new File(moduleDir, "good4.html").exists());
    assertTrue(new File(moduleDir, "good5.html").exists());
    assertTrue(new File(moduleDir, "good6.html").exists());
    assertTrue(new File(moduleDir, "good7.html").exists());
    assertTrue(new File(moduleDir, "good8.html").exists());
    assertTrue(new File(moduleDir, "good10.html").exists());
    assertTrue(new File(moduleDir, "good11.html").exists());
    assertTrue(new File(moduleDir, "good9.html").exists());
    assertTrue(new File(moduleDir, "bar/CVS/good.html").exists());
    assertTrue(new File(moduleDir, "CVS/good.html").exists());
    assertTrue(new File(moduleDir, "GOOD/bar/GOOD/good.html").exists());
    assertTrue(new File(moduleDir, "GOOD/good.html").exists());

    assertFalse(new File(moduleDir, "bad.Html").exists());
    assertFalse(new File(moduleDir, "bar/CVS/bad.html").exists());
    assertFalse(new File(moduleDir, "CVS/bad.html").exists());
    assertFalse(new File(moduleDir, "bad1.html").exists());
    assertFalse(new File(moduleDir, "bad2.html").exists());
    assertFalse(new File(moduleDir, "bad3.html").exists());
    assertFalse(new File(moduleDir, "bad.html").exists());
    assertFalse(new File(moduleDir, "bar/bad.html").exists());
    assertFalse(new File(moduleDir, "GOOD/bar/bad.html").exists());
    assertFalse(new File(moduleDir, "GOOD/bar/GOOD/bar/bad.html").exists());
  }

}
