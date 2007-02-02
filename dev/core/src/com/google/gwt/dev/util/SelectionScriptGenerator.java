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

  private void genAnswerFunction(PrintWriter pw) {
    pw.println("function O(a,v) {");
    pw.println("  var answer = O.answers;");
    pw.println("  var i = -1;");
    pw.println("  var n = a.length - 1;");
    pw.println("  while (++i < n) {");
    pw.println("    if (!(a[i] in answer)) {");
    pw.println("      answer[a[i]] = [];");
    pw.println("    }");
    pw.println("    answer = answer[a[i]];");
    pw.println("  }");
    pw.println("  answer[a[n]] = v;");
    pw.println("}");
    pw.println("O.answers = [];");
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
   * Generates scripts that determines the value of a deferred binding client
   * property specified statically in host html.
   */
  private void genGetMetaProperty(PrintWriter pw) {
    pw.println("function __gwt_getMetaProperty(name) {");
    pw.println("  var value = __gwt_metaProps[name];");
    pw.println("  if (value) {");
    pw.println("    return value;");
    pw.println("  } else {");
    pw.println("    return null;");
    pw.println("  }");
    pw.println("}");
  }

  private void genInitHandlers(PrintWriter pw) {
    pw.println("function __gwt_initHandlers(resize, beforeunload, unload) {");
    pw.println("  var oldOnResize = window.onresize;");
    pw.println("  window.onresize = function() {");
    pw.println("    resize();");
    pw.println("    if (oldOnResize)");
    pw.println("      oldOnResize();");
    pw.println("  };");
    pw.println();
    pw.println("  var oldOnBeforeUnload = window.onbeforeunload;");
    pw.println("  window.onbeforeunload = function() {");
    pw.println("    var ret = beforeunload();");
    pw.println();
    pw.println("    var oldRet;");
    pw.println("    if (oldOnBeforeUnload)");
    pw.println("      oldRet = oldOnBeforeUnload();");
    pw.println();
    pw.println("    if (ret !== null)");
    pw.println("      return ret;");
    pw.println("    return oldRet;");
    pw.println("  };");
    pw.println();
    pw.println("  var oldOnUnload = window.onunload;");
    pw.println("  window.onunload = function() {");
    pw.println("    unload();");
    pw.println("    if (oldOnUnload)");
    pw.println("      oldOnUnload();");
    pw.println("  };");
    pw.println("}");
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

    pw.println("function __gwt_injectExternalFiles() {");

    for (Iterator iter = styles.iterator(); iter.hasNext();) {
      String src = (String) iter.next();
      printDocumentWrite(pw, "  ", "<link rel='stylesheet' href='" + src + "'>");
    }

    for (Iterator iter = scripts.iterator(); iter.hasNext();) {
      Script script = (Script) iter.next();
      printDocumentWrite(pw, "  ", "<script language='javascript' src='"
          + script.getSrc() + "'></script>");
    }

    pw.println("}");
  }

  /**
   * Determines whether or not a particular property value is allowed.
   * 
   * @param wnd the caller's window object (not $wnd!)
   * @param propName the name of the property being checked
   * @param propValue the property value being tested
   */
  private void genIsKnownPropertyValue(PrintWriter pw) {
    pw.println("function __gwt_isKnownPropertyValue(wnd, propName, propValue) {");
    pw.println("  return propValue in wnd['values$' + propName];");
    pw.println("}");
  }

  private void genOnBadLoad(PrintWriter pw) {
    pw.println("function __gwt_onBadLoad(moduleName) {");
    pw.println("  if (__gwt_onLoadError) {");
    pw.println("    __gwt_onLoadError(moduleName);");
    pw.println("    return;");
    pw.println("  } else {");
    pw.println("    alert('Failed to load module \\\"' + moduleName + ");
    pw.println("      '\\\".\\nPlease see the log in the development shell for details.');");
    pw.println("  }");
    pw.println("}");
    pw.println();
  }

  private void genOnBadProperty(PrintWriter pw) {
    pw.println();
    pw.println("function __gwt_onBadProperty(moduleName, propName, allowedValues, badValue) {");
    pw.println("  if (__gwt_onPropertyError) {");
    pw.println("    __gwt_onPropertyError(moduleName, propName, allowedValues, badValue);");
    pw.println("    return;");
    pw.println("  } else {");
    pw.println("    var msg = 'While attempting to load module \\\"' + moduleName + '\\\", ';");
    pw.println("    if (badValue != null) {");
    pw.println("      msg += 'property \\\"' + propName + '\\\" was set to the unexpected value \\\"'");
    pw.println("        + badValue + '\\\"';");
    pw.println("    } else {");
    pw.println("      msg += 'property \\\"' + propName + '\\\" was not specified';");
    pw.println("    }");
    pw.println();
    pw.println("    msg += 'Allowed values: ' + allowedValues;");
    pw.println();
    pw.println("    alert(msg);");
    pw.println("  }");
    pw.println("}");
    pw.println();
  }

  private void genOnLoadFunctions(PrintWriter pw) {
    pw.println("function __gwt_onInjectionDone(moduleName) {");
    pw.println("  var iframe = document.getElementById(moduleName);");
    pw.println("  window.__gwt_scriptsDone = true;");
    pw.println("  if (window.__gwt_loadDone)");
    pw.println("    iframe.contentWindow.gwtOnLoad(function() { __gwt_onBadLoad(moduleName) }, moduleName);");
    pw.println("}");
    pw.println();
    pw.println("function __gwt_onScriptLoad(wnd, moduleName) {");
    pw.println("  if (window.external && window.external.gwtOnLoad) {");
    pw.println("    if (!window.external.gwtOnLoad(wnd, moduleName)) {");
    pw.println("      if (__gwt_onLoadError) {");
    pw.println("        __gwt_onLoadError(name);");
    pw.println("      } else {");
    pw.println("        alert('Failed to load module " + moduleName
        + ". Please see the log in the development shell for details.');");
    pw.println("      }");
    pw.println("    }");
    pw.println("    return;");
    pw.println("  }");
    pw.println("  var iframe = document.getElementById(moduleName);");
    pw.println("  window.__gwt_loadDone = true;");
    pw.println("  if (window.__gwt_scriptsDone)");
    pw.println("    iframe.contentWindow.gwtOnLoad(function() { __gwt_onBadLoad(moduleName) }, moduleName);");
    pw.println("}");
    pw.println();
  }

  private void genProcessMetas(PrintWriter pw) {
    pw.println("function __gwt_processMetas() {");
    pw.println("  if (!!window.__gwt_metaProps) {");
    pw.println("    return;");
    pw.println("  }");
    pw.println("  window.__gwt_metaProps = {};");
    pw.println();
    pw.println("  var metas = document.getElementsByTagName('meta');");
    pw.println();
    pw.println("  for (var i = 0, n = metas.length; i < n; ++i) {");
    pw.println("    var meta = metas[i];");
    pw.println("    var name = meta.getAttribute('name');");
    pw.println();
    pw.println("    if (name) {");
    pw.println("      if (name == 'gwt:property') {");
    pw.println("        var content = meta.getAttribute('content');");
    pw.println("        if (content) {");
    pw.println("          var name = content, value = '';");
    pw.println("          var eq = content.indexOf('=');");
    pw.println("          if (eq != -1) {");
    pw.println("            name = content.substring(0, eq);");
    pw.println("            value = content.substring(eq+1);");
    pw.println("          }");
    pw.println("          __gwt_metaProps[name] = value;");
    pw.println("        }");
    pw.println("      } else if (name == 'gwt:onPropertyErrorFn') {");
    pw.println("        var content = meta.getAttribute('content');");
    pw.println("        if (content) {");
    pw.println("          try {");
    pw.println("            __gwt_onPropertyError = eval(content);");
    pw.println("          } catch (e) {");
    pw.println("            alert('Bad handler \\\"' + content +");
    pw.println("              '\\\" for \\\"gwt:onPropertyErrorFn\\\"');");
    pw.println("          }");
    pw.println("        }");
    pw.println("      } else if (name == 'gwt:onLoadErrorFn') {");
    pw.println("        var content = meta.getAttribute('content');");
    pw.println("        if (content) {");
    pw.println("          try {");
    pw.println("            __gwt_onLoadError = eval(content);");
    pw.println("          } catch (e) {");
    pw.println("            alert('Bad handler \\\"' + content +");
    pw.println("              '\\\" for \\\"gwt:onLoadErrorFn\\\"');");
    pw.println("          }");
    pw.println("        }");
    pw.println("      }");
    pw.println("    }");
    pw.println("  }");
    pw.println("}");
    pw.println();
  }

  private void genPropProviders(PrintWriter pw) {
    pw.println();

    for (Iterator iter = moduleProps.iterator(); iter.hasNext();) {
      Property prop = (Property) iter.next();
      String activeValue = prop.getActiveValue();
      if (activeValue == null) {
        // Emit a provider function, defined by the user in module config.
        pw.println();
        PropertyProvider provider = prop.getProvider();
        assert (provider != null) : "expecting a default property provider to have been set";
        String js = Jsni.generateJavaScript(provider.getBody());
        pw.print("window[\"provider$" + prop.getName() + "\"] = function() ");
        pw.print(js);
        pw.println(";");

        // Emit a map of allowed property values as an object literal.
        pw.println();
        pw.println("window[\"values$" + prop.getName() + "\"] = {");
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
        pw.println("window[\"prop$" + prop.getName() + "\"] = function() {");
        pw.println("  var v = window[\"provider$" + prop.getName() + "\"]();");
        pw.println("  var ok = window[\"values$" + prop.getName() + "\"];");
        // Make sure this is an allowed value; if so, return.
        pw.println("  if (v in ok)");
        pw.println("    return v;");
        // Not an allowed value, so build a nice message and call the handler.
        pw.println("  var a = new Array(" + knownValues.length + ");");
        pw.println("  for (var k in ok)");
        pw.println("    a[ok[k]] = k;");
        pw.print("  __gwt_onBadProperty(");
        pw.print(literal(moduleName));
        pw.print(", ");
        pw.print(literal(prop.getName()));
        pw.println(", a, v);");
        pw.println("  if (arguments.length > 0) throw null; else return null;");
        pw.println("};");
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
        pw.print("(F=window[\"prop$" + prop.getName() + "\"],F(1))");
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
    // Emit well-known global variables.
    pw.println("var __gwt_onLoadError = null;");

    // Emit __gwt_initHandlers().
    genInitHandlers(pw);

    // Emit property providers.
    genPropProviders(pw);

    genGetMetaProperty(pw);
    genIsKnownPropertyValue(pw);
    genOnBadProperty(pw);
    genOnBadLoad(pw);
    genProcessMetas(pw);
    genOnLoadFunctions(pw);

    // Emit dynamic file injection logic.
    genInjectExternalFiles(pw);
    pw.println();

    // If the ordered props are specified, then we're generating for both modes.
    if (orderedProps != null) {
      // Web mode or hosted mode.
      if (orderedProps.length > 0) {
        pw.println();
        genAnswerFunction(pw);
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
      pw.println("function __gwt_go() {");
      pw.println("  __gwt_processMetas();");
      printDocumentWrite(pw, "  ", "<iframe id='" + moduleName
          + "' style='width:0;height:0;border:0' src='" + moduleName
          + ".cache.html'></iframe>");
    }

    pw.println();
    printDocumentWrite(pw, "  ", "<script>__gwt_onInjectionDone('" + moduleName
        + "')</script>");
    pw.println("}");

    pw.println();
    if (orderedProps != null) {
      // Web mode.
      if (hasExternalFiles()) {
        pw.println("__gwt_injectExternalFiles();");
      }
      pw.println("__gwt_go();");
    } else {
      // Hosted mode: test to see if we're actually running in web mode so we
      // can swap out to the compiled web mode script.
      pw.println("if (!window.external || !window.external.gwtOnLoad) {");
      pw.println("  document.write('<script src=\"" + moduleName
          + ".js?compiled\"></script>');");
      pw.println("} else {");
      if (hasExternalFiles()) {
        pw.println("  __gwt_injectExternalFiles();");
      }
      pw.println("  __gwt_go();");
      pw.println("}");
    }
  }

  /**
   * @param pw generate source onto this writer
   * @param oneAndOnlyStrongName if <code>null</code>, use the normal logic;
   *          otherwise, there are no client properties and thus there is
   *          exactly one permutation, specified by this parameter
   */
  private void genSrcSetFunction(PrintWriter pw, String oneAndOnlyStrongName) {
    pw.println("function __gwt_go() {");
    pw.println("  try {");
    pw.println("    __gwt_processMetas();");
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
      pw.println("    var newUrl = strongName + '.cache.html' + query;");
      pw.println("    document.write('<iframe id=\""
          + moduleName
          + "\" style=\"width:0;height:0;border:0\" src=\"\' + strongName + \'.cache.html\"></iframe>');");
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
    payLoad = payLoad.replace("'", "\\'");
    pw.println(prefix + "document.write('" + payLoad + "');");
  }
}
