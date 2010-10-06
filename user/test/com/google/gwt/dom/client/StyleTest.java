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
package com.google.gwt.dom.client;

import static com.google.gwt.dom.client.Style.Unit.CM;
import static com.google.gwt.dom.client.Style.Unit.EM;
import static com.google.gwt.dom.client.Style.Unit.EX;
import static com.google.gwt.dom.client.Style.Unit.IN;
import static com.google.gwt.dom.client.Style.Unit.MM;
import static com.google.gwt.dom.client.Style.Unit.PC;
import static com.google.gwt.dom.client.Style.Unit.PCT;
import static com.google.gwt.dom.client.Style.Unit.PT;
import static com.google.gwt.dom.client.Style.Unit.PX;

import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.FontStyle;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.HasCssName;
import com.google.gwt.dom.client.Style.ListStyleType;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.TextDecoration;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.junit.client.GWTTestCase;

/**
 * Tests the native {@link Style} class.
 */
public class StyleTest extends GWTTestCase {

  @Override
  public String getModuleName() {
    return "com.google.gwt.dom.DOMTest";
  }

  public void testCursor() {
    DivElement div = Document.get().createDivElement();
    Style style = div.getStyle();

    style.setCursor(Cursor.DEFAULT);
    assertEquals(Cursor.DEFAULT, style.getCursor());
    style.setCursor(Cursor.AUTO);
    assertEquals(Cursor.AUTO, style.getCursor());
    style.setCursor(Cursor.CROSSHAIR);
    assertEquals(Cursor.CROSSHAIR, style.getCursor());
    style.setCursor(Cursor.POINTER);
    assertEquals(Cursor.POINTER, style.getCursor());
    style.setCursor(Cursor.MOVE);
    assertEquals(Cursor.MOVE, style.getCursor());
    style.setCursor(Cursor.E_RESIZE);
    assertEquals(Cursor.E_RESIZE, style.getCursor());
    style.setCursor(Cursor.NE_RESIZE);
    assertEquals(Cursor.NE_RESIZE, style.getCursor());
    style.setCursor(Cursor.NW_RESIZE);
    assertEquals(Cursor.NW_RESIZE, style.getCursor());
    style.setCursor(Cursor.N_RESIZE);
    assertEquals(Cursor.N_RESIZE, style.getCursor());
    style.setCursor(Cursor.SE_RESIZE);
    assertEquals(Cursor.SE_RESIZE, style.getCursor());
    style.setCursor(Cursor.SW_RESIZE);
    assertEquals(Cursor.SW_RESIZE, style.getCursor());
    style.setCursor(Cursor.S_RESIZE);
    assertEquals(Cursor.S_RESIZE, style.getCursor());
    style.setCursor(Cursor.W_RESIZE);
    assertEquals(Cursor.W_RESIZE, style.getCursor());
    style.setCursor(Cursor.TEXT);
    assertEquals(Cursor.TEXT, style.getCursor());
    style.setCursor(Cursor.WAIT);
    assertEquals(Cursor.WAIT, style.getCursor());
    style.setCursor(Cursor.HELP);
    assertEquals(Cursor.HELP, style.getCursor());

    // These aren't supported on old mozilla, so testing them will break.
    // TODO: re-enable these cases when we finally drop linux hosted mode.
    /*
    style.setCursor(Cursor.COL_RESIZE);
    assertEquals(Cursor.COL_RESIZE, style.getCursor());
    style.setCursor(Cursor.ROW_RESIZE);
    assertEquals(Cursor.ROW_RESIZE, style.getCursor());
    */
  }

  public void testDisplay() {
    DivElement div = Document.get().createDivElement();
    Style style = div.getStyle();

    style.setDisplay(Display.NONE);
    assertEquals(Display.NONE, style.getDisplay());
    style.setDisplay(Display.BLOCK);
    assertEquals(Display.BLOCK, style.getDisplay());
    style.setDisplay(Display.INLINE);
    assertEquals(Display.INLINE, style.getDisplay());

    // Not supported on old mozilla, so testing it will break.
    // TODO: re-enable these cases when we finally drop linux hosted mode.
    /*
    style.setDisplay(Display.INLINE_BLOCK);
    assertEquals(Display.INLINE_BLOCK, style.getDisplay());
    */
  }

  public void testFontStyle() {
    DivElement div = Document.get().createDivElement();
    Style style = div.getStyle();

    style.setFontStyle(FontStyle.NORMAL);
    assertEquals(FontStyle.NORMAL, style.getFontStyle());
    style.setFontStyle(FontStyle.ITALIC);
    assertEquals(FontStyle.ITALIC, style.getFontStyle());
    style.setFontStyle(FontStyle.OBLIQUE);
    assertEquals(FontStyle.OBLIQUE, style.getFontStyle());
  }

  public void testFontWeight() {
    DivElement div = Document.get().createDivElement();
    Style style = div.getStyle();

    style.setFontWeight(FontWeight.NORMAL);
    assertEquals(FontWeight.NORMAL, style.getFontWeight());
    style.setFontWeight(FontWeight.BOLD);
    assertEquals(FontWeight.BOLD, style.getFontWeight());
    style.setFontWeight(FontWeight.BOLDER);
    assertEquals(FontWeight.BOLDER, style.getFontWeight());
    style.setFontWeight(FontWeight.LIGHTER);
    assertEquals(FontWeight.LIGHTER, style.getFontWeight());
  }

  public void testListStyleType() {
    DivElement div = Document.get().createDivElement();
    Style style = div.getStyle();

    style.setListStyleType(ListStyleType.NONE);
    assertEquals(ListStyleType.NONE, style.getListStyleType());
    style.setListStyleType(ListStyleType.DISC);
    assertEquals(ListStyleType.DISC, style.getListStyleType());
    style.setListStyleType(ListStyleType.CIRCLE);
    assertEquals(ListStyleType.CIRCLE, style.getListStyleType());
    style.setListStyleType(ListStyleType.SQUARE);
    assertEquals(ListStyleType.SQUARE, style.getListStyleType());
    style.setListStyleType(ListStyleType.DECIMAL);
    assertEquals(ListStyleType.DECIMAL, style.getListStyleType());
    style.setListStyleType(ListStyleType.LOWER_ALPHA);
    assertEquals(ListStyleType.LOWER_ALPHA, style.getListStyleType());
    style.setListStyleType(ListStyleType.UPPER_ALPHA);
    assertEquals(ListStyleType.UPPER_ALPHA, style.getListStyleType());
    style.setListStyleType(ListStyleType.LOWER_ROMAN);
    assertEquals(ListStyleType.LOWER_ROMAN, style.getListStyleType());
    style.setListStyleType(ListStyleType.UPPER_ROMAN);
    assertEquals(ListStyleType.UPPER_ROMAN, style.getListStyleType());
  }

  public void testOverflow() {
    DivElement div = Document.get().createDivElement();
    Style style = div.getStyle();

    style.setOverflow(Overflow.VISIBLE);
    assertEquals(Overflow.VISIBLE, style.getOverflow());
    style.setOverflow(Overflow.HIDDEN);
    assertEquals(Overflow.HIDDEN, style.getOverflow());
    style.setOverflow(Overflow.SCROLL);
    assertEquals(Overflow.SCROLL, style.getOverflow());
    style.setOverflow(Overflow.AUTO);
    assertEquals(Overflow.AUTO, style.getOverflow());
  }

  public void testPosition() {
    DivElement div = Document.get().createDivElement();
    Style style = div.getStyle();

    style.setPosition(Position.STATIC);
    assertEquals(Position.STATIC, style.getPosition());
    style.setPosition(Position.RELATIVE);
    assertEquals(Position.RELATIVE, style.getPosition());
    style.setPosition(Position.ABSOLUTE);
    assertEquals(Position.ABSOLUTE, style.getPosition());
    style.setPosition(Position.FIXED);
    assertEquals(Position.FIXED, style.getPosition());
  }

  public void testTextDecoration() {
    DivElement div = Document.get().createDivElement();
    Style style = div.getStyle();

    style.setTextDecoration(TextDecoration.NONE);
    assertEquals(TextDecoration.NONE, style.getTextDecoration());
    style.setTextDecoration(TextDecoration.UNDERLINE);
    assertEquals(TextDecoration.UNDERLINE, style.getTextDecoration());
    style.setTextDecoration(TextDecoration.OVERLINE);
    assertEquals(TextDecoration.OVERLINE, style.getTextDecoration());
    style.setTextDecoration(TextDecoration.LINE_THROUGH);
    assertEquals(TextDecoration.LINE_THROUGH, style.getTextDecoration());
  }

  public void testVerticalAlign() {
    DivElement div = Document.get().createDivElement();
    Style style = div.getStyle();

    style.setVerticalAlign(VerticalAlign.BASELINE);
    assertEquals(VerticalAlign.BASELINE, style.getVerticalAlign());
    style.setVerticalAlign(VerticalAlign.SUB);
    assertEquals(VerticalAlign.SUB, style.getVerticalAlign());
    style.setVerticalAlign(VerticalAlign.SUPER);
    assertEquals(VerticalAlign.SUPER, style.getVerticalAlign());
    style.setVerticalAlign(VerticalAlign.TOP);
    assertEquals(VerticalAlign.TOP, style.getVerticalAlign());
    style.setVerticalAlign(VerticalAlign.TEXT_TOP);
    assertEquals(VerticalAlign.TEXT_TOP, style.getVerticalAlign());
    style.setVerticalAlign(VerticalAlign.MIDDLE);
    assertEquals(VerticalAlign.MIDDLE, style.getVerticalAlign());
    style.setVerticalAlign(VerticalAlign.BOTTOM);
    assertEquals(VerticalAlign.BOTTOM, style.getVerticalAlign());
    style.setVerticalAlign(VerticalAlign.TEXT_BOTTOM);
    assertEquals(VerticalAlign.TEXT_BOTTOM, style.getVerticalAlign());
  }

  public void testVisibility() {
    DivElement div = Document.get().createDivElement();
    Style style = div.getStyle();

    // Safari 3 coerces 'visible' to '', so this test fails.
    /*
    style.setVisibility(Visibility.VISIBLE);
    assertEquals(Visibility.VISIBLE, style.getVisibility());
    */
    style.setVisibility(Visibility.HIDDEN);
    assertEquals(Visibility.HIDDEN, style.getVisibility());
  }

  public void testUnits() {
    DivElement div = Document.get().createDivElement();
    Style style = div.getStyle();

    style.setWidth(1, PX);
    assertEquals("1px", style.getWidth());
    style.setWidth(1, PCT);
    assertEquals("1%", style.getWidth());
    style.setWidth(1, EM);
    assertEquals("1em", style.getWidth());
    style.setWidth(1, EX);
    assertEquals("1ex", style.getWidth());
    style.setWidth(1, PT);
    assertEquals("1pt", style.getWidth());
    style.setWidth(1, PC);
    assertEquals("1pc", style.getWidth());
    style.setWidth(1, CM);
    assertEquals("1cm", style.getWidth());
    style.setWidth(1, IN);
    assertEquals("1in", style.getWidth());
    style.setWidth(1, MM);
    assertEquals("1mm", style.getWidth());
  }

  private void assertEquals(HasCssName enumValue, String cssValue) {
    assertEquals(enumValue.getCssName(), cssValue);
  }
}
