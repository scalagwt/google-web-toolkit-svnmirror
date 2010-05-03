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
 * Implementation for the Level class when logging is enabled.
 */
public class LevelImplRegular implements LevelImpl {
  
  /** 
   * Since the Impl class is in a different package than the Level class, we
   * need to work around the fact that the Impl class cannot access the
   * protected Level constructor.
   */
  private static class LevelWithExposedConstructor extends Level {
    public LevelWithExposedConstructor(String name, int value) {
      super(name, value);
    }
  }

  private static Level ALL = 
    new LevelWithExposedConstructor("ALL", Integer.MIN_VALUE); 
  private static Level CONFIG =
    new LevelWithExposedConstructor("CONFIG", 700); 
  private static Level FINE =
    new LevelWithExposedConstructor("FINE", 500); 
  private static Level FINER =
    new LevelWithExposedConstructor("FINER", 400); 
  private static Level FINEST =
    new LevelWithExposedConstructor("FINEST", 300); 
  private static Level INFO =
    new LevelWithExposedConstructor("INFO", 800); 
  private static Level OFF =
    new LevelWithExposedConstructor("OFF", Integer.MAX_VALUE); 
  private static Level SEVERE =
    new LevelWithExposedConstructor("SEVERE", 1000);
  private static Level WARNING =
    new LevelWithExposedConstructor("WARNING", 900); 
  
  private String name;
  private int value;

  public LevelImplRegular() { }
  
  @Override
  public Level all() {
    return LevelImplRegular.ALL;
  }
  
  @Override
  public Level config() {
    return LevelImplRegular.CONFIG;
  }
    
  @Override
  public Level fine() {
    return LevelImplRegular.FINE;
  }
  
  @Override
  public Level finer() {
    return LevelImplRegular.FINER;
  }
  
  @Override
  public Level finest() {
    return LevelImplRegular.FINEST;
  }

  public String getName() {
    return name;
  }

  @Override
  public Level info() {
    return LevelImplRegular.INFO;
  }

  public int intValue() {
    return value;
  }

  @Override
  public Level off() {
    return LevelImplRegular.OFF;
  }

  public void setName(String newName) {
    name = newName;
  }

  public void setValue(int newValue) {
    value = newValue;
  }

  @Override
  public Level severe() {
    return LevelImplRegular.SEVERE;
  }

  @Override
  public String toString() {
    return getName();
  }

  @Override
  public Level warning() {
    return LevelImplRegular.WARNING;
  }
}
