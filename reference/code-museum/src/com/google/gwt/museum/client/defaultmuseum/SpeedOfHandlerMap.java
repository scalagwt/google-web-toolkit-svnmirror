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

package com.google.gwt.museum.client.defaultmuseum;

import com.google.gwt.core.client.Duration;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HandlesAllMouseEvents;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.GwtEventUtil;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.museum.client.common.AbstractIssue;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Checks the speed of adding and firing dispatch events based on different
 * implementation of handler map.
 * 
 */
public class SpeedOfHandlerMap extends AbstractIssue {
  /**
   * Enum widget selector.
   * 
   * @param <E> enum
   */
  private class EnumInput<E extends Enum<?>> extends Composite {
    private ListBox b = new ListBox();
    private E[] enumArray;

    public EnumInput(String name, E[] enumArray) {
      Panel p = new VerticalPanel();
      HTML label = new HTML(name);
      p.add(label);
      p.add(b);
      initWidget(p);
      this.enumArray = enumArray;
      for (int i = 0; i < enumArray.length; i++) {
        b.addItem(enumArray[i].name());
      }
    }

    private E getValue() {
      return enumArray[b.getSelectedIndex()];
    }
  }

  /**
   * Types of handler maps.
   */
  private enum HandlerMapType {
    handlerMapForJs, handlerMapForJava, handlerMapForSimpleJs
  }

  /**
   * Java handler map.
   */
  private static class JavaHandlerMap extends
      HashMap<GwtEvent.Type<?>, ArrayList<?>> {

    public <H extends EventHandler> void addHandler(Type<H> type, H handler) {
      ArrayList<H> l = get(type);
      if (l == null) {
        l = new ArrayList<H>();
        super.put(type, l);
      }
      l.add(handler);
    }

    public <H extends EventHandler> void fireEvent(GwtEvent<H> event) {
      Type<H> type = GwtEventUtil.getType(event);
      int count = getHandlerCount(type);
      for (int i = 0; i < count; i++) {
        H handler = getHandler(type, i);
        GwtEventUtil.dispatch(event, handler);
      }
    }

    public <H extends EventHandler> H getHandler(GwtEvent.Type<H> eventKey,
        int index) {
      ArrayList<H> l = get(eventKey);
      return l.get(index);
    }

    public int getHandlerCount(GwtEvent.Type<?> eventKey) {
      ArrayList<?> l = super.get(eventKey);
      if (l == null) {
        return 0;
      } else {
        return l.size();
      }
    }

    public <H> void removeHandler(GwtEvent.Type<H> eventKey, H handler) {
      ArrayList<H> l = get(eventKey);
      if (l != null) {
        boolean result = l.remove(handler);
        assert result : "Tried to remove unknown handler";
      }
    }

    @SuppressWarnings("unchecked")
    private <H> ArrayList<H> get(GwtEvent.Type<H> type) {
      // This cast is safe because we control the puts.
      return (ArrayList<H>) super.get(type);
    }
  }

  /**
   * The flattened js handler map.
   */
  private static class JsHandlerMap extends JavaScriptObject {

    /**
     * Required constructor.
     */
    protected JsHandlerMap() {
    }

    private <H extends EventHandler> void addHandler(Type<H> type, H myHandler) {

      // The base is the equivalent to a c pointer into the flattened handler
      // data structure.
      int base = getIndex(type);
      int count = getCount(base);
      boolean flattened = isFlattened(base);
      H handler = myHandler;
      // If we already have the maximum number of handlers we can store in the
      // flattened data structure, store the handlers in an external list
      // instead.
      if ((count == EXPECTED_HANDLERS) & flattened) {
        unflatten(base);
        flattened = false;
      }
      if (flattened) {
        setFlatHandler(base, count, handler);
      } else {
        setHandler(base, count, handler);
      }
      setCount(base, count + 1);
    }

    private <H extends EventHandler> void fireEvent(GwtEvent<H> event) {
      Type<H> type = GwtEventUtil.getType(event);
      int base = getIndex(type);
      int count = getCount(base);
      boolean isFlattened = isFlattened(base);
      if (isFlattened) {
        for (int i = 0; i < count; i++) {
          // Gets the given handler to fire.
          H handler = getFlatHandler(base, i);
          // Fires the handler.
          GwtEventUtil.dispatch(event, handler);
        }
      } else {
        JavaScriptObject handlers = getHandlers(base);
        for (int i = 0; i < count; i++) {
          // Gets the given handler to fire.
          H handler = getHandler(handlers, i);

          // Fires the handler.
          GwtEventUtil.dispatch(event, handler);
        }
      }
    }

    private native int getCount(int index) /*-{
      var count = this[index];
      return count == null? 0:count;
    }-*/;

    private native <H extends EventHandler> H getFlatHandler(int base, int index) /*-{
      return this[base + 2 + index];
    }-*/;

    private <H extends EventHandler> H getHandler(GwtEvent.Type<H> type,
        int index) {
      int base = getIndex(type);
      int count = getCount(base);
      if (index >= count) {
        throw new IndexOutOfBoundsException("index: " + index);
      }
      return getHandler(base, index, isFlattened(base));
    }

    private native <H extends EventHandler> H getHandler(int base, int index,
        boolean flattened) /*-{
      return flattened? this[base + 2 + index]: this[base + 1][index];
    }-*/;

    private native <H extends EventHandler> H getHandler(
        JavaScriptObject handlers, int index) /*-{
      return handlers[index];
    }-*/;

    private int getHandlerCount(GwtEvent.Type<?> eventKey) {
      return getCount(eventKey.hashCode());
    }

    private native JavaScriptObject getHandlers(int base) /*-{
      return  this[base + 1];
    }-*/;

    private native boolean isFlattened(int base) /*-{
      return this[base + 1] == null;
    }-*/;

    private <H> void removeHandler(GwtEvent.Type<H> eventKey,
        EventHandler handler) {
      int base = eventKey.hashCode();

      // Removing a handler is unusual, so smaller code is preferable to
      // handling both flat and dangling list of pointers.
      if (isFlattened(base)) {
        unflatten(base);
      }
      boolean result = removeHelper(base, handler);
      // Hiding this behind an assertion as we'd rather not force the compiler
      // to have to include all handler.toString() instances.
      assert result : handler + " did not exist";
    }

    private native boolean removeHelper(int base, EventHandler handler) /*-{
      // Find the handler.
      var count = this[base];
      var handlerList = this[base + 1];
      var handlerIndex = -1;
      for(var index = 0;  index < count; index++){
        if(handlerList[index] == handler){
          handlerIndex = index;
          break;
        }
      }
      if(handlerIndex == -1) {
        return false;
      }

      // Remove the handler.
      var last = count -1;
      for(; handlerIndex < last; handlerIndex++){
        handlerList[handlerIndex] = handlerList[handlerIndex+1]
      }
      handlerList[last] = null;
      this[base] = this[base]-1;
      return true;
    }-*/;

    private native void setCount(int index, int count) /*-{
      this[index] = count;
    }-*/;

    private native void setFlatHandler(int base, int index, EventHandler handler) /*-{
      this[base + 2 + index] = handler;
    }-*/;

    private native void setHandler(int base, int index, EventHandler handler) /*-{
      this[base + 1][index] = handler;
    }-*/;

    private native void unflatten(int base) /*-{
      var handlerList = {};
      var count = this[base];
      var start = base + 2;
       for(var i = 0; i < count;i++){
         handlerList[i] = this[start + i];
         this[start + i] = null;
        }
       this[base + 1] = handlerList;
    }-*/;
  }

  /**
   * Simple handler used for this speed test.
   */
  private static class MyHandler extends HandlesAllMouseEvents {
    private static int fired;

    public static int getFiredCounter() {
      return fired;
    }

    public void clearFiredCounter() {
      fired = 0;
    }

    public void onMouseDown(MouseDownEvent event) {
      ++fired;
    }

    public void onMouseMove(MouseMoveEvent event) {
      ++fired;
    }

    public void onMouseOut(MouseOutEvent event) {
      ++fired;
    }

    public void onMouseOver(MouseOverEvent event) {
      ++fired;
    }

    public void onMouseUp(MouseUpEvent event) {
      ++fired;
    }

    public void onMouseWheel(MouseWheelEvent event) {
      ++fired;
    }
  }

  /**
   * Number input.
   */
  private class NumberInput extends Composite {
    TextBox box = new TextBox();

    public NumberInput(String name, int defaultValue) {
      Panel p = new VerticalPanel();
      HTML label = new HTML(name);
      p.add(label);
      p.add(box);
      box.setText(defaultValue + "");
      initWidget(p);
    }

    @Override
    public String toString() {
      return box.getText();
    }

    private int getValue() {
      String txt = box.getText();
      txt = txt.replaceAll(" ", "");
      return Integer.parseInt(txt);
    }
  }

  /**
   * Simple js handler map.
   */
  private static class SimpleJsHandlerMap extends JavaScriptObject {

    /**
     * Required constructor.
     */
    protected SimpleJsHandlerMap() {
    }

    private <H extends EventHandler> void addHandler(Type<H> type, H myHandler) {

      // The base is the equivalent to a c pointer into the flattened handler
      // data structure.
      int base = getIndex(type);
      int count = getCount(base);
      if (count == 0) {
        createListFor(base);
      }
      H handler = myHandler;
      setHandler(base, count, handler);
      setCount(base, count + 1);
    }

    private native void createListFor(int base) /*-{
      this[base + 1] =  {};
    }-*/;

    private <H extends EventHandler> void fireEvent(GwtEvent<H> event) {
      Type<H> type = GwtEventUtil.getType(event);
      int base = getIndex(type);
      int count = getCount(base);

      JavaScriptObject handlers = getHandlers(base);
      for (int i = 0; i < count; i++) {
        // Gets the given handler to fire.
        H handler = getHandler(handlers, i);

        // Fires the handler.
        GwtEventUtil.dispatch(event, handler);
      }
    }

    private native int getCount(int index) /*-{
      var count = this[index];
      return count == null? 0 : count;
    }-*/;

    private <H extends EventHandler> H getHandler(GwtEvent.Type<H> type,
        int index) {
      int base = getIndex(type);
      int count = getCount(base);
      if (index >= count) {
        throw new IndexOutOfBoundsException("index: " + index);
      }
      return getHandler(getHandlers(base), index);
    }

    private native <H extends EventHandler> H getHandler(
        JavaScriptObject handlers, int index) /*-{
      return handlers[index];
    }-*/;

    private int getHandlerCount(GwtEvent.Type<?> eventKey) {
      return getCount(eventKey.hashCode());
    }

    private native JavaScriptObject getHandlers(int base) /*-{
      return this[base + 1];
    }-*/;

    private <H> void removeHandler(GwtEvent.Type<H> eventKey,
        EventHandler handler) {
      int base = eventKey.hashCode();
      boolean result = removeHelper(base, handler);
      // Hiding this behind an assertion as we'd rather not force the compiler
      // to have to include all handler.toString() instances.
      assert result : handler + " did not exist";
    }

    private native boolean removeHelper(int base, EventHandler handler) /*-{
      // Find the handler.
      var count = this[base];
      var handlerList = this[base + 1];
      var handlerIndex = -1;
      for(var index = 0;  index < count; index++){
        if(handlerList[index] == handler){
          handlerIndex = index;
          break;
        }
      }
      if(handlerIndex == -1) {
        return false;
      }

      // Remove the handler.
      var last = count -1;
      for(; handlerIndex < last; handlerIndex++){
        handlerList[handlerIndex] = handlerList[handlerIndex+1]
      }
      handlerList[last] = null;
      this[base] = this[base]-1;
      return true;
    }-*/;

    private native void setCount(int index, int count) /*-{
      this[index] = count;
    }-*/;

    private native void setHandler(int base, int index, EventHandler handler) /*-{
      this[base + 1][index] = handler;
    }-*/;
  }

  /**
   * Reports benchmarks.
   */
  private class SpeedReporter extends Composite {
    private FlexTable p = new FlexTable();
    private int index = 0;
    private HashMap<String, Integer> currentReports = new HashMap<String, Integer>();
    private Duration duration;

    public SpeedReporter() {
      initWidget(p);
    }

    public void log(String action) {
      double milli = duration.elapsedMillis();
      report(action, milli + " millseconds");
      duration = null;
    }

    public void start() {
      MyHandler.fired = 0;
      duration = new Duration();
    }

    public void start(String startDesc) {
      report(startDesc, null);
      start();
    }

    private void report(String action, String results) {
      Integer prevIndex = currentReports.get(action);
      if (prevIndex == null) {
        p.setText(index, 0, action);
        if (results != null) {
          p.setText(index, 1, results);
        }
        currentReports.put(action, index);
        ++index;
      } else {
        if (results != null) {
          p.setText(prevIndex, 1, p.getText(prevIndex, 1) + ", " + results);
        }
      }
    }
  }

  private static int EXPECTED_HANDLERS = 10;

  private static int getIndex(Type<?> t) {
    return t.hashCode() * EXPECTED_HANDLERS;
  }

  private EnumInput<HandlerMapType> mapTypeInput = new EnumInput<HandlerMapType>(
      "map type", HandlerMapType.values());

  private NumberInput numMapsInput = new NumberInput("number of maps", 5000);
  private NumberInput numHandlersInput = new NumberInput(
      "hander types per map, max 8", 6);
  private int numHandlers;
  private int numMaps;
  private int currentStep = 0;
  private HandlerMapType mapType;

  // The handler maps, only one of which will be not empty each testing round.
  private JavaHandlerMap[] javaMaps;
  private JsHandlerMap[] jsMaps;
  private SimpleJsHandlerMap[] simpleJsMaps;

  // Speed results.
  private SpeedReporter reporter = new SpeedReporter();

  // Actual test objects.
  private MyHandler handler = new MyHandler();

  // We cheat below, so cannot use generics here.
  @SuppressWarnings("unchecked")
  private Type[] types = {
      MouseDownEvent.getType(), MouseUpEvent.getType(),
      MouseMoveEvent.getType(), MouseOverEvent.getType(),
      MouseWheelEvent.getType(), KeyDownEvent.getType(), KeyUpEvent.getType(),
      KeyPressEvent.getType()};

  @Override
  public Widget createIssue() {
    VerticalPanel p = new VerticalPanel();
    HorizontalPanel controls = new HorizontalPanel();

    if (GWT.isScript()) {
      numMapsInput = new NumberInput("number of maps", 5000);
    } else {
      numMapsInput = new NumberInput("number of maps", 5);
    }
    controls.add(numMapsInput);
    controls.add(numHandlersInput);
    controls.add(mapTypeInput);
    p.add(controls);
    final Button runSpeedTest = new Button("Start");
    p.add(runSpeedTest);
    runSpeedTest.addClickHandler(new ClickHandler() {

      public void onClick(ClickEvent event) {
        setup();
        speedTest();
      }

    });
    p.add(reporter);
    return p;
  }

  @Override
  public String getInstructions() {
    return "Try different combination of variables";
  }

  @Override
  public String getSummary() {
    return "Handler map speed test";
  }

  @Override
  public boolean hasCSS() {
    return false;
  }

  private void setup() {
    numMaps = numMapsInput.getValue();
    numHandlers = numHandlersInput.getValue();
    mapType = mapTypeInput.getValue();
    javaMaps = new JavaHandlerMap[numMaps];
    jsMaps = new JsHandlerMap[numMaps];
    simpleJsMaps = new SimpleJsHandlerMap[numMaps];
  }

  @SuppressWarnings("unchecked")
  private void speedTest() {
    reporter.start("====== Start " + mapType + " Test======");
    if (mapType == HandlerMapType.handlerMapForJava) {
      for (int i = 0; i < numMaps; i++) {
        javaMaps[i] = new JavaHandlerMap();
      }
    } else if (mapType == HandlerMapType.handlerMapForJs) {
      for (int i = 0; i < numMaps; i++) {
        jsMaps[i] = JsHandlerMap.createArray().cast();
      }
    } else if (mapType == HandlerMapType.handlerMapForSimpleJs) {
      for (int i = 0; i < numMaps; i++) {
        simpleJsMaps[i] = JsHandlerMap.createArray().cast();
      }
    }

    reporter.log("Creating " + numMaps + " " + mapType.name() + " maps");
    reporter.start();

    if (mapType == HandlerMapType.handlerMapForJava) {
      for (int i = 0; i < numMaps; i++) {
        JavaHandlerMap map = javaMaps[i];
        for (int j = 0; j < numHandlers; j++) {
          map.addHandler(types[i], handler);
        }
      }
    } else if (mapType == HandlerMapType.handlerMapForJs) {
      for (int i = 0; i < numMaps; i++) {
        JsHandlerMap map = jsMaps[i];
        for (int j = 0; j < numHandlers; j++) {
          map.addHandler(types[j], handler);
        }
      }
    } else if (mapType == HandlerMapType.handlerMapForSimpleJs) {
      for (int i = 0; i < numMaps; i++) {
        SimpleJsHandlerMap map = simpleJsMaps[i];
        for (int j = 0; j < numHandlers; j++) {
          map.addHandler(types[j], handler);
        }
      }
    }

    reporter.log("Adding " + numHandlers + " handlers for " + numMaps
        + " to a " + mapType.name());
    reporter.start();
    MouseMoveEvent e = new MouseMoveEvent() {
    };
    if (mapType == HandlerMapType.handlerMapForJava) {
      for (int i = 0; i < numMaps; i++) {
        JavaHandlerMap map = javaMaps[i];
        for (int j = 0; j < numHandlers; j++) {
          map.fireEvent(e);
        }
      }
    } else if (mapType == HandlerMapType.handlerMapForJs) {
      for (int i = 0; i < numMaps; i++) {
        JsHandlerMap map = jsMaps[i];
        for (int j = 0; j < numHandlers; j++) {
          map.fireEvent(e);
        }
      }
    } else if (mapType == HandlerMapType.handlerMapForSimpleJs) {
      for (int i = 0; i < numMaps; i++) {
        SimpleJsHandlerMap map = simpleJsMaps[i];
        for (int j = 0; j < numHandlers; j++) {
          map.fireEvent(e);
        }
      }
    }
    
    reporter.log("Fired " + MyHandler.getFiredCounter() + " handlers for "
        + numMaps + " widgets using " + mapType);
    reporter.start();
    StringBuffer b = new StringBuffer();
    for (int i = 0; i < 4000; i++) {
      b.append(i + " index");
      if (b.toString().length() > 20) {
        b.setLength(0);
      }
    }
    reporter.log("created a random string with " + (numMaps * numHandlers)
        + " handlers " + " using " + mapType);
  }
}
