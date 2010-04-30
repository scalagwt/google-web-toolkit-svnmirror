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
package com.google.gwt.sample.bikeshed.style.client;

import com.google.gwt.bikeshed.list.client.CellList;
import com.google.gwt.bikeshed.list.client.CellTable;
import com.google.gwt.bikeshed.list.client.SimplePager;
import com.google.gwt.bikeshed.tree.client.CellBrowser;
import com.google.gwt.bikeshed.tree.client.CellTree;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.CssResource.NotStrict;

/**
 * The Styles used in bikeshed.
 */
public class Styles {

  /**
   * Common styles.
   */
  public interface Common extends CssResource {
    String box();

    String header();

    String headerLeft();

    String headerMain();

    String padded();

    String table();
  }

  /**
   * Shared resources.
   */
  public interface Resources extends ClientBundle, CellBrowser.Resources,
      CellList.Resources, CellTable.Resources, CellTree.Resources,
      SimplePager.Resources {

    @NotStrict
    @Source("common.css")
    Common common();

    /**
     * Icon used to represent a user group.
     */
    ImageResource groupIcon();

    /**
     * Icon used to represent a user.
     */
    ImageResource userIcon();
  }

  private static Resources resources;

  static {
    resources = GWT.create(Resources.class);
    resources.common().ensureInjected();
  }

  public static Common common() {
    return resources.common();
  }

  public static Resources resources() {
    return resources;
  }
}
