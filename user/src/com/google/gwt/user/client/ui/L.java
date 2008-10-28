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

package com.google.gwt.user.client.ui;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.AllFocusHandlers;
import com.google.gwt.event.dom.client.AllKeyHandlers;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.CellClickEvent;
import com.google.gwt.event.dom.client.CellClickHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasAllFocusHandlers;
import com.google.gwt.event.dom.client.HasAllKeyHandlers;
import com.google.gwt.event.dom.client.HasCellClickHandlers;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasMouseDownHandlers;
import com.google.gwt.event.dom.client.HasMouseMoveHandlers;
import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.HasMouseUpHandlers;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.shared.AbstractEvent;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.AbstractEvent.Type;

import java.util.EventListener;

/**
 * Root of legacy listener support hierarchy.
 * 
 * @param <T> listener type
 * 
 */
@Deprecated
abstract class L<T> implements EventHandler {

  public static class Change extends L<ChangeListener> implements ChangeHandler {
    @Deprecated
    public static void add(HasChangeHandlers source, ChangeListener listener) {
      source.addChangeHandler(new Change(listener));
    }

    public static void remove(Widget eventSource, ChangeListener listener) {
      baseRemove(eventSource, listener, ChangeEvent.getType());
    }

    protected Change(ChangeListener listener) {
      super(listener);
    }

    public void onChange(ChangeEvent event) {
      listener.onChange(source(event));
    }
  }

  public static class Click extends L<ClickListener> implements ClickHandler {
    @Deprecated
    public static void add(HasClickHandlers source, ClickListener listener) {
      source.addClickHandler(new Click(listener));
    }

    public static void remove(Widget eventSource, ClickListener listener) {
      baseRemove(eventSource, listener, ClickEvent.getType());
    }

    protected Click(ClickListener listener) {
      super(listener);
    }

    public void onClick(ClickEvent event) {
      listener.onClick(source(event));
    }
  }

  public static class Disclosure extends L<DisclosureHandler> implements
      CloseHandler<DisclosurePanel>, OpenHandler<DisclosurePanel> {

    public static void add(DisclosurePanel source, DisclosureHandler listener) {
      Disclosure handlers = new Disclosure(listener);
      source.addOpenHandler(handlers);
      source.addCloseHandler(handlers);
    }

    public static void remove(Widget eventSource, DisclosureHandler listener) {
      baseRemove(eventSource, listener, CloseEvent.getType(),
          OpenEvent.getType());
    }

    protected Disclosure(DisclosureHandler listener) {
      super(listener);
    }

    public void onClose(CloseEvent<DisclosurePanel> event) {
      listener.onClose(new DisclosureEvent((DisclosurePanel) event.getSource()));
    }

    public void onOpen(OpenEvent<DisclosurePanel> event) {
      listener.onOpen(new DisclosureEvent((DisclosurePanel) event.getSource()));
    }
  }

  /*
   * Handler wrapper for {@link FocusListener}.
   */
  public static class Focus extends L<FocusListener> implements FocusHandler,
      BlurHandler {

    public static <EventSourceType extends Widget & HasAllFocusHandlers> void add(
        EventSourceType source, FocusListener listener) {
      AllFocusHandlers.addHandlers(source, new Focus(listener));
    }

    public static void remove(Widget eventSource, FocusListener listener) {
      baseRemove(eventSource, listener, LoadEvent.getType(),
          ErrorEvent.getType());
    }

    public Focus(FocusListener listener) {
      super(listener);
    }

    public void onBlur(BlurEvent event) {
      listener.onLostFocus(source(event));
    }

    public void onFocus(FocusEvent event) {
      listener.onFocus(source(event));
    }
  }

  public static class Form extends L<FormHandler> implements
      FormPanel.SubmitHandler, FormPanel.SubmitCompleteHandler {

    public static void add(FormPanel source, FormHandler listener) {
      Form handlers = new Form(listener);
      source.addSubmitHandler(handlers);
      source.addSubmitCompleteHandler(handlers);
    }

    public static void remove(Widget eventSource, FormHandler listener) {
      baseRemove(eventSource, listener, FormPanel.SubmitEvent.getType(),
          FormPanel.SubmitCompleteEvent.getType());
    }

    protected Form(FormHandler listener) {
      super(listener);
    }

    public void onSubmit(FormPanel.SubmitEvent event) {
      FormSubmitEvent fse = new FormSubmitEvent((FormPanel) event.getSource());
      listener.onSubmit(fse);
      if (fse.isSetCancelledCalled()) {
        event.setCanceled(fse.isCancelled());
      }
    }

    public void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {
      listener.onSubmitComplete(new FormSubmitCompleteEvent(
          (FormPanel) event.getSource(), event.getResults()));
    }
  }

  public static class Load extends L<LoadListener> implements LoadHandler,
      ErrorHandler {

    public static void remove(Widget eventSource, LoadListener listener) {
      baseRemove(eventSource, listener, LoadEvent.getType(),
          ErrorEvent.getType());
    }

    protected Load(LoadListener listener) {
      super(listener);
    }

    public void onError(ErrorEvent event) {
      listener.onError(source(event));
    }

    public void onLoad(LoadEvent event) {
      listener.onLoad(source(event));
    }
  }
  public static class Mouse extends L<MouseListener> implements
      MouseDownHandler, MouseUpHandler, MouseOutHandler, MouseOverHandler,
      MouseMoveHandler {

    public static <EventSourceType extends Widget & HasMouseDownHandlers & HasMouseUpHandlers & HasMouseOutHandlers & HasMouseOverHandlers & HasMouseMoveHandlers> void add(
        EventSourceType source, MouseListener listener) {
      Mouse handlers = new Mouse(listener);
      source.addMouseDownHandler(handlers);
      source.addMouseUpHandler(handlers);
      source.addMouseOutHandler(handlers);
      source.addMouseOverHandler(handlers);
      source.addMouseMoveHandler(handlers);
    }

    public static void remove(Widget eventSource, MouseListener listener) {
      baseRemove(eventSource, listener, MouseDownEvent.getType(),
          MouseUpEvent.getType(), MouseOverEvent.getType(),
          MouseOutEvent.getType());
    }

    protected Mouse(MouseListener listener) {
      super(listener);
    }

    public void onMouseDown(MouseDownEvent event) {
      listener.onMouseDown(source(event), event.getClientX(),
          event.getScreenY());
    }

    public void onMouseMove(MouseMoveEvent event) {
      listener.onMouseMove(source(event), event.getClientX(),
          event.getClientY());
    }

    public void onMouseOut(MouseOutEvent event) {
      listener.onMouseLeave(source(event));
    }

    public void onMouseOver(MouseOverEvent event) {
      listener.onMouseEnter(source(event));
    }

    public void onMouseUp(MouseUpEvent event) {
      listener.onMouseUp(source(event), event.getClientX(), event.getClientY());
    }
  }

  public static class MouseWheel extends L<MouseWheelListener> implements
      MouseWheelHandler {
    public static void remove(Widget eventSource, MouseWheelListener listener) {
      baseRemove(eventSource, listener, MouseWheelEvent.getType());
    }

    protected MouseWheel(MouseWheelListener listener) {
      super(listener);
    }

    public void onMouseWheel(MouseWheelEvent event) {
      listener.onMouseWheel(source(event), new MouseWheelVelocity(
          event.getNativeEvent()));
    }
  }

  public static class Popup extends L<PopupListener> implements
      CloseHandler<PopupPanel> {

    public static void remove(Widget eventSource, PopupListener listener) {
      baseRemove(eventSource, listener, CloseEvent.getType());
    }

    protected Popup(PopupListener listener) {
      super(listener);
    }

    public void onClose(CloseEvent<PopupPanel> event) {
      listener.onPopupClosed((PopupPanel) event.getSource(),
          event.isAutoClosed());
    }
  }

  public static class Scroll extends L<ScrollListener> implements ScrollHandler {

    public static void remove(Widget eventSource, ScrollListener listener) {
      baseRemove(eventSource, listener, ScrollEvent.getType(),
          ErrorEvent.getType());
    }

    protected Scroll(ScrollListener listener) {
      super(listener);
    }

    public void onScroll(ScrollEvent event) {
      Widget source = source(event);
      Element elem = source.getElement();
      listener.onScroll(source(event), elem.getScrollLeft(),
          elem.getScrollTop());
    }
  }

  public static class Tab extends L<TabListener> implements
      SelectionHandler<Integer>, BeforeSelectionHandler<Integer> {
    @Deprecated
    public static void add(TabBar source, TabListener listener) {
      Tab t = new Tab(listener);
      source.addBeforeSelectionHandler(t);
      source.addSelectionHandler(t);
    }

    public static void add(TabPanel source, TabListener listener) {
      Tab t = new Tab(listener);
      source.addBeforeSelectionHandler(t);
      source.addSelectionHandler(t);
    }

    public static void remove(Widget eventSource, TabListener listener) {
      baseRemove(eventSource, listener, SelectionEvent.getType(),
          BeforeSelectionEvent.getType());
    }

    protected Tab(TabListener listener) {
      super(listener);
    }

    public void onBeforeSelection(BeforeSelectionEvent<Integer> event) {
      if (!listener.onBeforeTabSelected((SourcesTabEvents) event.getSource(),
          event.getItem().intValue())) {
        event.cancel();
      }
    }

    public void onSelection(SelectionEvent<Integer> event) {
      listener.onTabSelected((SourcesTabEvents) event.getSource(),
          event.getSelectedItem().intValue());
    }
  }

  public static class Table extends L<TableListener> implements
      CellClickHandler {
    @Deprecated
    public static void add(HasCellClickHandlers source, TableListener listener) {
      source.addCellClickHandler(new Table(listener));
    }

    public static void remove(Widget eventSource, TableListener listener) {
      baseRemove(eventSource, listener, CellClickEvent.getType());
    }

    protected Table(TableListener listener) {
      super(listener);
    }

    public void onCellClick(CellClickEvent event) {
      listener.onCellClicked((SourcesTableEvents) event.getSource(),
          event.getRowIndex(), event.getCellIndex());
    }
  }

  public static class Tree extends L<TreeListener> implements
      SelectionHandler<TreeItem>, CloseHandler<TreeItem>, OpenHandler<TreeItem> {
    @Deprecated
    public static void add(com.google.gwt.user.client.ui.Tree tree,
        TreeListener listener) {
      Tree t = new Tree(listener);
      tree.addSelectionHandler(t);
      tree.addCloseHandler(t);
      tree.addOpenHandler(t);
    }

    public static void remove(Widget eventSource, TreeListener listener) {
      baseRemove(eventSource, listener, ValueChangeEvent.getType());
    }

    protected Tree(TreeListener listener) {
      super(listener);
    }

    public void onClose(CloseEvent<TreeItem> event) {
      listener.onTreeItemStateChanged(event.getTarget());
    }

    public void onOpen(OpenEvent<TreeItem> event) {
      listener.onTreeItemSelected(event.getTarget());
    }

    public void onSelection(SelectionEvent<TreeItem> event) {
      listener.onTreeItemSelected(event.getSelectedItem());
    }
  }

  static class Keyboard extends L<KeyboardListener> implements KeyDownHandler,
      KeyUpHandler, KeyPressHandler {

    public static <EventSourceType extends Widget & HasAllKeyHandlers> void add(
        EventSourceType source, KeyboardListener listener) {
      AllKeyHandlers.addHandlers(source, new Keyboard(listener));
    }

    public static void remove(Widget eventSource, KeyboardListener listener) {
      L.baseRemove(eventSource, listener, KeyDownEvent.getType(),
          KeyUpEvent.getType(), KeyPressEvent.getType());
    }

    public Keyboard(KeyboardListener listener) {
      super(listener);
    }

    public void onKeyDown(KeyDownEvent event) {
      listener.onKeyDown(source(event), (char) event.getKeyCode(),
          event.getKeyModifiers());
    }

    public void onKeyPress(KeyPressEvent event) {
      listener.onKeyPress(source(event),
          (char) event.getNativeEvent().getKeyCode(), event.getKeyModifiers());
    }

    public void onKeyUp(KeyUpEvent event) {
      source(event);
      listener.onKeyUp(source(event), (char) event.getKeyCode(),
          event.getKeyModifiers());
    }
  }

  // This is an internal helper method with the current formulation, we have
  // lost the info needed to make it safe by this point.
  @SuppressWarnings("unchecked")
  static <H extends EventHandler> void baseRemove(Widget eventSource,
      EventListener listener, Type... keys) {
    HandlerManager manager = eventSource.getHandlers();
    if (manager != null) {
      for (Type<H> key : keys) {
        int handlerCount = manager.getHandlerCount(key);
        for (int i = 0; i < handlerCount; i++) {
          H handler = manager.getHandler(key, i);
          if (handler instanceof L && ((L) handler).listener.equals(listener)) {
            manager.removeHandler(key, handler);
          }
        }
      }
    }
  }

  /**
   * Listener being wrapped.
   */
  protected final T listener;

  protected L(T listener) {
    this.listener = listener;
  }

  Widget source(AbstractEvent<?> event) {
    return (Widget) event.getSource();
  }
}
