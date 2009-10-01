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
package com.google.gwt.dev.cfg;

import com.google.gwt.dev.util.collect.Sets;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Represents a single named deferred binding or configuration property that can
 * answer with its value. The BindingProperty maintains two sets of values, the
 * "defined" set and the "allowed" set. The allowed set must always be a subset
 * of the defined set.
 */
public class BindingProperty extends Property {

  private static final String EMPTY = "";

  private final Map<Condition, SortedSet<String>> conditionalValues = new LinkedHashMap<Condition, SortedSet<String>>();
  private final SortedSet<String> definedValues = new TreeSet<String>();
  private PropertyProvider provider;
  private String fallback;
  private final ConditionAll rootCondition = new ConditionAll();

  {
    conditionalValues.put(rootCondition, new TreeSet<String>());
  }

  public BindingProperty(String name) {
    super(name);
    fallback = EMPTY;
  }

  public void addDefinedValue(Condition condition, String newValue) {
    definedValues.add(newValue);
    SortedSet<String> set = conditionalValues.get(condition);
    if (set == null) {
      set = new TreeSet<String>();
      set.addAll(conditionalValues.get(rootCondition));
      conditionalValues.put(condition, set);
    }
    set.add(newValue);
  }

  /**
   * Returns the set of allowed values in sorted order when a certain condition
   * is satisfied.
   */
  public String[] getAllowedValues(Condition condition) {
    Set<String> allowedValues = conditionalValues.get(condition);
    return allowedValues.toArray(new String[allowedValues.size()]);
  }

  public Map<Condition, SortedSet<String>> getConditionalValues() {
    return Collections.unmodifiableMap(conditionalValues);
  }

  /**
   * If the BindingProperty has exactly one value across all conditions and
   * permutations, return that value otherwise return <code>null</code>.
   */
  public String getConstrainedValue() {
    String constrainedValue = null;
    for (SortedSet<String> allowedValues : conditionalValues.values()) {
      if (allowedValues.size() != 1) {
        return null;
      } else if (constrainedValue == null) {
        constrainedValue = allowedValues.iterator().next();
      } else if (!constrainedValue.equals(allowedValues.iterator().next())) {
        return null;
      }
    }
    return constrainedValue;
  }

  /**
   * Returns the set of defined values in sorted order.
   */
  public String[] getDefinedValues() {
    return definedValues.toArray(new String[definedValues.size()]);
  }

  public String getFallback() {
    return fallback;
  }

  public PropertyProvider getProvider() {
    return provider;
  }

  public Set<String> getRequiredProperties() {
    Set<String> toReturn = Sets.create();
    for (Condition cond : conditionalValues.keySet()) {
      toReturn = Sets.addAll(toReturn, cond.getRequiredProperties());
    }
    return toReturn;
  }

  public ConditionAll getRootCondition() {
    return rootCondition;
  }

  /**
   * Returns true if the supplied value is legal under some condition.
   */
  public boolean isAllowedValue(String value) {
    for (Set<String> values : conditionalValues.values()) {
      if (values.contains(value)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns <code>true</code> if the value was previously provided to
   * {@link #addDefinedValue(String)} since the last time {@link #clearValues()}
   * was called.
   */
  public boolean isDefinedValue(String value) {
    return definedValues.contains(value);
  }

  /**
   * Returns <code>true</code> if the value of this BindingProperty is always
   * derived from other BindingProperties. That is, for each Condition in the
   * BindingProperty, there is exactly one allowed value.
   */
  public boolean isDerived() {
    for (Set<String> allowedValues : conditionalValues.values()) {
      if (allowedValues.size() != 1) {
        return false;
      }
    }
    return true;
  }

  /**
   * Set the currently allowed values. The values provided must be a subset of
   * the currently-defined values.
   * 
   * @throws IllegalArgumentException if any of the provided values were not
   *           provided to {@link #addDefinedValue(String)}.
   */
  public void setAllowedValues(Condition condition, String... values) {
    SortedSet<String> temp = new TreeSet<String>(Arrays.asList(values));
    if (!definedValues.containsAll(temp)) {
      throw new IllegalArgumentException(
          "Attempted to set an allowed value that was not previously defined");
    }

    // XML has a last-one-wins semantic which we reflect in our evaluation order
    if (condition == rootCondition) {
      /*
       * An unconditional set-property would undo any previous conditional
       * setters, so we can just clear out this map.
       */
      conditionalValues.clear();
    } else {
      /*
       * Otherwise, we'll just ensure that this condition is moved to the end.
       */
      conditionalValues.remove(condition);
    }
    conditionalValues.put(condition, temp);
  }

  public void setFallback(String token) {
    fallback = token;
  }

  public void setProvider(PropertyProvider provider) {
    this.provider = provider;
  }
}
