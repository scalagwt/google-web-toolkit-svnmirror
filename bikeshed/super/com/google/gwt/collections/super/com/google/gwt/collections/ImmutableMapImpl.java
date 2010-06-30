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
package com.google.gwt.collections;

/**
 * A map that is always empty. Byte code implementation.
 * 
 * @param <K> the type used to access values stored in the Map
 * @param <V> the type of values stored in the Map
 */
public class ImmutableMapImpl<K, V> extends ImmutableMap<K, V> {
  
  protected ImmutableMapImpl() {
  }

}
