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

  /**
   * Maps compilation strong name onto a <code>Set</code> of
   * <code>String[]</code>. We use a <code>TreeMap</code> to produce the
   * same generated code for the same set of compilations.
   */
  private final Map propertyValuesSetByStrongName = new TreeMap();
  private final Property[] orderedProps;
  private final Properties moduleProps;
  private final String moduleName;
  private final String moduleFunction;
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
    this.moduleFunction = moduleName.replace('.', '_');
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

        pw.print("    O([");
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

  /**
   * Generates a function that injects calls to a shared file-injection
   * functions.
   *
   * @param pw generate source onto this writer
   */
  private void genInjectExternalFiles(PrintWriter pw) {
    if (!hasExternalFiles()) {
      return;
    }

    for (Iterator iter = styles.iterator(); iter.hasNext();) {
      String src = (String) iter.next();
      pw.print(SelectionScriptTemplate.cssInjector(src));
    }

    for (Iterator iter = scripts.iterator(); iter.hasNext();) {
      Script script = (Script) iter.next();
      pw.print(SelectionScriptTemplate.scriptInjector(script.getSrc()));
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
    pw.println("    var F;");
    pw.print("    var I = [");
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
  private void genScript(PrintWriter pw) {
    pw.print(SelectionScriptTemplate.fixedHeader());
    pw.println("function " + moduleFunction + "() {");
    pw.println(SelectionScriptTemplate.moduleFunctionHeader());

    // Hosted mode extras:
    if (orderedProps == null) {
      // Generate a check to switch off to the compiled script if
      // running in a non-hosted mode browser.
      pw.println("  if (!" + moduleFunction + ".isHostedMode()) {");
      pw.println("    document.write('<script src=\"" + moduleName +
          ".nocache.js?compiled\"></script>');");
      pw.println("    return;");
      pw.println("  }");

      // Create a global reference to providers which will be referenced by the
      // iframe's HTML in hosted mode (generated in
      // GWTShellServlet.genHostedCacheHtml).
      pw.println("  " + moduleFunction + ".providers = providers;");
      pw.println();
    }

    genPropProviders(pw);
    pw.println();

    // If the ordered props are specified, then we're generating for both modes.
    if (orderedProps != null) {
      // Web mode or hosted mode.
      if (orderedProps.length > 0) {
        pw.println();
        pw.print(SelectionScriptTemplate.answerFunction());
        pw.println();
        genSrcSetFunction(pw, null);
      } else {
        // Rare case of no properties; happens if you inherit from Core alone.
        assert (orderedProps.length == 0);
        Set entrySet = propertyValuesSetByStrongName.entrySet();
        assert (entrySet.size() == 1);
        Map.Entry entry = (Entry) entrySet.iterator().next();
        String strongName = (String) entry.getKey();
        genSrcSetFunction(pw, strongName);
      }
    } else {
      // Hosted mode only, so there is no strong name selection (i.e. because
      // there is no compiled JavaScript);
      pw.println("  " + moduleFunction + ".processMetas();");
      printDocumentWrite(pw, "  ", "<iframe id='" + moduleName
          + "' style='width:0;height:0;border:0' src='" + moduleName
          + ".cache.html'></iframe>");
    }

    pw.println();

    // Script and CSS injection.
    genInjectExternalFiles(pw);
    printDocumentWrite(pw, "  ", "<script>" + moduleFunction + ".onInjectionDone('" + moduleName + "')</script>");

    pw.println("}\n");

    pw.print(SelectionScriptTemplate.outerFunctions(moduleName));
    pw.println(moduleFunction + "();");
  }

  /**
   * @param pw generate source onto this writer
   * @param oneAndOnlyStrongName if <code>null</code>, use the normal logic;
   *          otherwise, there are no client properties and thus there is
   *          exactly one permutation, specified by this parameter
   */
  private void genSrcSetFunction(PrintWriter pw, String oneAndOnlyStrongName) {
    pw.println("  try {");
    pw.println("    " + moduleFunction + ".processMetas();");
    pw.println();

    if (oneAndOnlyStrongName == null) {
      genPropValues(pw);
      pw.println();
      genAnswers(pw);
      pw.println();
      pw.print("    var strongName = O.answers");
      for (int i = 0; i < orderedProps.length; i++) {
        pw.print("[I[" + i + "]]");
      }
      pw.println(";");

      pw.println("    var query = location.search;");
      pw.println("    query = query.substring(0, query.indexOf('&'));");
      pw.println();
      pw.println("    var base;");
      pw.println("    if (window.__gwt_base) {");
      pw.println("      base = __gwt_base['" + moduleName + "'];");
      pw.println("    }");
      pw.println("    var newUrl = (base ? base + '/' : '') + strongName + '.cache.html' + query;");
      pw.println("    document.write('<iframe id=\""
          + moduleName
          + "\" style=\"width:0;height:0;border:0\" src=\"\' + newUrl + \'\"></iframe>');");
    } else {
      // There is exactly one compilation, so it is unconditionally selected.
      String scriptToLoad = oneAndOnlyStrongName + ".cache.html";
      printDocumentWrite(pw, "  ", "<iframe id='" + moduleName
          + "' style='width:0;height:0;border:0' src='\" + "
          + oneAndOnlyStrongName + " + \".cache.html'></iframe>");
    }

    pw.println("  } catch (e) {");
    pw.println("    // intentionally silent on property failure");
    pw.println("  }");
  }

  private boolean hasExternalFiles() {
    return !scripts.isEmpty() || !styles.isEmpty();
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

  private void printDocumentWrite(PrintWriter pw, String prefix, String payLoad) {
    payLoad = payLoad.replaceAll("'", "\\\\'");
    pw.println(prefix + "document.write('" + payLoad + "');");
  }
}
