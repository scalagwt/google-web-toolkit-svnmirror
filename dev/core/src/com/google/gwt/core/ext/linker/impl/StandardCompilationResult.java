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
package com.google.gwt.core.ext.linker.impl;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.linker.CompilationResult;
import com.google.gwt.core.ext.linker.SelectionProperty;
import com.google.gwt.dev.util.Util;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * The standard implementation of {@link CompilationResult}.
 */
public class StandardCompilationResult extends CompilationResult {

  /**
   * Smaller maps come before larger maps, then we compare the concatenation of
   * every value.
   */
  public static final Comparator<SortedMap<SelectionProperty, String>> MAP_COMPARATOR = new Comparator<SortedMap<SelectionProperty, String>>() {
    public int compare(SortedMap<SelectionProperty, String> arg0,
        SortedMap<SelectionProperty, String> arg1) {
      int diff = arg0.size() - arg1.size();
      if (diff != 0) {
        return diff;
      }

      Iterator<String> i0 = arg0.values().iterator();
      Iterator<String> i1 = arg1.values().iterator();

      StringBuffer sb0 = new StringBuffer();
      StringBuffer sb1 = new StringBuffer();

      while (i0.hasNext()) {
        assert i1.hasNext();
        sb0.append(i0.next());
        sb1.append(i1.next());
      }
      assert !i1.hasNext();

      return sb0.toString().compareTo(sb1.toString());
    }
  };

  private final File cacheFile;
  private SoftReference<String> js;
  private final SortedSet<SortedMap<SelectionProperty, String>> propertyValues = new TreeSet<SortedMap<SelectionProperty, String>>(
      MAP_COMPARATOR);
  private final String strongName;

  public StandardCompilationResult(TreeLogger logger, String js, File cacheDir)
      throws UnableToCompleteException {
    super(StandardLinkerContext.class);
    this.js = new SoftReference<String>(js);

    byte[] bytes = Util.getBytes(js);
    strongName = Util.computeStrongName(bytes);
    cacheFile = new File(cacheDir, strongName);

    Util.writeBytesToFile(logger, cacheFile, bytes);
  }

  /**
   * Record a particular permutation of SelectionProperty values that resulted
   * in the compilation.
   */
  public void addSelectionPermutation(Map<SelectionProperty, String> values) {
    SortedMap<SelectionProperty, String> map = new TreeMap<SelectionProperty, String>(
        StandardLinkerContext.SELECTION_PROPERTY_COMPARATOR);
    map.putAll(values);
    propertyValues.add(Collections.unmodifiableSortedMap(map));
  }

  public String getJavaScript() {
    String toReturn = js.get();
    if (toReturn == null) {
      toReturn = Util.readFileAsString(cacheFile);
      js = new SoftReference<String>(toReturn);
    }
    return toReturn;
  }

  public SortedSet<SortedMap<SelectionProperty, String>> getPropertyMap() {
    return Collections.unmodifiableSortedSet(propertyValues);
  }

  public String getStrongName() {
    return strongName;
  }
}
