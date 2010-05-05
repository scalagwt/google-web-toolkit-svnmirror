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
package com.google.gwt.bikeshed.list.client;

/**
 * Interface that must be implemented by {@link com.google.gwt.cell.client.Cell}
 * containers.
 */
public interface HasViewData {

  /**
   * Gets the view data associated with the given item.
   * 
   * @param key the key of the item whose view data is desired
   * @return the view data
   */
  Object getViewData(Object key);

  /**
   * Sets the view data associated with the given item.
   * 
   * @param key the key of the item whose view data will be set
   * @param viewData the view data
   */
  void setViewData(Object key, Object viewData);
}
