package com.google.gwt.dev.util;

public class SelectionScriptTemplate {

  public static String answerFunction() {
    return "  // Deferred-binding mapper function.\n" + "  //\n"
        + "  function O(a,v) {\n" + "    var answer = O.answers;\n"
        + "    var i = -1;\n" + "    var n = a.length - 1;\n"
        + "    while (++i < n) {\n" + "      if (!(a[i] in answer)) {\n"
        + "        answer[a[i]] = [];\n" + "      }\n"
        + "      answer = answer[a[i]];\n" + "    }\n"
        + "    answer[a[n]] = v;\n" + "  }\n" + "  O.answers = [];\n" + "\n";
  }

  public static String cssInjector(String cssUrl) {
    return "  if (!__gwt_stylesLoaded['" + cssUrl + "']) {\n"
        + "    __gwt_stylesLoaded['" + cssUrl + "'] = true;\n"
        + "    document.write('<link rel=\\\"stylesheet\\\" href=\\\"" + cssUrl
        + "\\\">');\n" + "  }\n";
  }

  public static String fixedHeader() {
    return "if (!window.__gwt_stylesLoaded) { window.__gwt_stylesLoaded = {}; }\n"
        + "if (!window.__gwt_scriptsLoaded) { window.__gwt_scriptsLoaded = {}; }\n"
        + "\n"

        + "__gwt_getMetaProperty = function(name) {\n"
        + "  var value = __gwt_metaProps[name];\n"
        + "  if (value) {\n"
        + "    return value;\n"
        + "  } else {\n"
        + "    return null;\n"
        + "  }\n"
        + "}\n"
        + "\n"
        +

        "// Called from compiled code to hook the window's resize & load events (the\n"
        + "// code running in the script frame is not allowed to hook these directly).\n"
        + "//\n"
        + "function __gwt_initHandlers(resize, beforeunload, unload) {\n"
        + "  var oldOnResize = window.onresize;\n"
        + "  window.onresize = function() {\n"
        + "    resize();\n"
        + "    if (oldOnResize)\n"
        + "      oldOnResize();\n"
        + "  };\n"
        + "\n"
        + "  var oldOnBeforeUnload = window.onbeforeunload;\n"
        + "  window.onbeforeunload = function() {\n"
        + "    var ret = beforeunload();\n"
        + "\n"
        + "    var oldRet;\n"
        + "    if (oldOnBeforeUnload)\n"
        + "      oldRet = oldOnBeforeUnload();\n"
        + "\n"
        + "    if (ret !== null)\n"
        + "      return ret;\n"
        + "    return oldRet;\n"
        + "  };\n"
        + "\n"
        + "  var oldOnUnload = window.onunload;\n"
        + "  window.onunload = function() {\n"
        + "    unload();\n"
        + "    if (oldOnUnload)\n"
        + "      oldOnUnload();\n"
        + "  };\n"
        + "}\n" + "\n";
  }

  public static String moduleFunctionHeader() {
    return "  // These variables contain deferred-binding properties, values, and\n"
        + "  // providers.\n"
        + "  //\n"
        + "  var props = [];\n"
        + "  var values = [];\n" + "  var providers = [];\n";
  }

  public static String outerFunctions(String moduleName) {
    String moduleFunction = moduleName.replace('.', '_');

    return "// This is global because it is needed by onScriptLoad().\n"
        + "//\n"
        + moduleFunction
        + ".isHostedMode = function() {\n"
        + "  return (window.external && window.external.gwtOnLoad &&\n"
        + "    (document.location.href.indexOf('gwt.hybrid') == -1));\n"
        + "}\n"
        + "\n"
        +

        "// Called by both onScriptLoad() and onInjectionDone(). It causes\n"
        + "// the specified module to be cranked up.\n"
        + "//\n"
        + moduleFunction
        + ".maybeStartModule = function() {\n"
        + "  if ("
        + moduleFunction
        + ".scriptsDone && "
        + moduleFunction
        + ".loadDone) {\n"
        + "    var iframe = document.getElementById('"
        + moduleName
        + "');\n"
        + "    iframe.contentWindow.gwtOnLoad(function() { "
        + moduleFunction
        + ".onBadLoad() },\n"
        + "      '"
        + moduleName
        + "');\n"
        + "  }\n"
        + "}\n"
        + "\n"
        +

        "// Called when the compiled script identified by moduleName is done loading.\n"
        + "// It needs the iframe's window object for starting hosted mode.\n"
        + "//\n"
        + moduleFunction
        + ".onScriptLoad = function(wnd) {\n"
        + "  // If we're running in hosted mode, call window.external.gwtOnLoad().\n"
        + "  if ("
        + moduleFunction
        + ".isHostedMode()) {\n"
        + "    var rc = window.external.gwtOnLoad(wnd, '"
        + moduleName
        + "');\n"
        + "    if (!rc) {\n"
        + "      if (window.__gwt_onLoadError) {\n"
        + "        window.__gwt_onLoadError(name);\n"
        + "      } else {\n"
        + "        alert('Failed to load module "
        + moduleName
        + "' +\n"
        + "          '. Please see the log in the development shell for details.');\n"
        + "      }\n"
        + "    }\n"
        + "    return;\n"
        + "  }\n"
        + "\n"
        + "  // Mark this module's script as done loading and (possibly) start the module.\n"
        + "  "
        + moduleFunction
        + ".loadDone = true;\n"
        + "  "
        + moduleFunction
        + ".maybeStartModule();\n"
        + "}\n"
        + "\n"
        +

        "// Called when the script injection is complete.\n"
        + "//\n"
        + moduleFunction
        + ".onInjectionDone = function() {\n"
        + "  // Mark this module's script injection as done and (possibly) start the\n"
        + "  // module.\n"
        + "  "
        + moduleFunction
        + ".scriptsDone = true;\n"
        + "  "
        + moduleFunction
        + ".maybeStartModule();\n"
        + "}\n"
        + "\n"
        +

        "// Called to slurp up all <meta> tags:\n"
        + "//  gwt:property, gwt:base, gwt:onPropertyErrorFn, gwt:onLoadErrorFn\n"
        + "//\n"
        + moduleFunction
        + ".processMetas = function() {\n"
        + "  if (!!window.__gwt_metaProps) {\n"
        + "    return;\n"
        + "  }\n"
        + "  window.__gwt_metaProps = {};\n"
        + "\n"
        + "  var metas = document.getElementsByTagName('meta');\n"
        + "\n"
        + "  for (var i = 0, n = metas.length; i < n; ++i) {\n"
        + "    var meta = metas[i];\n"
        + "    var name = meta.getAttribute('name');\n"
        + "\n"
        + "    if (name) {\n"
        + "      if (name == 'gwt:property') {\n"
        + "        var content = meta.getAttribute('content');\n"
        + "        if (content) {\n"
        + "          var name = content, value = '';\n"
        + "          var eq = content.indexOf('=');\n"
        + "          if (eq != -1) {\n"
        + "            name = content.substring(0, eq);\n"
        + "            value = content.substring(eq+1);\n"
        + "          }\n"
        + "          __gwt_metaProps[name] = value;\n"
        + "        }\n"
        + "      } else if (name == 'gwt:onPropertyErrorFn') {\n"
        + "        var content = meta.getAttribute('content');\n"
        + "        if (content) {\n"
        + "          try {\n"
        + "            window.__gwt_onPropertyError = eval(content);\n"
        + "          } catch (e) {\n"
        + "            alert('Bad handler \\\"' + content +\n"
        + "              '\\\" for \\\"gwt:onPropertyErrorFn\\\"');\n"
        + "          }\n"
        + "        }\n"
        + "      } else if (name == 'gwt:onLoadErrorFn') {\n"
        + "        var content = meta.getAttribute('content');\n"
        + "        if (content) {\n"
        + "          try {\n"
        + "            window.__gwt_onLoadError = eval(content);\n"
        + "          } catch (e) {\n"
        + "            alert('Bad handler \\\"' + content + '\\\" for \\\"gwt:onLoadErrorFn\\\"');\n"
        + "          }\n"
        + "        }\n"
        + "      } else if (name == 'gwt:base') {\n"
        + "        var content = meta.getAttribute('content');\n"
        + "        var eqPos = content.lastIndexOf('=');\n"
        + "        if (eqPos == -1) {\n"
        + "          continue;\n"
        + "        }\n"
        + "        var moduleBase = content.substring(0, eqPos);\n"
        + "        var moduleName = content.substring(eqPos + 1);\n"
        + "        if (!window.__gwt_base) {\n"
        + "          window.__gwt_base = [];\n"
        + "        }\n"
        + "        window.__gwt_base[moduleName] = moduleBase;\n"
        + "      }\n"
        + "    }\n"
        + "  }\n"
        + "}\n"
        + "\n"
        +

        "// Called whenever a bad property is provided.\n"
        + "//\n"
        + moduleFunction
        + ".onBadProperty = function(propName, allowedValues, badValue) {\n"
        + "  if (window.__gwt_onPropertyError) {\n"
        + "    "
        + moduleFunction
        + ".onPropertyError(propName, allowedValues, badValue);\n"
        + "    return;\n"
        + "  } else {\n"
        + "    var msg = 'While attempting to load module "
        + moduleName
        + ", ';\n"
        + "    if (badValue != null) {\n"
        + "      msg += 'property \\\"' + propName + '\\\" was set to the unexpected value \\\"'\n"
        + "        + badValue + '\\\"';\n"
        + "    } else {\n"
        + "      msg += 'property \\\"' + propName + '\\\" was not specified';\n"
        + "    }\n"
        + "\n"
        + "    msg += 'Allowed values: ' + allowedValues;\n"
        + "\n"
        + "    alert(msg);\n"
        + "  }\n"
        + "}\n"
        + "\n"
        +

        "// Called if the module fails to load.\n"
        + "//\n"
        + moduleFunction
        + ".onBadLoad = function() {\n"
        + "  if (window.__gwt_onLoadError) {\n"
        + "    window.__gwt_onLoadError('"
        + moduleName
        + "');\n"
        + "    return;\n"
        + "  } else {\n"
        + "    alert('Failed to load module "
        + moduleName
        + "' +\n"
        + "      '\".\\nPlease see the log in the development shell for details.');\n"
        + "  }\n" + "}\n" + "\n";
  }

  public static String scriptInjector(String scriptUrl) {
    return "  if (!__gwt_scriptsLoaded['" + scriptUrl + "']) {\n"
        + "    __gwt_scriptsLoaded['" + scriptUrl + "'] = true;\n"
        + "    document.write('<script language=\\\"javascript\\\" src=\\\""
        + scriptUrl + "\\\"></script>');\n" + "  }\n";
  }
}
