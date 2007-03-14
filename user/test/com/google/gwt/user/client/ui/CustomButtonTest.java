package com.google.gwt.user.client.ui;

import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.CustomButton.Face;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Test for <code>PushButton</code> as most of this widget's functionality is
 * UI based, the primary test will be in the new UI testing framework once it is
 * released.
 * 
 */
public class CustomButtonTest extends GWTTestCase {

  public String getModuleName() {
    return "com.google.gwt.user.User";
  }

  public void testCSS() {
    ToggleButton b = new ToggleButton("up", "down");
    b.setStyleName("random");
    b.setDown(true);
    assertEquals(b.getStyleName(), "random");

    Map faces = new HashMap();
    faces.put("downDisabled", b.getDownDisabledFace());
    faces.put("upDisabled", b.getUpDisabledFace());
    faces.put("down", b.getDownFace());
    faces.put("up", b.getUpFace());
    faces.put("upHovering", b.getUpHoveringFace());
    faces.put("downHovering", b.getDownHoveringFace());
    Iterator entries = faces.entrySet().iterator();

    // Set all faces as text.
    while (entries.hasNext()) {
      Map.Entry entry = (Entry) entries.next();
      Face f = (Face) entry.getValue();
      b.setCurrentFace(f);
      assertEquals("random-" + f.getName(), DOM.getAttribute(b.getElement(),
          "className"));
    }
  }

  public void testSettingFaces() {
    PushButton b = new PushButton();
    Map faces = new HashMap();
    faces.put("downDisabled", b.getDownDisabledFace());
    faces.put("upDisabled", b.getUpDisabledFace());
    faces.put("down", b.getDownFace());
    faces.put("up", b.getUpFace());
    faces.put("upHovering", b.getUpHoveringFace());
    faces.put("downHovering", b.getDownHoveringFace());
    Iterator entries = faces.entrySet().iterator();

    // Set all faces as text.
    while (entries.hasNext()) {
      Map.Entry entry = (Entry) entries.next();
      Face f = (Face) entry.getValue();
      String faceName = (String) entry.getKey();
      f.setText(faceName);
    }
    entries = faces.entrySet().iterator();
    while (entries.hasNext()) {
      Map.Entry entry = (Entry) entries.next();
      Face f = (Face) entry.getValue();
      String faceName = (String) entry.getKey();
      assertEquals(f.getText(), faceName);
    }
    // Set all faces as HTML
    entries = faces.entrySet().iterator();
    while (entries.hasNext()) {
      Map.Entry entry = (Entry) entries.next();
      Face f = (Face) entry.getValue();
      String faceName = (String) entry.getKey();
      f.setHTML("<b>" + faceName + "</b>");
    }

    entries = faces.entrySet().iterator();
    while (entries.hasNext()) {
      Map.Entry entry = (Entry) entries.next();
      Face f = (Face) entry.getValue();
      String faceName = (String) entry.getKey();
      assertEquals(f.getText(), faceName);
      assertEquals(f.getHTML().toLowerCase(), "<b>" + faceName.toLowerCase()
          + "</b>");
    }

  }

  public void testTransitions() {
    ToggleButton b = new ToggleButton("transitions");

    b.setDown(true);
    assertTrue(b.isDown());
    assertFalse(b.isHovering());
    assertTrue(b.isEnabled());

    b.setHovering(true);
    assertTrue(b.isDown());
    assertTrue(b.isHovering());
    assertTrue(b.isEnabled());

    b.setEnabled(false);
    assertTrue(b.isDown());
    assertFalse(b.isHovering());
    assertFalse(b.isEnabled());

    b.setDown(false);
    assertFalse(b.isHovering());
    assertFalse(b.isEnabled());
    assertFalse(b.isDown());

    b.setEnabled(false);
    assertFalse(b.isHovering());
    assertFalse(b.isEnabled());
    assertFalse(b.isDown());

    b.setEnabled(false);
    b.setHovering(true);
    assertTrue(b.isHovering());
    assertFalse(b.isDown());
    assertFalse(b.isEnabled());
  }

}
