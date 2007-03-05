/*
 * Copyright 2007 Google Inc.
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
package com.google.gwt.user.client.ui.richtext;

import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.RootPanel;

/** 
 * test {@link RichTextArea}.
 */
public class RichTextAreaTest extends GWTTestCase { 

  private static final int TEST_DELAY_TIME = 100;
  private static final int RICH_TEXT_AREA_WAIT_TIME = 10;

  /**
   * Gets the module name.
   */
  public String getModuleName() {
    return "com.google.gwt.user.User";
  }

  /**
   * Test constructors.
   */
  public void testConstructor() {
    RichTextArea rta = new RichTextArea();
    RichTextArea rtaWithCss = new RichTextArea("SampleRichTextArea.css");
    assertNotSame("Two different rich text objects must not be the same", rta, rtaWithCss);
  }
  
  /**
   * Test the ability to get and set HTML.
   */
  public void testHasHTML() {
    final RichTextArea rta = new RichTextArea();
    RootPanel.get().add(rta);
    // must wait for the rich text to load.
    new Timer() {
      public void run() {
        String sampleHTML = "<html><head></head><body><table><tbody><tr><td>a</td><td>b</td></tr><tr><td>a</td><td>b</td></tr></tbody></table></body></html>";
        rta.setHTML(sampleHTML);
        assertEquals("html should be equal", sampleHTML,rta.getHTML()); 
      }
    }.schedule(RICH_TEXT_AREA_WAIT_TIME);
    
    // Set a delay period significantly longer than the
    // event is expected to take.
    delayTestFinish(TEST_DELAY_TIME);
  }

  /**
   * Test the ability to get and set text.
   */
  public void testHasText() {
    final RichTextArea rta = new RichTextArea();
    RootPanel.get().add(rta);
    // must wait for the rich text to load.
    new Timer() {
      public void run() {
        String sampleHTML = "<html><head></head><body><table><tbody><tr><td>a</td><td>b</td></tr><tr><td>a</td><td>b</td></tr></tbody></table></body></html>";
        rta.setHTML(sampleHTML);
        assertEquals("html should be equal", sampleHTML,rta.getHTML()); 
      }
    }.schedule(RICH_TEXT_AREA_WAIT_TIME);
    
    // Set a delay period significantly longer than the
    // event is expected to take.
    delayTestFinish(TEST_DELAY_TIME);
  }
  
  public void testBoldItalicUnderlineInitialState() {
    final RichTextArea rta = new RichTextArea();
    RootPanel.get().add(rta);
    // must wait for the rich text to load.
    new Timer() {
      public void run() {
        String plainHTML = "<html><head></head><body><b><i><u>iuiuiu</u></i></u></body></html>";
        rta.setHTML(plainHTML);
        assertFalse("should be not bold", rta.isBold());
        assertFalse("should be not italic", rta.isItalic());
        assertFalse("should be not underlined", rta.isUnderlined());
        String boldHTML = "<html><head></head><body><b>bbb</b></body></html>";
        rta.setHTML(boldHTML);
        assertTrue("should be bold", rta.isBold());
        assertFalse("should be not italic", rta.isItalic());
        assertFalse("should be not underlined", rta.isUnderlined());
        String italicHTML = "<html><head></head><body><i>iii</i></body></html>";
        rta.setHTML(italicHTML);
        assertTrue("should be italic", rta.isItalic());
        assertFalse("should be not bold", rta.isBold());
        assertFalse("should be not underlined", rta.isUnderlined());
        String underlineHTML = "<html><head></head><body><u>uuu</u></body></html>";
        rta.setHTML(underlineHTML);
        assertTrue("should be underlined", rta.isUnderlined());
        assertFalse("should be not bold", rta.isBold());
        assertFalse("should be not italic", rta.isItalic());
        String boldItalicHTML = "<html><head></head><body><b><i>bibibi</i></b></body></html>";
        rta.setHTML(boldItalicHTML);
        assertTrue("should be bold", rta.isBold());
        assertTrue("should be italic", rta.isItalic());
        assertFalse("should be not underlined", rta.isUnderlined());
        String boldUnderlineHTML = "<html><head></head><body><b><i>bububu</i></b></body></html>";
        rta.setHTML(boldUnderlineHTML);
        assertTrue("should be bold", rta.isBold());
        assertTrue("should be underlined", rta.isUnderlined());
        assertFalse("should be not italic", rta.isItalic());
        String italicUnderlineHTML = "<html><head></head><body><i><u>iuiuiu</u></i></body></html>";
        rta.setHTML(italicUnderlineHTML);
        assertTrue("should be italic", rta.isItalic());
        assertTrue("should be underlined", rta.isUnderlined());
        assertFalse("should be not bold", rta.isBold());
        String boldItalicUnderlineHTML = "<html><head></head><body><b><i><u>iuiuiu</u></i></u></body></html>";
        rta.setHTML(boldItalicUnderlineHTML);
        assertTrue("should be bold", rta.isBold());
        assertTrue("should be italic", rta.isItalic());
        assertTrue("should be underlined", rta.isUnderlined());
      }
    }.schedule(RICH_TEXT_AREA_WAIT_TIME);
    
    // Set a delay period significantly longer than the
    // event is expected to take.
    delayTestFinish(TEST_DELAY_TIME);
  }
  
}
