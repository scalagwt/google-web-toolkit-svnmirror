/*
 * Copyright 2009 Google Inc.
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
package com.google.gwt.junit;

import com.gargoylesoftware.htmlunit.javascript.host.Window;

import net.sourceforge.htmlunit.corejs.javascript.Context;
import net.sourceforge.htmlunit.corejs.javascript.Function;
import net.sourceforge.htmlunit.corejs.javascript.Scriptable;
import net.sourceforge.htmlunit.corejs.javascript.ScriptableObject;

/**
 * HTMLUnit object that represents the hosted-mode plugin.
 */
public class HostedModePluginObject extends ScriptableObject {

  /**
   * Function object which implements the connect method on the hosted-mode
   * plugin.
   */
  private class ConnectMethod extends ScriptableObject implements Function {

    private static final long serialVersionUID = -8799481412144205779L;

    public Object call(Context context, Scriptable scope, Scriptable thisObj,
        Object[] args) {
      // Allow extra arguments for forward compatibility
      if (args.length < 3) {
        throw Context.reportRuntimeError("Bad number of parameters for function"
            + " connect: expected 3, got " + args.length);
      }
      try {
        return connect((String) args[0], (String) args[1], (Window) args[2]);
      } catch (ClassCastException e) {
        throw Context.reportRuntimeError("Incorrect parameter types for "
            + " connect: expected String/String/Window");
      }
    }

    public Scriptable construct(Context context, Scriptable scope, Object[] args) {
      throw Context.reportRuntimeError("Function connect can't be used as a "
          + "constructor");
    }

    @Override
    public String getClassName() {
      return "function HostedModePluginObject.connect";
    }
  }

  /**
   * Function object which implements the init method on the hosted-mode
   * plugin.
   */
  private class InitMethod extends ScriptableObject implements Function {

    private static final long serialVersionUID = -8799481412144205779L;

    public Object call(Context context, Scriptable scope, Scriptable thisObj,
        Object[] args) {
      // Allow extra arguments for forward compatibility
      if (args.length < 1) {
        throw Context.reportRuntimeError("Bad number of parameters for function"
            + " init: expected 1, got " + args.length);
      }
      try {
        return init((String) args[0]);
      } catch (ClassCastException e) {
        throw Context.reportRuntimeError("Incorrect parameter types for "
            + " initt: expected String");
      }
    }

    public Scriptable construct(Context context, Scriptable scope, Object[] args) {
      throw Context.reportRuntimeError("Function init can't be used as a "
          + "constructor");
    }

    @Override
    public String getClassName() {
      return "function HostedModePluginObject.init";
    }
  }

  private static final long serialVersionUID = -1815031145376726799L;

  private Scriptable connectMethod = new ConnectMethod();
  private Scriptable initMethod = new InitMethod();
  private Window window;

  /**
   * Initiate a hosted mode connection to the requested port and load the
   * requested module.
   * 
   * @param port "host:port" or "address:port" to use for the OOPHM server
   * @param module module name to load
   * @param window $wnd for this module
   * @return true if the connection succeeds
   */
  public boolean connect(String port, String module, Window window) {
    this.window = window;
    System.err.println("connect(port=" + port + ", module=" + module
        + ", window=" + System.identityHashCode(window) + ")");
    // TODO: actually connect to the OOPHM server at port, send LoadModule msg
    return true;
  }

  @Override
  public Object get(String name, Scriptable start) {
    if ("connect".equals(name)) {
      return connectMethod;
    } else if ("init".equals(name)) {
      return initMethod;
    }
    return NOT_FOUND;
  }

  @Override
  public String getClassName() {
    return "HostedModePluginObject";
  }

  /**
   * Verify that the plugin can be initialized properly and supports the
   * requested version.
   * 
   * @param version hosted mode protocol version
   * @return true if initialization succeeds, otherwise false
   */
  public boolean init(String version) {
    return true;
  }
}