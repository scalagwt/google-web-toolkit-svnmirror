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

import com.google.gwt.i18n.client.DateTimeFormat;

import java.util.Date;

/**
 * A {@link Cell} used to render {@link Date}s.
 */
public class DateCell extends Cell<Date> {

  private final DateTimeFormat format;

  public DateCell() {
    this(DateTimeFormat.getFullDateFormat());
  }

  public DateCell(DateTimeFormat format) {
    this.format = format;
  }

  @Override
  public void render(Date value, Object viewData, StringBuilder sb) {
    if (value != null) {
      sb.append(format.format(value));
    }
  }
}
