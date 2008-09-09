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

import com.google.gwt.dev.cfg.ModuleDefLoader;
import com.google.gwt.user.tools.util.ArgHandlerAddToClassPath;
import com.google.gwt.user.tools.util.ArgHandlerEclipse;
import com.google.gwt.user.tools.util.ArgHandlerIgnore;
import com.google.gwt.user.tools.util.ArgHandlerOverwrite;
import com.google.gwt.user.tools.util.CreatorUtilities;
import com.google.gwt.util.tools.ArgHandlerExtra;
import com.google.gwt.util.tools.ArgHandlerOutDir;
import com.google.gwt.util.tools.ArgHandlerString;
import com.google.gwt.util.tools.ToolBase;
import com.google.gwt.util.tools.Utility;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Creates a GWT application skeleton.
 */
public final class ApplicationCreator extends ToolBase {

  /*
   * Arguments for the application creator.
   */

  /**
   * Add an extra module injection into the top level module file.
   */
  protected class ArgHandlerAddModule extends ArgHandlerString {
    private List<String> extraModuleList = new ArrayList<String>();

    public List<String> getExtraModuleList() {
      return extraModuleList;
    }

    @Override
    public String getPurpose() {
      return "Adds extra GWT modules to be inherited.";
    }

    @Override
    public String getTag() {
      return "-addModule";
    }

    @Override
    public String[] getTagArgs() {
      return new String[] {"module"};
    }

    @Override
    public boolean setString(String str) {
      // Parse out a comma separated list
      StringTokenizer st = new StringTokenizer(str, ",");
      while (st.hasMoreTokens()) {
        String module = st.nextToken();

        // Check className to see that it is a period separated string of words.
        if (!module.matches("[\\w\\$]+(\\.[\\w\\$]+)+")) {
          System.err.println("'" + module
              + "' does not appear to be a valid fully-qualified module name");
          return false;
        }
        extraModuleList.add(module);
      }

      return true;
    }
  }

  /**
   * Specify the top level class name of the application to create.
   */
  protected class ArgHandlerAppClass extends ArgHandlerExtra {

    @Override
    public boolean addExtraArg(String arg) {
      if (fullClassName != null) {
        System.err.println("Too many arguments");
        return false;
      }

      // Check className for certain properties
      if (!arg.matches("[\\w\\$]+(\\.[\\w\\$]+)+")) {
        System.err.println("'" + arg
            + "' does not appear to be a valid fully-qualified Java class name");
        return false;
      }

      // Check out the class name.
      if (arg.indexOf('$') != -1) {
        System.err.println("'" + arg
            + "': This version of the tool does not support nested classes");
        return false;
      }

      String[] parts = arg.split("\\.");
      if (parts.length < 2 || !parts[parts.length - 2].equals("client")) {
        System.err.println("'"
            + arg
            + "': Please use 'client' as the final package, as in 'com.example.foo.client.MyApp'.\n"
            + "It isn't technically necessary, but this tool enforces the best practice.");
        return false;
      }

      fullClassName = arg;
      return true;
    }

    @Override
    public String getPurpose() {
      return "The fully-qualified name of the application class to create";
    }

    @Override
    public String[] getTagArgs() {
      return new String[] {"className"};
    }

    @Override
    public boolean isRequired() {
      return true;
    }
  }

  private static final String PACKAGE_PATH;

  static {
    String path = ApplicationCreator.class.getName();
    path = path.substring(0, path.lastIndexOf('.') + 1);
    PACKAGE_PATH = path.replace('.', '/');
  }

  public static void main(String[] args) {
    ApplicationCreator creator = new ApplicationCreator();
    if (creator.processArgs(args)) {
      if (creator.run()) {
        return;
      }
    }
    System.exit(1);
  }

  /**
   * @param fullClassName Name of the fully-qualified Java class to create as an
   *          Application.
   * @param outDir Where to put the output files
   * @param eclipse The name of a project to attach a .launch config to
   * @param overwrite Overwrite an existing files if they exist.
   * @param ignore Ignore existing files if they exist.
   * @throws IOException
   */
  static void createApplication(String fullClassName, File outDir,
      String eclipse, boolean overwrite, boolean ignore) throws IOException {
    createApplication(fullClassName, outDir, eclipse, overwrite, ignore, null,
        null);
  }

  /**
   * @param fullClassName Name of the fully-qualified Java class to create as an
   *          Application.
   * @param outDir Where to put the output files
   * @param eclipse The name of a project to attach a .launch config to
   * @param overwrite Overwrite an existing files if they exist.
   * @param ignore Ignore existing files if they exist.
   * @param extraClassPaths A list of paths to append to the class path for
   *          launch configs.
   * @param extraModules A list of GWT modules to add 'inherits' tags for.
   * @throws IOException
   */
  static void createApplication(String fullClassName, File outDir,
      String eclipse, boolean overwrite, boolean ignore,
      List<String> extraClassPaths, List<String> extraModules)
      throws IOException {

    // Figure out the installation directory
    String installPath = Utility.getInstallPath(true);
    String gwtUserPath = installPath + '/' + "gwt-user.jar";
    String gwtDevPath = installPath + '/' + "gwt-dev.jar";

    // Validate the arguments for extra class path entries and modules.
    if (!CreatorUtilities.validatePathsAndModules(gwtUserPath, extraClassPaths,
        extraModules)) {
      return;
    }

    // Check out the class and package names.
    //
    int pos = fullClassName.lastIndexOf('.');
    String clientPackageName = fullClassName.substring(0, pos);
    String className = fullClassName.substring(pos + 1);

    // Compute module name and directories
    //
    pos = clientPackageName.lastIndexOf('.');
    File basePackageDir;
    String moduleName;
    File javaDir = Utility.getDirectory(outDir, "src", true);
    if (pos >= 0) {
      String basePackage = clientPackageName.substring(0, pos);
      moduleName = basePackage + "." + className;
      basePackage = basePackage.replace('.', '/');
      basePackageDir = Utility.getDirectory(javaDir, basePackage, true);
    } else {
      moduleName = className;
      basePackageDir = javaDir;
    }
    File clientDir = Utility.getDirectory(basePackageDir, "client", true);
    File publicDir = Utility.getDirectory(basePackageDir, "public", true);
    String startupUrl = moduleName + "/" + className + ".html";

    // Create a map of replacements
    //
    Map<String, String> replacements = new HashMap<String, String>();
    replacements.put("@className", className);
    replacements.put("@moduleName", moduleName);
    replacements.put("@clientPackage", clientPackageName);
    replacements.put("@gwtUserPath", gwtUserPath);
    replacements.put("@gwtDevPath", gwtDevPath);
    replacements.put("@shellClass", "com.google.gwt.dev.GWTShell");
    replacements.put("@compileClass", "com.google.gwt.dev.GWTCompiler");
    replacements.put("@startupUrl", startupUrl);
    replacements.put("@vmargs", "");
    replacements.put("@eclipseExtraLaunchPaths",
        CreatorUtilities.createEclipseExtraLaunchPaths(extraClassPaths));
    replacements.put("@extraModuleInherits",
        createExtraModuleInherits(extraModules));
    replacements.put("@extraClassPathsColon", CreatorUtilities.appendPaths(":",
        extraClassPaths));
    replacements.put("@extraClassPathsSemicolon", CreatorUtilities.appendPaths(
        ";", extraClassPaths));

    {
      // Create the module
      File moduleXML = Utility.createNormalFile(basePackageDir, className
          + ModuleDefLoader.GWT_MODULE_XML_SUFFIX, overwrite, ignore);
      if (moduleXML != null) {
        String out = Utility.getFileFromClassPath(PACKAGE_PATH
            + "Module.gwt.xmlsrc");
        Utility.writeTemplateFile(moduleXML, out, replacements);
      }
    }

    {
      // Create a skeleton html file
      File publicHTML = Utility.createNormalFile(publicDir,
          className + ".html", overwrite, ignore);
      if (publicHTML != null) {
        String out = Utility.getFileFromClassPath(PACKAGE_PATH
            + "AppHtml.htmlsrc");
        Utility.writeTemplateFile(publicHTML, out, replacements);
      }
    }

    {
      // Create a skeleton css file
      File publicCSS = Utility.createNormalFile(publicDir, className + ".css",
          overwrite, ignore);
      if (publicCSS != null) {
        String out = Utility.getFileFromClassPath(PACKAGE_PATH
            + "AppCss.csssrc");
        Utility.writeTemplateFile(publicCSS, out, replacements);
      }
    }

    {
      // Create a skeleton Application class
      File javaClass = Utility.createNormalFile(clientDir, className + ".java",
          overwrite, ignore);
      if (javaClass != null) {
        String out = Utility.getFileFromClassPath(PACKAGE_PATH
            + "AppClassTemplate.javasrc");
        Utility.writeTemplateFile(javaClass, out, replacements);
      }
    }

    if (eclipse != null) {
      // Create an eclipse launch config
      replacements.put("@projectName", eclipse);
      File launchConfig = Utility.createNormalFile(outDir, className
          + ".launch", overwrite, ignore);
      if (launchConfig != null) {
        String out = Utility.getFileFromClassPath(PACKAGE_PATH
            + "App.launchsrc");
        Utility.writeTemplateFile(launchConfig, out, replacements);
      }
    }

    // create startup files

    // If the path from here to the install directory is relative, we need to
    // set specific "base" directory tags; this is for sample generation during
    // the build.
    String[] basePathEnvs;
    if (!new File(installPath).isAbsolute()) {
      basePathEnvs = new String[] {"%~dp0\\", "$APPDIR/"};
    } else {
      basePathEnvs = new String[] {"", ""};
    }
    String extensions[] = {".cmd", ""};

    for (int platform = 0; platform < 2; ++platform) {
      String basePathEnv = basePathEnvs[platform];
      String extension = extensions[platform];
      replacements.put("@gwtUserPath", basePathEnv + gwtUserPath);
      replacements.put("@gwtDevPath", basePathEnv + gwtDevPath);

      File gwtshell = Utility.createNormalFile(outDir, className + "-shell"
          + extension, overwrite, ignore);
      if (gwtshell != null) {
        String out = Utility.getFileFromClassPath(PACKAGE_PATH + "gwtshell"
            + extension + "src");
        Utility.writeTemplateFile(gwtshell, out, replacements);
        if (extension.length() == 0) {
          chmodExecutable(gwtshell);
        }
      }

      File gwtcompile = Utility.createNormalFile(outDir, className + "-compile"
          + extension, overwrite, ignore);
      if (gwtcompile != null) {
        String out = Utility.getFileFromClassPath(PACKAGE_PATH + "gwtcompile"
            + extension + "src");
        Utility.writeTemplateFile(gwtcompile, out, replacements);
        if (extension.length() == 0) {
          chmodExecutable(gwtcompile);
        }
      }
    }
  }

  /**
   * Try to make the given file executable. Implementation tries to exec chmod,
   * which may fail if the platform doesn't support it. Prints a warning to
   * stderr if the call fails.
   * 
   * @param file the file to make executable
   */
  private static void chmodExecutable(File file) {
    try {
      Runtime.getRuntime().exec("chmod u+x " + file.getAbsolutePath());
    } catch (Throwable e) {
    }
  }

  private static String createExtraModuleInherits(List<String> modules) {
    if (modules == null) {
      return "";
    }
    // Create an <inherits> tag in the gwt.xml file for each extra module
    StringBuilder buf = new StringBuilder();
    for (String module : modules) {
      buf.append("      <inherits name=\"");
      buf.append(module);
      buf.append("\" />\n");
    }
    return buf.toString();
  }

  private ArgHandlerAddToClassPath classPathHandler = new ArgHandlerAddToClassPath();
  private String eclipse = null;
  private String fullClassName = null;
  private boolean ignore = false;
  private ArgHandlerAddModule moduleHandler = new ArgHandlerAddModule();
  private File outDir;
  private boolean overwrite = false;

  protected ApplicationCreator() {

    registerHandler(new ArgHandlerEclipse() {
      @Override
      public String getPurpose() {
        return "Creates a debug launch config for the named eclipse project";
      }

      @Override
      public boolean setString(String str) {
        eclipse = str;
        return true;
      }
    });

    registerHandler(new ArgHandlerOutDir() {
      @Override
      public void setDir(File dir) {
        outDir = dir;
      }
    });

    registerHandler(new ArgHandlerOverwrite() {
      @Override
      public boolean setFlag() {
        if (ignore) {
          System.err.println("-overwrite cannot be used with -ignore");
          return false;
        }
        overwrite = true;
        return true;
      }
    });

    registerHandler(new ArgHandlerIgnore() {
      @Override
      public boolean setFlag() {
        if (overwrite) {
          System.err.println("-ignore cannot be used with -overwrite");
          return false;
        }
        ignore = true;
        return true;
      }
    });

    registerHandler(new ArgHandlerAppClass());
    registerHandler(classPathHandler);
    registerHandler(moduleHandler);
  }

  protected boolean run() {
    try {
      createApplication(fullClassName, outDir, eclipse, overwrite, ignore,
          classPathHandler.getExtraClassPathList(),
          moduleHandler.getExtraModuleList());
      return true;
    } catch (IOException e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      return false;
    }
  }

}
