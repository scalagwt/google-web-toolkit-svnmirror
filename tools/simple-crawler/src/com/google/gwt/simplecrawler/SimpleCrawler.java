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
package com.google.gwt.simplecrawler;

import org.xml.sax.SAXException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Set;
import java.util.Queue;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javax.xml.parsers.ParserConfigurationException;

/**
 * PLEASE READ, THIS IS NOT THE USUAL YADDA YADDA.
 * 
 * This class performs a simple crawl of your web site. It is intended to be
 * used as a tool for developers who make their Ajax applications crawlable.
 * While it was developed by Google engineers, it makes *ABSOLUTELY NO
 * GUARANTEES* about behaving like the actual Google web crawler. This crawler
 * is initialized either by a URI or by a sitemap, and it follows hyperlinks on
 * the same site. This will allow developers to get an idea of what the real
 * crawler will see.
 */
public class SimpleCrawler {
  /**
   * Set of all web pages already seen.
   */
  private static Set<String> alreadySeenSet = new HashSet<String>();
  /**
   * Pattern to extract HREFs from pages.
   */
  private static final String HREF = "<[^>]*href=\"([^\"]*)\"";
  /**
   * Defaults to System.out if nothing is specified by the user.
   */
  private static PrintWriter out = new PrintWriter(System.out, true);
  /**
   * Queue of pages yet to be crawled.
   */
  private static Queue<String> urlQueue = new LinkedList<String>();

  /**
   * Gets the content of a URL via a web connection.
   * 
   * @param urlString
   *            the URL to fetch
   * @return content of the URL
   */
  private static String getUrlContent(String urlString) {
    try {
      URL url = new URL(urlString);
      InputStream urlInputStream = url.openStream();
      Scanner contentScanner = new Scanner(urlInputStream, "UTF-8");
      String content = "";
      if (contentScanner.hasNextLine()) {
        // trick documented here:
        // http://weblogs.java.net/blog/pat/archive/2004/10/stupid_scanner_1.html
        content = contentScanner.useDelimiter("\\A").next();
      }
      return content;
      } catch (MalformedURLException e) {
        System.err.println("Malformed url: " + urlString);
        return null;
      } catch (IOException e) {
      System.err.println("Could not open url: " + e.getMessage());
      return null;
    }
  }

  /**
   * Entry point.
   */
   public static void main(String[] args) throws ParserConfigurationException,
    SAXException {
    try {
      Settings settings = Settings.fromArgumentList(args);
      if (settings.out.get() != null) {
        out = new PrintWriter(new FileOutputStream(settings.out.get()), true);
      }
      if (settings.initUrl.get() != null) {
        String initialUrl = settings.initUrl.get();
        urlQueue.add(initialUrl);
      } else {
        SimpleSitemapParser.parseSitemap(settings.sitemap.get(), urlQueue);
      }
      runUntilQueueEmpty();
      } catch (Settings.ArgumentListException e) {
        System.err.println(e.getMessage());
        System.err.println("Usage: java com.google.gwt.crawler.SimpleCrawler [-sitemap SiteMapFile | -initUrl URL] [-out outFile]");
        System.err.println(Settings.settingsHelp());
        System.exit(1);
    } catch (IOException e) {
      System.err.println("Could not open file: " + e.getMessage());
      System.exit(1);
    }
  }

  /**
   * Creates a full URL from a hash fragment.
   * 
   * @return full URL
   */
  private static String makeFullUrlFromFragment(String hashFragment,
    String nextUrl) {
    // Get the baseUrl up to the query parameters.
    String baseUrl = nextUrl.replaceAll("\\?.*", "");
    return baseUrl + hashFragment;
  }

  /**
   * Maps to crawler URL, which in short means #! will be mapped to
   * _escaped_fragment_=. For example, www.example.com#!mystate is mapped to
   * www.example.com?_escaped_fragment_=mystate.
   * 
   * @param url
   * @return mapped URL
   */
  private static String mapToCrawlerUrl(String url) {
    String toReturn = url;
    if (toReturn.contains("#!")) {
      if (!toReturn.contains("?")) {
        toReturn = toReturn.replaceAll("#!", "?_escaped_fragment_=");
      } else {
        toReturn = toReturn.replaceAll("#!", "&_escaped_fragment_=");
      }
    }
    return toReturn;
  }

  /**
   * Maps back to the original URL, which can contain #!. For example,
   * www.example.com?_escaped_fragment_=mystate is mapped to
   * www.example.com#!mystate.
   * 
   * @param url
   * @return mapped url
   */
  private static String mapToOriginalUrl(String url) {
    String toReturn = url;
    if (toReturn.contains("_escaped_fragment_=")) {
      toReturn = toReturn.replaceAll("_escaped_fragment_=", "#!");
    }
    return toReturn;
  }

  /**
   * Gets the content for all the URLs on the queue, extracts links from it,
   * and follows the links.
   */
  private static void runUntilQueueEmpty() {
    /*
     * This pattern is correct in many, but not all cases.  It would be
     * impossible to find a pattern that matches all cases, for
     * example faulty HREFs.
     */
    Pattern hrefPattern = Pattern.compile(HREF, Pattern.CASE_INSENSITIVE);
    while (!urlQueue.isEmpty()) {
      String nextUrl = urlQueue.poll();
      if (!alreadySeenSet.contains(nextUrl)) {
        alreadySeenSet.add(nextUrl);
        out.println("------- The original URL is: " + nextUrl + " ------");
        nextUrl = mapToCrawlerUrl(nextUrl);
        out.println("------- The crawler is requesting the following URL: " + nextUrl + " ------");
        String indexedUrl = mapToOriginalUrl(nextUrl);
        if (indexedUrl.compareTo(nextUrl) != 0) {
          out.println("------- NOTE: This page will be indexed with the following URL: " 
            + indexedUrl + " ------");
        }
        String nextUrlContent = getUrlContent(nextUrl);
        if (nextUrlContent == null) {
          out.println("------ no content for this web page ------");
        } else {
          out.println(nextUrlContent);
          Matcher matcher = hrefPattern.matcher(nextUrlContent);
          while (matcher.find()) {
            String extractedUrl = matcher.group(1);
            if (!extractedUrl.startsWith("http")) {
              if (extractedUrl.startsWith("#")) {
                extractedUrl = makeFullUrlFromFragment(extractedUrl, nextUrl);
              }
              if ((extractedUrl.length() > 0) && (!alreadySeenSet.contains(extractedUrl))) {
                urlQueue.add(extractedUrl);
              }
            }
          }
        }
      }
    }
  }
}