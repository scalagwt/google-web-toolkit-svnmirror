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
package com.google.gwt.dev.util;

import com.google.gwt.dev.cfg.ModuleDef;
import com.google.gwt.dev.cfg.Properties;
import com.google.gwt.dev.cfg.Property;
import com.google.gwt.dev.cfg.PropertyProvider;
import com.google.gwt.dev.cfg.Script;
import com.google.gwt.dev.cfg.Scripts;
import com.google.gwt.dev.cfg.Styles;
import com.google.gwt.util.tools.Utility;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

/**
 * Generates the "module.nocache.html" file for use in both hosted and web mode.
 * It is able to generate JavaScript with the knowledge of a module's settings.
 * This class is used by {@link com.google.gwt.dev.GWTCompiler} and
 * {@link com.google.gwt.dev.shell.GWTShellServlet}.
 */
public class SelectionScriptGenerator {

  private static String cssInjector(String cssUrl) {
    return "  if (!__gwt_stylesLoaded['" + cssUrl + "']) {\n"
        + "    __gwt_stylesLoaded['" + cssUrl + "'] = true;\n"
        + "    document.write('<link rel=\\\"stylesheet\\\" href=\\\"" + cssUrl
        + "\\\">');\n" + "  }\n";
  }

  private static void replaceAll(StringBuffer buf, String search, String replace) {
    int len = search.length();
    for (int pos = buf.indexOf(search); pos >= 0; pos = buf.indexOf(search,
        pos + 1)) {
      buf.replace(pos, pos + len, replace);
    }
  }

  private static String scriptInjector(String scriptUrl) {
    return "  if (!__gwt_scriptsLoaded['" + scriptUrl + "']) {\n"
        + "    __gwt_scriptsLoaded['" + scriptUrl + "'] = true;\n"
        + "    document.write('<script language=\\\"javascript\\\" src=\\\""
        + scriptUrl + "\\\"></script>');\n" + "  }\n";
  }

  private final String moduleFunction;
  private final String moduleName;
  private final Properties moduleProps;
  private final Property[] orderedProps;

  /**
   * Maps compilation strong name onto a <code>Set</code> of
   * <code>String[]</code>. We use a <code>TreeMap</code> to produce the
   * same generated code for the same set of compilations.
   */
  private final Map propertyValuesSetByStrongName = new TreeMap();

  private final Scripts scripts;

  private final Styles styles;

  /**
   * A constructor for creating a selection script that will work only in hosted
   * mode.
   * 
   * @param moduleDef the module for which the selection script will be
   *          generated
   */
  public SelectionScriptGenerator(ModuleDef moduleDef) {
    this.moduleName = moduleDef.getName();
    this.moduleFunction = moduleDef.getFunctionName();
    this.scripts = moduleDef.getScripts();
    this.styles = moduleDef.getStyles();
    this.moduleProps = moduleDef.getProperties();
    this.orderedProps = null;
  }

  /**
   * A constructor for creating a selection script that will work in either
   * hosted or web mode.
   * 
   * @param moduleDef the module for which the selection script will be
   *          generated
   * @param props the module's property objects, arranged in the same order in
   *          which sets of property values should be interpreted by the
   *          {@link #recordSelection(String[], String)} method
   */
  public SelectionScriptGenerator(ModuleDef moduleDef, Property[] props) {
    this.moduleName = moduleDef.getName();
    this.moduleFunction = moduleName.replace('.', '_');
    this.scripts = moduleDef.getScripts();
    this.styles = moduleDef.getStyles();
    this.moduleProps = moduleDef.getProperties();
    this.orderedProps = (Property[]) props.clone();
  }

  /**
   * Generates a selection script based on the current settings.
   * 
   * @return an javascript whose contents are the definition of a module.js file
   */
  public String generateSelectionScript() {
    StringWriter src = new StringWriter();
    PrintWriter pw = new PrintWriter(src, true);

    genScript(pw);

    pw.close();
    String html = src.toString();
    return html;
  }

  /**
   * Records a mapping from a unique set of client property values onto a strong
   * name (that is, a compilation).
   * 
   * @param values a set of client property values ordered such that the i'th
   *          value corresponds with the i'th property in {@link #props}
   * @param strongName the base name of a compiled <code>.cache.html</code>
   *          file
   */
  public void recordSelection(String[] values, String strongName) {
    Set valuesSet = (Set) propertyValuesSetByStrongName.get(strongName);
    if (valuesSet == null) {
      valuesSet = new HashSet();
      propertyValuesSetByStrongName.put(strongName, valuesSet);
    }
    valuesSet.add(values.clone());
  }

  private void genAnswers(PrintWriter pw) {
    for (Iterator iter = propertyValuesSetByStrongName.entrySet().iterator(); iter.hasNext();) {
      Map.Entry entry = (Entry) iter.next();
      String strongName = (String) entry.getKey();
      Set propValuesSet = (Set) entry.getValue();

      // Create one answers entry for each string array in the set.
      //
      for (Iterator iterator = propValuesSet.iterator(); iterator.hasNext();) {
        String[] propValues = (String[]) iterator.next();

        pw.print("      O([");
        for (int i = 0; i < orderedProps.length; i++) {
          if (i > 0) {
            pw.print(",");
          }
          pw.print(literal(propValues[i]));
        }
        pw.print("]");
        pw.print(",");
        pw.print(literal(strongName));
        pw.println(");");
      }
    }
  }

  private void genPropProviders(PrintWriter pw) {
    for (Iterator iter = moduleProps.iterator(); iter.hasNext();) {
      Property prop = (Property) iter.next();
      String activeValue = prop.getActiveValue();
      if (activeValue == null) {
        // Emit a provider function, defined by the user in module config.
        PropertyProvider provider = prop.getProvider();
        assert (provider != null) : "expecting a default property provider to have been set";
        String js = Jsni.generateJavaScript(provider.getBody());
        pw.print("providers['" + prop.getName() + "'] = function() ");
        pw.print(js);
        pw.println(";");

        // Emit a map of allowed property values as an object literal.
        pw.println();
        pw.println("values['" + prop.getName() + "'] = {");
        String[] knownValues = prop.getKnownValues();
        for (int i = 0; i < knownValues.length; i++) {
          if (i > 0) {
            pw.println(", ");
          }
          // Each entry is of the form: "propName":<index>.
          // Note that we depend here on the known values being already
          // enclosed in quotes (because property names can have dots which
          // aren't allowed unquoted as keys in the object literal).
          pw.print(literal(knownValues[i]) + ": ");
          pw.print(i);
        }
        pw.println();
        pw.println("};");

        // Emit a wrapper that verifies that the value is valid.
        // It is this function that is called directly to get the propery.
        pw.println();
        pw.println("props['" + prop.getName() + "'] = function() {");
        pw.println("  var v = providers['" + prop.getName() + "']();");
        pw.println("  var ok = values['" + prop.getName() + "'];");
        // Make sure this is an allowed value; if so, return.
        pw.println("  if (v in ok)");
        pw.println("    return v;");
        // Not an allowed value, so build a nice message and call the handler.
        pw.println("  var a = new Array(" + knownValues.length + ");");
        pw.println("  for (var k in ok)");
        pw.println("    a[ok[k]] = k;");
        pw.print("  " + moduleFunction + ".onBadProperty(");
        pw.print(literal(prop.getName()));
        pw.println(", a, v);");
        pw.println("  if (arguments.length > 0) throw null; else return null;");
        pw.println("};");
        pw.println();
      }
    }
  }

  private void genPropValues(PrintWriter pw) {
    pw.println("      var F;");
    pw.print("      var I = [");
    for (int i = 0; i < orderedProps.length; i++) {
      if (i > 0) {
        pw.print(", ");
      }

      Property prop = orderedProps[i];
      String activeValue = prop.getActiveValue();
      if (activeValue == null) {
        // This is a call to a property provider function.
        //
        PropertyProvider provider = prop.getProvider();
        assert (provider != null) : "expecting a default property provider to have been set";
        // When we call the provider, we supply a bogus argument to indicate
        // that it should throw an exception if the property is a bad value.
        // The absence of arguments (as in hosted mode) tells it to return null.
        pw.print("(F=props['" + prop.getName() + "'],F(1))");
      } else {
        // This property was explicitly set at compile-time.
        //
        pw.print(literal(activeValue));
      }
    }
    pw.println("];");
  }

  /**
   * Emits all the script required to set up the module and, in web mode, select
   * a compilation.
   * 
   * @param pw
   */
  private void genScript(PrintWriter mainPw) {
    try {
      String src = Utility.getFileFromClassPath("com/google/gwt/dev/util/SelectionScriptTemplate.js");
      StringBuffer buf = new StringBuffer(src);
      replaceAll(buf, "__MODULE_FUNC__", moduleFunction);
      replaceAll(buf, "__MODULE_NAME__", moduleName);

      // Remove hosted mode only stuff
      if (orderedProps != null) {
        int startPos = buf.indexOf("// __SHELL_SERVLET_ONLY_BEGIN__");
        int endPos = buf.indexOf("// __SHELL_SERVLET_ONLY_END__");
        buf.delete(startPos, endPos);
      }

      // Add external dependencies
      int startPos = buf.indexOf("// __MODULE_DEPS_END__");
      for (Iterator iter = styles.iterator(); iter.hasNext();) {
        String style = (String) iter.next();
        String text = cssInjector(style);
        buf.insert(startPos, text);
        startPos += text.length();
      }

      for (Iterator iter = scripts.iterator(); iter.hasNext();) {
        Script script = (Script) iter.next();
        String text = scriptInjector(script.getSrc());
        buf.insert(startPos, text);
        startPos += text.length();
      }

      // Add property providers
      {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        genPropProviders(pw);
        pw.close();
        String stuff = sw.toString();
        startPos = buf.indexOf("// __PROPERTIES_END__");
        buf.insert(startPos, stuff);
      }

      // Add permutations
      {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);

        // If the ordered props are specified, then we're generating for both
        // modes.
        if (orderedProps != null) {
          // Web mode or hosted mode.
          if (orderedProps.length > 0) {
            pw.println();
            genPropValues(pw);
            pw.println();
            genAnswers(pw);
            pw.println();
            pw.print("      strongName = O.answers");
            for (int i = 0; i < orderedProps.length; i++) {
              pw.print("[I[" + i + "]]");
            }
          } else {
            // Rare case of no properties; happens if you inherit from Core
            // alone.
            assert (orderedProps.length == 0);
            Set entrySet = propertyValuesSetByStrongName.entrySet();
            assert (entrySet.size() == 1);
            Map.Entry entry = (Entry) entrySet.iterator().next();
            String strongName = (String) entry.getKey();
            // There is exactly one compilation, so it is unconditionally
            // selected.
            pw.print("    strongName = " + literal(strongName));
          }
          pw.println(";");
        }

        pw.close();
        String stuff = sw.toString();
        startPos = buf.indexOf("// __PERMUTATIONS_END__");
        buf.insert(startPos, stuff);
      }

      mainPw.print(buf.toString());
    } catch (Throwable e) {
      throw new RuntimeException("Error processing selection script template.",
          e);
    }
  }

  /**
   * Determines whether or not the URL is relative.
   * 
   * @param src the test url
   * @return <code>true</code> if the URL is relative, <code>false</code> if
   *         not
   */
  private boolean isRelativeURL(String src) {
    // A straight absolute url for the same domain, server, and protocol.
    if (src.startsWith("/")) {
      return false;
    }

    // If it can be parsed as a URL, then it's probably absolute.
    try {
      URL testUrl = new URL(src);

      // Let's guess that it is absolute (thus, not relative).
      return false;
    } catch (MalformedURLException e) {
      // Do nothing, since it was a speculative parse.
    }

    // Since none of the above matched, let's guess that it's relative.
    return true;
  }

  private String literal(String lit) {
    return "\"" + lit + "\"";
  }

}
