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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Queue;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * This simple sitemap parser only looks at url entries and extracts the loc
 * from them. It ignores everything else.
 * The format of site maps is documented here: http://www.sitemaps.org.
 * A simple site map will look as follows:
 * <?xml version="1.0" encoding="UTF-8"?>
 * <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
 *    <url><loc>http://j15r.com:8800/Showcase/Showcase.html?locale=ar</loc></url>
 *    <url><loc>http://j15r.com:8800/Showcase/Showcase.html?locale=en</loc></url>
 * </urlset>
 */
public class SimpleSitemapParser {

  /**
   * Parses a site map file
   * 
   * @param fileName file name for site map. The format of the site map is
   *          documented here: {@link http://www.sitemaps.org/}
   * @param urlQueue Url queue where all Urls in the site map will be stored
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws IOException
   */
  public static void parseSitemap(String fileName, Queue<String> urlQueue)
      throws ParserConfigurationException, SAXException, IOException {
    DefaultHandler handler = makeSiteMapParserHandler(urlQueue);
    SAXParserFactory factoryMain = SAXParserFactory.newInstance();
    factoryMain.setNamespaceAware(true);
    SAXParser saxParser = factoryMain.newSAXParser();
    InputStream in = new FileInputStream(fileName);
    in = new BufferedInputStream(in);
    saxParser.parse(in, handler);
  }

  /**
   * SAX parser that performs the parse
   * 
   * @param urlQueue Url queue where all Urls in the site map will be stored
   * @return the SAX parser handler
   */
  private static DefaultHandler makeSiteMapParserHandler(final Queue<String> urlQueue) {
    return new DefaultHandler() {
      StringBuilder valueBuilder = new StringBuilder();

      @Override
      public void characters(char ch[], int start, int length) {
        valueBuilder.append(ch, start, length);
      }

      @Override
      public void endElement(String uri, String localName, String qName) {
        if (localName.compareTo("loc") == 0) {
          String url = valueBuilder.toString();
          urlQueue.add(url);
        }
      }

      @Override
      public void startElement(String uri, String localName, String qName,
          final Attributes attributes) {
        valueBuilder.delete(0, valueBuilder.length());
      }
    };
  }
}
