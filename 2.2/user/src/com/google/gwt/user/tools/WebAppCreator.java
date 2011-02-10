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
package com.google.gwt.user.tools;

import com.google.gwt.dev.About;
import com.google.gwt.dev.ArgProcessorBase;
import com.google.gwt.dev.Compiler;
import com.google.gwt.dev.DevMode;
import com.google.gwt.dev.GwtVersion;
import com.google.gwt.dev.util.Util;
import com.google.gwt.user.tools.util.ArgHandlerIgnore;
import com.google.gwt.user.tools.util.ArgHandlerOverwrite;
import com.google.gwt.user.tools.util.CreatorUtilities;
import com.google.gwt.util.tools.ArgHandlerExtra;
import com.google.gwt.util.tools.ArgHandlerFlag;
import com.google.gwt.util.tools.ArgHandlerOutDir;
import com.google.gwt.util.tools.ArgHandlerString;
import com.google.gwt.util.tools.Utility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates a GWT application skeleton.
 */
public final class WebAppCreator {

  class ArgProcessor extends ArgProcessorBase {

    private final class ArgHandlerOutDirExtension extends ArgHandlerOutDir {
      @Override
      public void setDir(File dir) {
        outDir = dir;
      }
    }

    public ArgProcessor() {
      registerHandler(new ArgHandlerOverwriteExtension());
      registerHandler(new ArgHandlerIgnoreExtension());
      registerHandler(new ArgHandlerModuleName());
      registerHandler(new ArgHandlerOutDirExtension());
      registerHandler(new ArgHandlerNoEclipse());
      registerHandler(new ArgHandlerOnlyEclipse());
      registerHandler(new ArgHandlerJUnitPath());
      registerHandler(new ArgHandlerMaven());
      registerHandler(new ArgHandlerNoAnt());
    }

    @Override
    protected String getName() {
      return WebAppCreator.class.getName();
    }
  }

  private final class ArgHandlerIgnoreExtension extends ArgHandlerIgnore {
    @Override
    public boolean setFlag() {
      if (overwrite) {
        System.err.println("-ignore cannot be used with -overwrite");
        return false;
      }
      ignore = true;
      return true;
    }
  }

  private final class ArgHandlerModuleName extends ArgHandlerExtra {
    @Override
    public boolean addExtraArg(String arg) {
      if (moduleName != null) {
        System.err.println("Too many arguments.");
        return false;
      }

      if (!CreatorUtilities.isValidModuleName(arg)) {
        System.err.println("'"
            + arg
            + "' does not appear to be a valid fully-qualified Java class name.");
        return false;
      }

      moduleName = arg;
      return true;
    }

    @Override
    public String getPurpose() {
      return "The name of the module to create (e.g. com.example.myapp.MyApp)";
    }

    @Override
    public String[] getTagArgs() {
      return new String[] {"moduleName"};
    }

    @Override
    public boolean isRequired() {
      return true;
    }
  }

  private final class ArgHandlerNoEclipse extends ArgHandlerFlag {
    @Override
    public String getPurpose() {
      return "Do not generate eclipse files";
    }

    @Override
    public String getTag() {
      return "-XnoEclipse";
    }

    @Override
    public boolean isUndocumented() {
      return true;
    }

    @Override
    public boolean setFlag() {
      noEclipse = true;
      return true;
    }
  }

  private final class ArgHandlerOnlyEclipse extends ArgHandlerFlag {
    @Override
    public String getPurpose() {
      return "Generate only eclipse files";
    }

    @Override
    public String getTag() {
      return "-XonlyEclipse";
    }

    @Override
    public boolean isUndocumented() {
      return true;
    }

    @Override
    public boolean setFlag() {
      onlyEclipse = true;
      return true;
    }
  }

  private final class ArgHandlerOverwriteExtension extends ArgHandlerOverwrite {
    @Override
    public boolean setFlag() {
      if (ignore) {
        System.err.println("-overwrite cannot be used with -ignore");
        return false;
      }
      overwrite = true;
      return true;
    }
  }

  private final class ArgHandlerJUnitPath extends ArgHandlerString {

    @Override
    public String[] getDefaultArgs() {
      return null;
    }

    @Override
    public String getPurpose() {
      return "Specifies the path to your junit.jar (optional)";
    }

    @Override
    public String getTag() {
      return "-junit";
    }

    @Override
    public String[] getTagArgs() {
      return new String[] {"pathToJUnitJar"};
    }

    @Override
    public boolean isRequired() {
      return false;
    }

    @Override
    public boolean setString(String str) {
      File f = new File(str);
      if (!f.exists() || !f.isFile()) {
        System.err.println("File not found: " + str);
        return false;
      }
      junitPath = str;
      return true;
    }
  }

  private final class ArgHandlerMaven extends ArgHandlerFlag {
    @Override
    public String getPurpose() {
      return "Create a maven2 project structure and pom file (default disabled)";
    }

    @Override
    public String getTag() {
      return "-maven";
    }

    @Override
    public boolean setFlag() {
      maven = true;
      return true;
    }
  }

  private final class ArgHandlerNoAnt extends ArgHandlerFlag {
    @Override
    public String getPurpose() {
      return "Do not create an ant configuration file";
    }

    @Override
    public String getTag() {
      return "-noant";
    }

    @Override
    public boolean setFlag() {
      ant = false;
      return true;
    }
  }

  private static final class FileCreator {
    private final File destDir;
    private final String destName;
    private final String sourceName;

    public FileCreator(File destDir, String destName, String sourceName) {
      this.destDir = destDir;
      this.sourceName = sourceName;
      this.destName = destName;
    }
  }

  public static void main(String[] args) {
    System.exit(doMain(args) ? 0 : 1);
  }

  protected static boolean doMain(String... args) {
    WebAppCreator creator = new WebAppCreator();
    ArgProcessor argProcessor = creator.new ArgProcessor();
    if (argProcessor.processArgs(args)) {
      return creator.run();
    }
    return false;
  }

  private boolean ant = true;
  private boolean ignore = false;
  private boolean maven = false;
  private String moduleName;
  private boolean noEclipse;
  private boolean onlyEclipse;
  private File outDir;
  private boolean overwrite = false;
  private String junitPath = null;

  /**
   * Create the sample app.
   * 
   * @throws IOException if any disk write fails
   * @deprecated as of GWT 2.1, replaced by {@link #doRun(String)}
   */
  @Deprecated
  protected void doRun() throws IOException {
    doRun(Utility.getInstallPath());
  }

  /**
   * Create the sample app.
   * 
   * @param installPath directory containing GWT libraries
   * @throws IOException if any disk write fails
   */
  protected void doRun(String installPath) throws IOException {

    // GWT libraries
    String gwtUserPath = installPath + '/' + "gwt-user.jar";
    String gwtDevPath = installPath + '/' + "gwt-dev.jar";

    // Public builds generate a DTD reference.
    String gwtModuleDtd = "";
    GwtVersion gwtVersion = About.getGwtVersionObject();
    if (gwtVersion.isNoNagVersion()) {
      gwtModuleDtd = "\n<!DOCTYPE module PUBLIC \"-//Google Inc.//DTD Google Web Toolkit "
          + About.getGwtVersionNum()
          + "//EN\" \"http://google-web-toolkit.googlecode.com/svn/tags/"
          + About.getGwtVersionNum()
          + "/distro-source/core/src/gwt-module.dtd\">";
    }

    // Compute module package and name.
    int pos = moduleName.lastIndexOf('.');
    String modulePackageName = moduleName.substring(0, pos);
    String moduleShortName = moduleName.substring(pos + 1);

    // pro-actively let ant user know that this script can also create tests.
    if (junitPath == null && !maven) {
      System.err.println("Not creating tests because -junit argument was not specified.\n");
    }

    // Compute module name and directories
    String srcFolder = maven ? "src/main/java" : "src";
    String testFolder = maven ? "src/test/java" : "test";
    String warFolder = maven ? "src/main/webapp" : "war";
    File srcDir = Utility.getDirectory(outDir, srcFolder, true);
    File warDir = Utility.getDirectory(outDir, warFolder, true);
    File webInfDir = Utility.getDirectory(warDir, "WEB-INF", true);
    File libDir = Utility.getDirectory(webInfDir, "lib", true);
    File moduleDir = Utility.getDirectory(srcDir, modulePackageName.replace(
        '.', '/'), true);
    File clientDir = Utility.getDirectory(moduleDir, "client", true);
    File serverDir = Utility.getDirectory(moduleDir, "server", true);
    File sharedDir = Utility.getDirectory(moduleDir, "shared", true);
    File moduleTestDir = Utility.getDirectory(outDir, testFolder + "/"
        + modulePackageName.replace('.', '/'), true);
    File clientTestDir = Utility.getDirectory(moduleTestDir, "client", true);

    // Create a map of replacements
    Map<String, String> replacements = new HashMap<String, String>();
    replacements.put("@moduleShortName", moduleShortName);
    replacements.put("@modulePackageName", modulePackageName);
    replacements.put("@moduleName", moduleName);
    replacements.put("@clientPackage", modulePackageName + ".client");
    replacements.put("@serverPackage", modulePackageName + ".server");
    replacements.put("@sharedPackage", modulePackageName + ".shared");
    replacements.put("@gwtSdk", installPath);
    replacements.put("@gwtUserPath", gwtUserPath);
    replacements.put("@gwtDevPath", gwtDevPath);
    replacements.put("@gwtVersion", About.getGwtVersionNum());
    replacements.put("@gwtModuleDtd", gwtModuleDtd);
    replacements.put("@shellClass", DevMode.class.getName());
    replacements.put("@compileClass", Compiler.class.getName());
    replacements.put("@startupUrl", moduleShortName + ".html");
    replacements.put("@renameTo", moduleShortName.toLowerCase());
    replacements.put("@moduleNameJUnit", moduleName + "JUnit");
    replacements.put("@srcFolder", srcFolder);
    replacements.put("@testFolder", testFolder);
    replacements.put("@warFolder", warFolder);

    // Add command to copy gwt-servlet-deps.jar into libs, unless this is a
    // maven project. Maven projects should include libs as maven dependencies.
    String copyServletDeps = "";
    if (!maven) {
      copyServletDeps = "<copy todir=\"" + warFolder + "/WEB-INF/lib\" "
          + "file=\"${gwt.sdk}/gwt-servlet-deps.jar\" />";
    }
    replacements.put("@copyServletDeps", copyServletDeps);

    // Collect the list of server libs to include on the eclipse classpath.
    StringBuilder serverLibs = new StringBuilder();
    if (libDir.exists()) {
      for (File file : libDir.listFiles()) {
        if (file.getName().toLowerCase().endsWith(".jar")) {
          serverLibs.append("   <classpathentry kind=\"lib\" path=\"war/WEB-INF/lib/");
          serverLibs.append(file.getName());
          serverLibs.append("\"/>\n");
        }
      }
    }
    replacements.put("@serverClasspathLibs", serverLibs.toString());

    String antEclipseRule = "";
    if (noEclipse) {
      /*
       * Generate a rule into the build file that allows for the generation of
       * an eclipse project later on. This is primarily for distro samples. This
       * is a quick and dirty way to inject a build rule, but it works.
       */
      antEclipseRule = "\n\n"
          + "  <target name=\"eclipse.generate\" depends=\"libs\" description=\"Generate eclipse project\">\n"
          + "    <java failonerror=\"true\" fork=\"true\" classname=\""
          + this.getClass().getName() + "\">\n" + "      <classpath>\n"
          + "        <path refid=\"project.class.path\"/>\n"
          + "      </classpath>\n" + "      <arg value=\"-XonlyEclipse\"/>\n"
          + "      <arg value=\"-ignore\"/>\n" + "      <arg value=\""
          + moduleName + "\"/>\n" + "    </java>\n" + "  </target>";
    } else {
      antEclipseRule = "";
    }
    replacements.put("@antEclipseRule", antEclipseRule);

    {
      String testTargetsBegin = "";
      String testTargetsEnd = "";
      String junitJarPath = junitPath;
      String eclipseTestDir = "";
      if (junitPath != null || maven) {
        eclipseTestDir = "\n   <classpathentry kind=\"src\" path=\""
            + testFolder + "\"/>";
      } 
      if (junitPath == null) {
        testTargetsBegin = "\n<!--"
            + "\n"
            + "Test targets suppressed because -junit argument was not specified when running webAppCreator.\n";
        testTargetsEnd = "-->\n";
        junitJarPath = "path_to_the_junit_jar";
      }
      replacements.put("@testTargetsBegin", testTargetsBegin);
      replacements.put("@testTargetsEnd", testTargetsEnd);
      replacements.put("@junitJar", junitJarPath);
      replacements.put("@eclipseTestDir", eclipseTestDir);
    }

    // Create a list with the files to create
    List<FileCreator> files = new ArrayList<FileCreator>();
    if (!onlyEclipse) {
      files.add(new FileCreator(moduleDir, moduleShortName + ".gwt.xml",
          "Module.gwt.xml"));
      files.add(new FileCreator(warDir, moduleShortName + ".html",
          "AppHtml.html"));
      files.add(new FileCreator(warDir, moduleShortName + ".css", "AppCss.css"));
      files.add(new FileCreator(webInfDir, "web.xml", "web.xml"));
      files.add(new FileCreator(clientDir, moduleShortName + ".java",
          "AppClassTemplate.java"));
      files.add(new FileCreator(clientDir, "GreetingService.java",
          "RpcClientTemplate.java"));
      files.add(new FileCreator(clientDir, "GreetingServiceAsync.java",
          "RpcAsyncClientTemplate.java"));
      files.add(new FileCreator(serverDir, "GreetingServiceImpl.java",
          "RpcServerTemplate.java"));
      files.add(new FileCreator(sharedDir, "FieldVerifier.java",
          "SharedClassTemplate.java"));
      if (ant) {
        files.add(new FileCreator(outDir, "build.xml", "project.ant.xml"));
      }
      if (maven) {
        files.add(new FileCreator(outDir, "pom.xml", "project.maven.xml"));
      }
      files.add(new FileCreator(outDir, "README.txt", "README.txt"));
      if (junitPath != null || maven) {
        // create the test file.
        files.add(new FileCreator(moduleTestDir, moduleShortName
            + "JUnit.gwt.xml", "JUnit.gwt.xml"));
        files.add(new FileCreator(clientTestDir, moduleShortName + "Test"
            + ".java", "JUnitClassTemplate.java"));
      }
    }
    if (!noEclipse) {
      files.add(new FileCreator(outDir, ".project", ".project"));
      files.add(new FileCreator(outDir, ".classpath", ".classpath"));
      files.add(new FileCreator(outDir, moduleShortName + ".launch",
          "App.launch"));
      if (junitPath != null || maven) {
        files.add(new FileCreator(outDir, moduleShortName + "Test-dev.launch",
            "JUnit-dev.launch"));
        files.add(new FileCreator(outDir, moduleShortName + "Test-prod.launch",
            "JUnit-prod.launch"));
      }
    }

    // copy source files, replacing the content as needed
    for (FileCreator fileCreator : files) {
      URL url = WebAppCreator.class.getResource(fileCreator.sourceName + "src");
      if (url == null) {
        throw new FileNotFoundException(fileCreator.sourceName + "src");
      }
      File file = Utility.createNormalFile(fileCreator.destDir,
          fileCreator.destName, overwrite, ignore);
      if (file != null) {
        String data = Util.readURLAsString(url);
        Utility.writeTemplateFile(file, data, replacements);
      }
    }
  }

  protected boolean run() {
    try {
      doRun(Utility.getInstallPath());
      return true;
    } catch (IOException e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      return false;
    }
  }
}
