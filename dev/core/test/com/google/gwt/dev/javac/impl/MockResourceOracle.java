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
package com.google.gwt.dev.javac.impl;

import com.google.gwt.dev.resource.Resource;
import com.google.gwt.dev.resource.ResourceOracle;

import junit.framework.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A simple {@link ResourceOracle} for testing.
 */
public class MockResourceOracle implements ResourceOracle {

  private Map<String, Resource> exportedMap = Collections.emptyMap();
  private Set<Resource> exportedValues = Collections.emptySet();

  public MockResourceOracle(Resource... resources) {
    add(resources);
  }

  public Set<String> getPathNames() {
    return exportedMap.keySet();
  }

  public Map<String, Resource> getResourceMap() {
    return exportedMap;
  }

  public Set<Resource> getResources() {
    return exportedValues;
  }

  void add(Resource... resources) {
    Map<String, Resource> newMap = new HashMap<String, Resource>(exportedMap);
    for (Resource resource : resources) {
      String path = resource.getPath();
      Assert.assertFalse(newMap.containsKey(path));
      newMap.put(path, resource);
    }
    export(newMap);
  }

  void remove(String... paths) {
    Map<String, Resource> newMap = new HashMap<String, Resource>(exportedMap);
    for (String path : paths) {
      Resource oldValue = newMap.remove(path);
      Assert.assertNotNull(oldValue);
    }
    export(newMap);
  }

  void replace(Resource... resources) {
    Map<String, Resource> newMap = new HashMap<String, Resource>(exportedMap);
    for (Resource resource : resources) {
      String path = resource.getPath();
      Assert.assertTrue(newMap.containsKey(path));
      newMap.put(path, resource);
    }
    export(newMap);
  }

  private void export(Map<String, Resource> newMap) {
    exportedMap = Collections.unmodifiableMap(newMap);
    // Make a new hash set for constant lookup.
    exportedValues = Collections.unmodifiableSet(new HashSet<Resource>(
        exportedMap.values()));
  }

}