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
package com.google.gwt.uibinder.parsers;

import com.google.gwt.uibinder.rebind.UiBinderWriter;

import java.util.HashMap;

/**
 * Parses a
 * {@link com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant}.
 */
public class VerticalAlignmentConstantParser implements AttributeParser {

  private static final HashMap<String, String> values =
    new HashMap<String, String>();

  static {
    values.put("ALIGN_TOP",
      "com.google.gwt.user.client.ui.HasVerticalAlignment.ALIGN_TOP");
    values.put("ALIGN_MIDDLE",
      "com.google.gwt.user.client.ui.HasVerticalAlignment.ALIGN_MIDDLE");
    values.put("ALIGN_BOTTOM",
      "com.google.gwt.user.client.ui.HasVerticalAlignment.ALIGN_BOTTOM");
  }

  public String parse(String value, UiBinderWriter writer) {
    String translated = values.get(value);
    if (translated == null) {
      throw new RuntimeException("Invalid value: vorizontalAlignment='"
        + value + "'");
    }
    return translated;
  }
}
