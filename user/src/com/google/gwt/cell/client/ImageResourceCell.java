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
package com.google.gwt.cell.client;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

/**
 * An {@link AbstractCell} used to render an {@link ImageResource}.
 *
 * <p>
 * This class assumes that the URL returned from ImageResource is safe from
 * script attacks. If you do not generate the ImageResource from a
 * {@link com.google.gwt.resources.client.ClientBundle ClientBundle}, you should
 * use {@link com.google.gwt.safehtml.shared.UriUtils UriUtils} to sanitize the
 * URL before returning it from {@link ImageResource#getURL()}.
 */
public class ImageResourceCell extends AbstractCell<ImageResource> {
  
  /**
   * Construct a new ImageResourceCell.
   */
  public ImageResourceCell() {
  }

  @Override
  public void render(Context context, ImageResource value, SafeHtmlBuilder sb) {
    if (value != null) {
      SafeHtml html = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(
          value).getHTML());
      sb.append(html);
    }
  }
}
