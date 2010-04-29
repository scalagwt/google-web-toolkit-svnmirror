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
package com.google.gwt.bikeshed.cells.client;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

/**
 * A {@link Cell} decorator that adds an icon to another Cell.
 * 
 * @param <C> the type that this Cell represents
 */
public class IconCellDecorator<C> extends Cell<C> {

  private final Cell<C> cell;
  private final String iconHtml;
  private final String placeHolderHtml;
  private final String valign;

  /**
   * Construct a new {@link IconCellDecorator}. The icon and the content will be
   * middle aligned by default.
   * 
   * @param icon the icon to use
   * @param cell the cell to decorate
   */
  public IconCellDecorator(ImageResource icon, Cell<C> cell) {
    this(icon, cell, "middle");
  }

  /**
   * Construct a new {@link IconCellDecorator}.
   * 
   * @param icon the icon to use
   * @param cell the cell to decorate
   * @param valign the vertical alignment attribute of the contents
   */
  public IconCellDecorator(ImageResource icon, Cell<C> cell, String valign) {
    this.cell = cell;
    this.iconHtml = AbstractImagePrototype.create(icon).getHTML();
    this.valign = valign;

    // A div element won't line up correctly, so we use a copy of the img
    // element, but without a background.
    placeHolderHtml = iconHtml.replace("background:", "nobackground:");
  }

  @Override
  public boolean consumesEvents() {
    return cell.consumesEvents();
  }

  @Override
  public boolean dependsOnSelection() {
    return cell.dependsOnSelection();
  }

  @Override
  public Object onBrowserEvent(Element parent, C value, Object viewData,
      NativeEvent event, ValueUpdater<C> valueUpdater) {
    return cell.onBrowserEvent(parent, value, viewData, event, valueUpdater);
  }

  @Override
  public void render(C value, Object viewData, StringBuilder sb) {
    sb.append("<table><tr>");
    sb.append("<td valign='").append(valign).append("'>");
    if (isIconUsed(value)) {
      sb.append(getIconHtml(value));
    } else {
      sb.append(placeHolderHtml);
    }
    sb.append("</td><td valign='").append(valign).append("'>");
    cell.render(value, viewData, sb);
    sb.append("</td>");
    sb.append("</tr></table>");
  }

  @Override
  public void setValue(Element parent, C value, Object viewData) {
    cell.setValue(parent, value, viewData);
  }

  /**
   * Get the HTML string that represents the icon. Override this method to
   * change the icon based on the value.
   * 
   * @param value the value being rendered
   * @return the HTML string that represents the icon
   */
  protected String getIconHtml(C value) {
    return iconHtml;
  }

  /**
   * Check if the icon should be used for the value. If the icon should not be
   * used, a placeholder of the same size will be used instead. The default
   * implementations returns true.
   * 
   * @param value the value being rendered
   * @return true to use the icon, false to use a placeholder
   */
  protected boolean isIconUsed(C value) {
    return true;
  }
}
