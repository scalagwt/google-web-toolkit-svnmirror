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
package com.google.gwt.dev.shell.jetty;

import com.google.gwt.core.ext.ServletContainer;
import com.google.gwt.core.ext.ServletContainerLauncher;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.dev.util.InstalledHelpInfo;

import org.mortbay.component.AbstractLifeCycle;
import org.mortbay.jetty.AbstractConnector;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.RequestLog;
import org.mortbay.jetty.Response;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.RequestLogHandler;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppClassLoader;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.log.Log;
import org.mortbay.log.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

/**
 * A {@link ServletContainerLauncher} for an embedded Jetty server.
 */
public class JettyLauncher extends ServletContainerLauncher {

  /**
   * Log jetty requests/responses to TreeLogger.
   */
  public static class JettyRequestLogger extends AbstractLifeCycle implements
      RequestLog {

    private final TreeLogger logger;

    public JettyRequestLogger(TreeLogger logger) {
      this.logger = logger;
    }

    /**
     * Log an HTTP request/response to TreeLogger.
     */
    @SuppressWarnings("unchecked")
    public void log(Request request, Response response) {
      int status = response.getStatus();
      if (status < 0) {
        // Copied from NCSARequestLog
        status = 404;
      }
      TreeLogger.Type logStatus, logHeaders;
      if (status >= 500) {
        logStatus = TreeLogger.ERROR;
        logHeaders = TreeLogger.INFO;
      } else if (status >= 400) {
        logStatus = TreeLogger.WARN;
        logHeaders = TreeLogger.INFO;
      } else {
        logStatus = TreeLogger.INFO;
        logHeaders = TreeLogger.DEBUG;
      }
      String userString = request.getRemoteUser();
      if (userString == null) {
        userString = "";
      } else {
        userString += "@";
      }
      String bytesString = "";
      if (response.getContentCount() > 0) {
        bytesString = " " + response.getContentCount() + " bytes";
      }
      TreeLogger branch = logger.branch(logStatus, String.valueOf(status)
          + " - " + request.getMethod() + ' ' + request.getUri() + " ("
          + userString + request.getRemoteHost() + ')' + bytesString);
      TreeLogger headers = branch.branch(logHeaders, "Request headers");
      Enumeration<String> headerNames = request.getHeaderNames();
      while (headerNames.hasMoreElements()) {
        String hdr = headerNames.nextElement();
        String hdrVal = request.getHeader(hdr);
        headers.log(logHeaders, hdr + ": " + hdrVal);
      }
      // TODO(jat): add response headers
    }
  }

  /**
   * An adapter for the Jetty logging system to GWT's TreeLogger. This
   * implementation class is only public to allow {@link Log} to instantiate it.
   * 
   * The weird static data / default construction setup is a game we play with
   * {@link Log}'s static initializer to prevent the initial log message from
   * going to stderr.
   */
  public static class JettyTreeLogger implements Logger {
    private final TreeLogger logger;

    public JettyTreeLogger(TreeLogger logger) {
      if (logger == null) {
        throw new NullPointerException();
      }
      this.logger = logger;
    }

    public void debug(String msg, Object arg0, Object arg1) {
      logger.log(TreeLogger.SPAM, format(msg, arg0, arg1));
    }

    public void debug(String msg, Throwable th) {
      logger.log(TreeLogger.SPAM, msg, th);
    }

    public Logger getLogger(String name) {
      return this;
    }

    public void info(String msg, Object arg0, Object arg1) {
      logger.log(TreeLogger.INFO, format(msg, arg0, arg1));
    }

    public boolean isDebugEnabled() {
      return logger.isLoggable(TreeLogger.SPAM);
    }

    public void setDebugEnabled(boolean enabled) {
      // ignored
    }

    public void warn(String msg, Object arg0, Object arg1) {
      logger.log(TreeLogger.WARN, format(msg, arg0, arg1));
    }

    public void warn(String msg, Throwable th) {
      logger.log(TreeLogger.WARN, msg, th);
    }

    /**
     * Copied from org.mortbay.log.StdErrLog.
     */
    private String format(String msg, Object arg0, Object arg1) {
      int i0 = msg.indexOf("{}");
      int i1 = i0 < 0 ? -1 : msg.indexOf("{}", i0 + 2);

      if (arg1 != null && i1 >= 0) {
        msg = msg.substring(0, i1) + arg1 + msg.substring(i1 + 2);
      }
      if (arg0 != null && i0 >= 0) {
        msg = msg.substring(0, i0) + arg0 + msg.substring(i0 + 2);
      }
      return msg;
    }
  }

  /**
   * The resulting {@link ServletContainer} this is launched.
   */
  protected static class JettyServletContainer extends ServletContainer {
    private final int actualPort;
    private final File appRootDir;
    private final TreeLogger logger;
    private final Server server;
    private final WebAppContext wac;

    public JettyServletContainer(TreeLogger logger, Server server,
        WebAppContext wac, int actualPort, File appRootDir) {
      this.logger = logger;
      this.server = server;
      this.wac = wac;
      this.actualPort = actualPort;
      this.appRootDir = appRootDir;
    }

    public int getPort() {
      return actualPort;
    }

    public void refresh() throws UnableToCompleteException {
      String msg = "Reloading web app to reflect changes in "
          + appRootDir.getAbsolutePath();
      TreeLogger branch = logger.branch(TreeLogger.INFO, msg);
      // Temporarily log Jetty on the branch.
      Log.setLog(new JettyTreeLogger(branch));
      try {
        wac.stop();
        wac.start();
        branch.log(TreeLogger.INFO, "Reload completed successfully");
      } catch (Exception e) {
        branch.log(TreeLogger.ERROR, "Unable to restart embedded Jetty server",
            e);
        throw new UnableToCompleteException();
      } finally {
        // Reset the top-level logger.
        Log.setLog(new JettyTreeLogger(logger));
      }
    }

    public void stop() throws UnableToCompleteException {
      TreeLogger branch = logger.branch(TreeLogger.INFO,
          "Stopping Jetty server");
      // Temporarily log Jetty on the branch.
      Log.setLog(new JettyTreeLogger(branch));
      try {
        server.stop();
        server.setStopAtShutdown(false);
        branch.log(TreeLogger.INFO, "Stopped successfully");
      } catch (Exception e) {
        branch.log(TreeLogger.ERROR, "Unable to stop embedded Jetty server", e);
        throw new UnableToCompleteException();
      } finally {
        // Reset the top-level logger.
        Log.setLog(new JettyTreeLogger(logger));
      }
    }
  }

  /**
   * A {@link WebAppContext} tailored to GWT hosted mode. Features hot-reload
   * with a new {@link WebAppClassLoader} to pick up disk changes. The default
   * Jetty {@code WebAppContext} will create new instances of servlets, but it
   * will not create a brand new {@link ClassLoader}. By creating a new
   * {@code ClassLoader} each time, we re-read updated classes from disk.
   * 
   * Also provides special class filtering to isolate the web app from the GWT
   * hosting environment.
   */
  protected final class WebAppContextWithReload extends WebAppContext {

    /**
     * Specialized {@link WebAppClassLoader} that allows outside resources to be
     * brought in dynamically from the system path. A warning is issued when
     * this occurs.
     */
    private class WebAppClassLoaderExtension extends WebAppClassLoader {

      public WebAppClassLoaderExtension() throws IOException {
        super(bootStrapOnlyClassLoader, WebAppContextWithReload.this);
      }

      @Override
      public URL findResource(String name) {
        // Always check this ClassLoader first.
        URL found = super.findResource(name);
        if (found != null) {
          return found;
        }

        // See if the outside world has it.
        found = systemClassLoader.getResource(name);
        if (found == null) {
          return null;
        }

        // Specifically for META-INF/services/javax.xml.parsers.SAXParserFactory
        String checkName = name;
        if (checkName.startsWith("META-INF/services/")) {
          checkName = checkName.substring("META-INF/services/".length());
        }

        // For system/server path, just return it quietly.
        if (isServerPath(checkName) || isSystemPath(checkName)) {
          return found;
        }

        // Warn, add containing URL to our own ClassLoader, and retry the call.
        String warnMessage = "Server resource '"
            + name
            + "' could not be found in the web app, but was found on the system classpath";
        if (!addContainingClassPathEntry(warnMessage, found, name)) {
          return null;
        }
        return super.findResource(name);
      }

      /**
       * Override to additionally consider the most commonly available JSP and
       * XML implementation as system resources. (In fact, Jasper is in gwt-dev
       * via embedded Tomcat, so we always hit this case.)
       */
      @Override
      public boolean isSystemPath(String name) {
        name = name.replace('/', '.');
        return super.isSystemPath(name)
            || name.startsWith("org.apache.jasper.")
            || name.startsWith("org.apache.xerces.");
      }

      @Override
      protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
          return super.findClass(name);
        } catch (ClassNotFoundException e) {
        }

        // For system/server path, just try the outside world quietly.
        if (isServerPath(name) || isSystemPath(name)) {
          return systemClassLoader.loadClass(name);
        }

        // See if the outside world has a URL for it.
        String resourceName = name.replace('.', '/') + ".class";
        URL found = systemClassLoader.getResource(resourceName);
        if (found == null) {
          return null;
        }

        // Warn, add containing URL to our own ClassLoader, and retry the call.
        String warnMessage = "Server class '"
            + name
            + "' could not be found in the web app, but was found on the system classpath";
        if (!addContainingClassPathEntry(warnMessage, found, resourceName)) {
          throw new ClassNotFoundException(name);
        }
        return super.findClass(name);
      }

      private boolean addContainingClassPathEntry(String warnMessage,
          URL resource, String resourceName) {
        TreeLogger.Type logLevel = (System.getProperty(PROPERTY_NOWARN_WEBAPP_CLASSPATH) == null)
            ? TreeLogger.WARN : TreeLogger.DEBUG;
        TreeLogger branch = logger.branch(logLevel, warnMessage);
        String classPathURL;
        String foundStr = resource.toExternalForm();
        if (resource.getProtocol().equals("file")) {
          assert foundStr.endsWith(resourceName);
          classPathURL = foundStr.substring(0, foundStr.length()
              - resourceName.length());
        } else if (resource.getProtocol().equals("jar")) {
          assert foundStr.startsWith("jar:");
          assert foundStr.endsWith("!/" + resourceName);
          classPathURL = foundStr.substring(4, foundStr.length()
              - (2 + resourceName.length()));
        } else {
          branch.log(TreeLogger.ERROR,
              "Found resouce but unrecognized URL format: '" + foundStr + '\'');
          return false;
        }
        branch = branch.branch(logLevel, "Adding classpath entry '"
            + classPathURL + "' to the web app classpath for this session",
            null, new InstalledHelpInfo("webAppClassPath.html"));
        try {
          addClassPath(classPathURL);
          return true;
        } catch (IOException e) {
          branch.log(TreeLogger.ERROR, "Failed add container URL: '"
              + classPathURL + '\'', e);
          return false;
        }
      }
    }

    /**
     * Parent ClassLoader for the Jetty web app, which can only load JVM
     * classes. We would just use <code>null</code> for the parent ClassLoader
     * except this makes Jetty unhappy.
     */
    private final ClassLoader bootStrapOnlyClassLoader = new ClassLoader(null) {
    };

    private final TreeLogger logger;

    /**
     * In the usual case of launching {@link com.google.gwt.dev.HostedMode},
     * this will always by the system app ClassLoader.
     */
    private final ClassLoader systemClassLoader = Thread.currentThread().getContextClassLoader();

    @SuppressWarnings("unchecked")
    private WebAppContextWithReload(TreeLogger logger, String webApp,
        String contextPath) {
      super(webApp, contextPath);
      this.logger = logger;

      // Prevent file locking on Windows; pick up file changes.
      getInitParams().put(
          "org.mortbay.jetty.servlet.Default.useFileMappedBuffer", "false");

      // Since the parent class loader is bootstrap-only, prefer it first.
      setParentLoaderPriority(true);
    }

    @Override
    protected void doStart() throws Exception {
      setClassLoader(new WebAppClassLoaderExtension());
      super.doStart();
    }

    @Override
    protected void doStop() throws Exception {
      super.doStop();
      setClassLoader(null);
    }
  }

  /**
   * System property to suppress warnings about loading web app classes from the
   * system classpath.
   */
  private static final String PROPERTY_NOWARN_WEBAPP_CLASSPATH = "gwt.nowarn.webapp.classpath";

  static {
    // Suppress spammy Jetty log initialization.
    System.setProperty("org.mortbay.log.class", JettyNullLogger.class.getName());
    Log.getLog();
  }

  public ServletContainer start(TreeLogger logger, int port, File appRootDir)
      throws Exception {
    TreeLogger branch = logger.branch(TreeLogger.INFO,
        "Starting Jetty on port " + port, null);

    checkStartParams(branch, port, appRootDir);

    // Setup our branch logger during startup.
    Log.setLog(new JettyTreeLogger(branch));

    // Turn off XML validation.
    System.setProperty("org.mortbay.xml.XmlParser.Validating", "false");

    AbstractConnector connector = getConnector();
    connector.setPort(port);

    // Don't share ports with an existing process.
    connector.setReuseAddress(false);

    // Linux keeps the port blocked after shutdown if we don't disable this.
    connector.setSoLingerTime(0);

    Server server = new Server();
    server.addConnector(connector);

    // Create a new web app in the war directory.
    WebAppContext wac = new WebAppContextWithReload(logger,
        appRootDir.getAbsolutePath(), "/");

    RequestLogHandler logHandler = new RequestLogHandler();
    logHandler.setRequestLog(new JettyRequestLogger(logger));
    logHandler.setHandler(wac);
    server.setHandler(logHandler);
    server.start();
    server.setStopAtShutdown(true);

    // Now that we're started, log to the top level logger.
    Log.setLog(new JettyTreeLogger(logger));

    return new JettyServletContainer(logger, server, wac,
        connector.getLocalPort(), appRootDir);
  }

  protected AbstractConnector getConnector() {
    return new SelectChannelConnector();
  }

  private void checkStartParams(TreeLogger logger, int port, File appRootDir) {
    if (logger == null) {
      throw new NullPointerException("logger cannot be null");
    }

    if (port < 0 || port > 65535) {
      throw new IllegalArgumentException(
          "port must be either 0 (for auto) or less than 65536");
    }

    if (appRootDir == null) {
      throw new NullPointerException("app root direcotry cannot be null");
    }
  }

}
