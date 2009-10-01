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
package com.google.gwt.sample.showcase.server;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that makes this application crawlable.
 */
public final class CrawlServlet implements Filter {

  /**
   * Special URL token that gets passed from the crawler to the servlet filter.
   * This token is used in case there are already existing query parameters.
   */
  private static final String ESCAPED_FRAGMENT_FORMAT1 = "&_escaped_fragment_=";
  /**
   * Special URL token that gets passed from the crawler to the servlet filter.
   * This token is used in case there are not already existing query parameters.
   */
  private static final String ESCAPED_FRAGMENT_FORMAT2 = "?_escaped_fragment_=";
  /**
   * Length of the special URL tokens.
   */
  private static final int ESCAPED_FRAGMENT_LENGTH = ESCAPED_FRAGMENT_FORMAT1.length();

  /**
   * Maps from the query string that contains _escaped_fragment_ to one that
   * doesn't, but is instead followed by a hash fragment. It also unescapes any
   * characters that were escaped by the crawler. If the query string does not
   * contain _escaped_fragment_, it is not modified.
   * 
   * @param queryString
   * @return A modified query string followed by a hash fragment if applicable.
   *         The non-modified query string otherwise.
   * @throws UnsupportedEncodingException
   */
  private static String rewriteQueryString(String queryString)
      throws UnsupportedEncodingException {
    int index = queryString.indexOf(ESCAPED_FRAGMENT_FORMAT1);
    if (index == -1) {
      index = queryString.indexOf(ESCAPED_FRAGMENT_FORMAT2);
    }
    if (index != -1) {
      StringBuilder queryStringSb = new StringBuilder();
      if (index > 0) {
        queryStringSb.append(queryString.substring(0, index - 1));
      }
      queryStringSb.append("#!");
      queryStringSb.append(URLDecoder.decode(queryStringSb.substring(index
          + ESCAPED_FRAGMENT_LENGTH, queryStringSb.length()), "UTF-8"));
      return queryStringSb.toString();
    }
    return queryString;
  }

  /**
   * The configuration for the servlet filter.
   */
  private FilterConfig filterConfig = null;

  /**
   * Destroys the filter configuration.
   */
  public void destroy() {
    this.filterConfig = null;
  }

  /**
   * Filters all requests and invokes headless browser if necessary.
   */
  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain chain) throws IOException {
    if (filterConfig == null) {
      return;
    }

    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse res = (HttpServletResponse) response;
    String queryString = req.getQueryString();

    if ((queryString != null) && (queryString.contains("_escaped_fragment_"))) {
      StringBuilder pageNameSb = new StringBuilder("http://");
      pageNameSb.append(req.getServerName());
      if (req.getServerPort() != 0) {
        pageNameSb.append(":");
        pageNameSb.append(req.getServerPort());
      }
      pageNameSb.append(req.getRequestURI());
      queryString = rewriteQueryString(queryString);
      pageNameSb.append(queryString);

      final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_3);
      webClient.setJavaScriptEnabled(true);
      String pageName = pageNameSb.toString();
      HtmlPage page = webClient.getPage(pageName);
      webClient.waitForBackgroundJavaScriptStartingBefore(2000);

      res.setContentType("text/html;charset=UTF-8");
      PrintWriter out = res.getWriter();
      out.println("<hr>");
      out.println("<center><h3>You are viewing a non-interactive page that is intended for the crawler.  "
          + "You probably want to see this page: <a href=\""
          + pageName
          + "\">"
          + pageName + "</a></h3></center>");
      out.println("<hr>");

      out.println(page.asXml());
      webClient.closeAllWindows();
      out.close();
    } else {
      try {
        chain.doFilter(request, response);
      } catch (ServletException e) {
        System.err.println("Servlet exception caught: " + e);
        e.printStackTrace();
      }
    }
  }

  /**
   * Initializes the filter configuration.
   */
  public void init(FilterConfig filterConfig) {
    this.filterConfig = filterConfig;
  }
}
