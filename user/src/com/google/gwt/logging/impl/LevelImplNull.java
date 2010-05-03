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

package com.google.gwt.logging.impl;

import java.util.logging.Level;

/**
 * Null implementation for the Level class which ensures that calls to Level
 * compile out when logging is disabled.
 */
public class LevelImplNull implements LevelImpl {

  @Override
  public Level all() {
    return null;
  }

  @Override
  public Level config() {
    return null;
  }

  @Override
  public Level fine() {
    return null;
  }

  @Override
  public Level finer() {
    return null;
  }

  @Override
  public Level finest() {
    return null;
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public Level info() {
    return null;
  }

  @Override
  public int intValue() {
    return 0;
  }

  @Override
  public Level off() {
    return null;
  }

  @Override
  public void setName(String newName) {
  }

  @Override
  public void setValue(int newValue) {
  }

  @Override
  public Level severe() {
    return null;
  }

  @Override
  public Level warning() {
    return null;
  }

}
