/*
 * Copyright 2010 Google Inc.
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
package com.google.gwt.requestfactory.server;

import com.google.gwt.requestfactory.shared.RequestFactory;
import com.google.gwt.user.server.rpc.RPCServletUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handles GWT RequestFactory JSON requests. Does user authentication on every
 * request, returning SC_UNAUTHORIZED if authentication fails, as well as a
 * header named "login" which contains the URL the user should be sent in to
 * login. Note that the servlet expects a "pageurl" header in the request,
 * indicating the page to redirect to after authentication. If authentication
 * succeeds, a header named "userId" is returned, which will be unique to the
 * user (so the app can react if the signed in user has changed).
 * 
 * Configured via servlet init params.
 * <p>
 * e.g. - in order to use GAE authentication:
 * 
 * <pre>  &lt;init-param>
    &lt;param-name>userInfoClass&lt;/param-name>
    &lt;param-value>com.google.gwt.sample.expenses.server.domain.GaeUserInformation&lt;/param-value>
  &lt;/init-param>

 * </pre>
 */
@SuppressWarnings("serial")
public class RequestFactoryServlet extends HttpServlet {

  private static final boolean DUMP_PAYLOAD = Boolean.getBoolean("gwt.rpc.dumpPayload");
  private static final String JSON_CHARSET = "UTF-8";
  private static final String JSON_CONTENT_TYPE = "application/json";
  private static final Logger log = Logger.getLogger(RequestFactoryServlet.class.getCanonicalName());

  /**
   * These ThreadLocals are used to allow service objects to obtain access to
   * the HTTP transaction.
   */
  private static final ThreadLocal<HttpServletRequest> perThreadRequest = new ThreadLocal<HttpServletRequest>();
  private static final ThreadLocal<HttpServletResponse> perThreadResponse = new ThreadLocal<HttpServletResponse>();

  /**
   * Returns the thread-local {@link HttpServletRequest}.
   * 
   * @return an {@link HttpServletRequest} instance
   */
  public static HttpServletRequest getThreadLocalRequest() {
    return perThreadRequest.get();
  }

  /**
   * Returns the thread-local {@link HttpServletResponse}.
   * 
   * @return an {@link HttpServletResponse} instance
   */
  public static HttpServletResponse getThreadLocalResponse() {
    return perThreadResponse.get();
  }

  private final SimpleRequestProcessor processor;

  /**
   * Constructs a new {@link RequestFactoryServlet} with a
   * {@code DefaultExceptionHandler}.
   */
  public RequestFactoryServlet() {
    this(new DefaultExceptionHandler());
  }

  /**
   * Use this constructor in subclasses to provide a custom
   * {@link ExceptionHandler}.
   * 
   * @param exceptionHandler an {@link ExceptionHandler} instance
   * @param serviceDecorators an array of ServiceLayerDecorators that change how
   *          the RequestFactory request processor interact with the domain
   *          objects
   */
  public RequestFactoryServlet(ExceptionHandler exceptionHandler,
      ServiceLayerDecorator... serviceDecorators) {
    processor = new SimpleRequestProcessor(
        ServiceLayer.create(serviceDecorators));
    processor.setExceptionHandler(exceptionHandler);
  }

  /**
   * Processes a POST to the server.
   * 
   * @param request an {@link HttpServletRequest} instance
   * @param response an {@link HttpServletResponse} instance
   * @throws IOException if an internal I/O error occurs
   * @throws ServletException if an error occurs in the servlet
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    perThreadRequest.set(request);
    perThreadResponse.set(response);

    // No new code should be placed outside of this try block.
    try {
      ensureConfig();
      String jsonRequestString = RPCServletUtils.readContent(request,
          JSON_CONTENT_TYPE, JSON_CHARSET);
      if (DUMP_PAYLOAD) {
        System.out.println(">>> " + jsonRequestString);
      }
      PrintWriter writer = response.getWriter();

      try {
        // Check that user is logged in before proceeding
        UserInformation userInfo = UserInformation.getCurrentUserInformation(request.getHeader("pageurl"));
        if (!userInfo.isUserLoggedIn()) {
          response.setHeader("login", userInfo.getLoginUrl());
          response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
          String payload = processor.process(jsonRequestString);
          if (DUMP_PAYLOAD) {
            System.out.println("<<< " + payload);
          }
          response.setHeader("userId", String.format("%s", userInfo.getId()));
          response.setStatus(HttpServletResponse.SC_OK);
          response.setContentType(RequestFactory.JSON_CONTENT_TYPE_UTF8);
          writer.print(payload);
          writer.flush();
        }
      } catch (RuntimeException e) {
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        log.log(Level.SEVERE, "Unexpected error", e);
      }
    } finally {
      perThreadRequest.set(null);
      perThreadResponse.set(null);
    }
  }

  private void ensureConfig() {
    // Instantiate a class for authentication, using either the default
    // UserInfo class, or a subclass if the web.xml specifies one. This allows
    // clients to use a Google App Engine based authentication class without
    // adding GAE dependencies to GWT.
    String userInfoClass = getServletConfig().getInitParameter("userInfoClass");
    if (userInfoClass != null) {
      UserInformation.setUserInformationImplClass(userInfoClass);
    }

    String symbolMapsDirectory = getServletConfig().getInitParameter(
        "symbolMapsDirectory");
    if (symbolMapsDirectory != null) {
      Logging.setSymbolMapsDirectory(symbolMapsDirectory);
    }
  }
}
