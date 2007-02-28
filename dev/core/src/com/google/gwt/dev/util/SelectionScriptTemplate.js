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
if (!window.__gwt_stylesLoaded) { window.__gwt_stylesLoaded = {}; }
if (!window.__gwt_scriptsLoaded) { window.__gwt_scriptsLoaded = {}; }

// Called from compiled code to hook the window's resize & load events (the
// code running in the script frame is not allowed to hook these directly).
//
function __gwt_initHandlers(resize, beforeunload, unload) {
  var oldOnResize = window.onresize;
  window.onresize = function() {
    resize();
    if (oldOnResize)
      oldOnResize();
  };

  var oldOnBeforeUnload = window.onbeforeunload;
  window.onbeforeunload = function() {
    var ret = beforeunload();

    var oldRet;
    if (oldOnBeforeUnload)
      oldRet = oldOnBeforeUnload();

    if (ret !== null)
      return ret;
    return oldRet;
  };

  var oldOnUnload = window.onunload;
  window.onunload = function() {
    unload();
    if (oldOnUnload)
      oldOnUnload();
  };
}

function __MODULE_FUNC__() {
  // These variables contain deferred-binding properties, values, and
  // providers.
  //
  var props = [];
  var values = [];
  var providers = [];

// __SHELL_SERVLET_ONLY_BEGIN__
  // Forces shell servlet to serve compiled output for web mode
  if (!__MODULE_FUNC__.isHostedMode()) {
    document.write('<script src="__MODULE_NAME__.nocache.js?compiled"></script>');
    return;
  }
// __SHELL_SERVLET_ONLY_END__

// __PROPERTIES_BEGIN__
  // Property providers and values
// __PROPERTIES_END__

  __MODULE_FUNC__.processMetas();

  var strongName;
  if (__MODULE_FUNC__.isHostedMode()) {
    // In hosted mode, inject the script frame directly.
    var iframe = document.createElement('iframe');
    document.body.appendChild(iframe);
    iframe.style.width = iframe.style.height = iframe.style.border = '0px';
    var wnd = iframe.contentWindow;
    wnd.$wnd = window;
    wnd.$doc = window.document;
    wnd.__gwt_getProperty = function(name) { return providers[name](); };
    wnd.onunload = function() { alert('unload!'); };
    __MODULE_FUNC__.onScriptLoad(wnd);
  } else {
    // Deferred-binding mapper function.
    //
    function O(a,v) {
      var answer = O.answers;
      var i = -1;
      var n = a.length - 1;
      while (++i < n) {
        if (!(a[i] in answer)) {
          answer[a[i]] = [];
        }
        answer = answer[a[i]];
      }
      answer[a[n]] = v;
    }
    O.answers = [];

    try {
// __PERMUTATIONS_BEGIN__
      // Permutation logic
// __PERMUTATIONS_END__
    } catch (e) {
      // intentionally silent on property failure
      return;
    }  

	  // TODO: do we still need this query stuff?
	  var query = location.search;
	  query = query.substring(0, query.indexOf('&'));

	  var base;
	  if (window.__gwt_base) {
	    base = __gwt_base['__MODULE_NAME__'];
	  }
	  var newUrl = (base ? base + '/' : '') + strongName + '.cache.html' + query;
	  document.write('<iframe id="__MODULE_NAME__" style="width:0;height:0;border:0" src="' + newUrl + '"></iframe>');
  }

// __MODULE_DEPS_BEGIN__
  // Module dependencies, such as scripts and css
// __MODULE_DEPS_END__
  document.write('<script>__MODULE_FUNC__.onInjectionDone(\'__MODULE_NAME__\')</script>');
}

__MODULE_FUNC__.getMetaProperty = function(name) {
  var value = __MODULE_FUNC__.metaProps[name];
  if (value) {
    return value;
  } else {
    return null;
  }
}

// This is global because it is needed by onScriptLoad().
//
__MODULE_FUNC__.isHostedMode = function() {
  return (window.external && window.external.gwtOnLoad &&
    (document.location.href.indexOf('gwt.hybrid') == -1));
}

// Called by both onScriptLoad() and onInjectionDone(). It causes
// the specified module to be cranked up.
//
__MODULE_FUNC__.maybeStartModule = function() {
  if (__MODULE_FUNC__.scriptsDone && __MODULE_FUNC__.loadDone) {
    var iframe = document.getElementById('__MODULE_NAME__');
    iframe.contentWindow.gwtOnLoad(function() { __MODULE_FUNC__.onBadLoad() },
      '__MODULE_NAME__');
  }
}

// Called when the compiled script identified by moduleName is done loading.
// It needs the iframe's window object for starting hosted mode.
//
__MODULE_FUNC__.onScriptLoad = function(wnd) {
  // If we're running in hosted mode, call window.external.gwtOnLoad().
  if (__MODULE_FUNC__.isHostedMode()) {
    var rc = window.external.gwtOnLoad(wnd, '__MODULE_NAME__');
    if (!rc) {
      if (window.__gwt_onLoadError) {
        window.__gwt_onLoadError(name);
      } else {
        alert('Failed to load module __MODULE_NAME__' +
          '. Please see the log in the development shell for details.');
      }
    }
    return;
  }

  // Mark this module's script as done loading and (possibly) start the module.
  __MODULE_FUNC__.loadDone = true;
  __MODULE_FUNC__.maybeStartModule();
}

// Called when the script injection is complete.
//
__MODULE_FUNC__.onInjectionDone = function() {
  // Mark this module's script injection as done and (possibly) start the
  // module.
  __MODULE_FUNC__.scriptsDone = true;
  __MODULE_FUNC__.maybeStartModule();
}

// Called to slurp up all <meta> tags:
// gwt:property, gwt:base, gwt:onPropertyErrorFn, gwt:onLoadErrorFn
//
__MODULE_FUNC__.processMetas = function() {
  __MODULE_FUNC__.metaProps = {};

  var metas = document.getElementsByTagName('meta');

  for (var i = 0, n = metas.length; i < n; ++i) {
    var meta = metas[i];
    var name = meta.getAttribute('name');

    if (name) {
      if (name == 'gwt:property') {
        var content = meta.getAttribute('content');
        if (content) {
          var name = content, value = '';
          var eq = content.indexOf('=');
          if (eq != -1) {
            name = content.substring(0, eq);
            value = content.substring(eq+1);
          }
          __MODULE_FUNC__.metaProps[name] = value;
        }
      } else if (name == 'gwt:onPropertyErrorFn') {
        var content = meta.getAttribute('content');
        if (content) {
          try {
            window.__gwt_onPropertyError = eval(content);
          } catch (e) {
            alert('Bad handler \"' + content +
              '\" for \"gwt:onPropertyErrorFn\"');
          }
        }
      } else if (name == 'gwt:onLoadErrorFn') {
        var content = meta.getAttribute('content');
        if (content) {
          try {
            window.__gwt_onLoadError = eval(content);
          } catch (e) {
            alert('Bad handler \"' + content + '\" for \"gwt:onLoadErrorFn\"');
          }
        }
      } else if (name == 'gwt:base') {
        var content = meta.getAttribute('content');
        var eqPos = content.lastIndexOf('=');
        if (eqPos == -1) {
          continue;
        }
        var moduleBase = content.substring(0, eqPos);
        var moduleName = content.substring(eqPos + 1);
        if (!window.__gwt_base) {
          window.__gwt_base = [];
        }
        window.__gwt_base[moduleName] = moduleBase;
      }
    }
  }
}

// Called whenever a bad property is provided.
//
__MODULE_FUNC__.onBadProperty = function(propName, allowedValues, badValue) {
  if (window.__gwt_onPropertyError) {
    __MODULE_FUNC__.onPropertyError(propName, allowedValues, badValue);
    return;
  } else {
    var msg = 'While attempting to load module __MODULE_NAME__, ';
    if (badValue != null) {
      msg += 'property \"' + propName + '\" was set to the unexpected value \"'
        + badValue + '\"';
    } else {
      msg += 'property \"' + propName + '\" was not specified';
    }

    msg += 'Allowed values: ' + allowedValues;

    alert(msg);
  }
}

// Called if the module fails to load.
//
__MODULE_FUNC__.onBadLoad = function() {
  if (window.__gwt_onLoadError) {
    window.__gwt_onLoadError('__MODULE_NAME__');
    return;
  } else {
    alert('Failed to load module __MODULE_NAME__' +
      '".\nPlease see the log in the development shell for details.');
  }
}

__MODULE_FUNC__();
