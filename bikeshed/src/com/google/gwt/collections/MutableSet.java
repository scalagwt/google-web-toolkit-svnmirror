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

import java.util.HashMap;
import java.util.Iterator;

/**
 * A {@link Set} whose content can change over time.
 * 
 * Elements in {@code this} Set are indexed by Strings. For each object in the
 * Set, the index String is computed by an "adapter" {@link Relation} provided
 * when creating the Set with
 * {@link CollectionFactory#createMutableSet(Relation)}.
 * 
 * Calls to {@link Relation#applyTo(Object)} should return {@code null} for
 * any object that cannot be a member of the Set. For any two objects to be
 * stored in the Set, {@code a} and {@code b} the adapter must meet
 * the condition that {@code adapt(a) == adapt(b)} iff.
 * {@code a.equals(b)}.
 * 
 * For example, suppose we need to store Foo objects uniquely identified by an
 * index property provided by {@code Foo.getIndex()}. The following adapter
 * would provide support for these objects in a Set:
 * 
 * <pre class="code">
 * <code>
 * public class FooRelation implements Relation&lt;Object, String&gt; {
 *   public String applyTo(Object value) {
 *     return (value instanceof Foo) ? ((Foo) value).getIndex() : null;
 *   }
 * }
 * </code>
 * </pre>
 * 
 * The default adapter is used when creating a Set with
 * {@link CollectionFactory#createMutableSet()}. This adapter uses
 * {@link Object#toString()} to obtain the index string for any value.
 * {@code null} values are not supported (i.e. the adapter returns
 * {@code null}).
 * 
 * Methods that require comparing one set to another implicitly require testing
 * membership of elements of one set into another. Adapters used in each set may
 * differ. For a method of {@code this} set that receives a set
 * {@code source} as parameter, the calls will succeed as long as the
 * following conditions are met:
 * 
 * <ul>
 * <li>For {@link MutableSet#addAll(Set)}, all elements {@code e} in
 * {@code source} must be such that {@code this.adapt(e) != null}</li>
 * 
 * <li>For {@link Set#containsAll}, {@link Set#containsSome(Set)},
 * {@link Set#isEqual(Set)} and MutableSet#removeAll(Set)}, all elements
 * {@code e} in {@code source} must be such that
 * {@code this.adapt(e)} will complete successfully</li>
 * 
 * <li>For {@link MutableSet#keepAll(Set)}, all elements {@code e} in
 * {@code this} must be such that {@code source.adapt(e)} will
 * complete successfully</li>
 * </ul>
 * 
 * @param <E> The type stored in the set elements
 */
public final class MutableSet<E> extends Set<E> {

  /**
   * Adds an {@code element} to this set. {@code element} must be a value
   * accepted by the underlying adapter; that is, a call to {@code
   * adapt(element)} produces a non-null result.
   * 
   * @param element element to add
   */
  public void add(E element) {
    String key = adapt(element);
    assert key != null : Assertions.ACCESS_UNSUPPORTED_VALUE;
    elements.put(key, element);
  }

  /**
   * Adds all elements from {@code source}. This set will contain the union of
   * {@code this} and {@code source} sets.
   * 
   * @param source a set whose contents will be added to this Set
   * @exception NullPointerException if {@code source} is {@code null}
   */
  public void addAll(Set<E> source) {
    if (source == null) {
      throw new NullPointerException("source == null");
    }

    HashMap<String, E> sourceElems = source.elements;
    Iterator<String> i = sourceElems.keySet().iterator();
    
    while (i.hasNext()) {
      add(sourceElems.get(i.next()));
    }
  }

  @Override
  public boolean contains(Object element) {
    String key = adapt(element);
    if (key == null) {
      return false;
    }
    return elements.containsKey(key);
  }

  @Override
  public boolean containsAll(Set<E> source) {
    if (source == null) {
      throw new NullPointerException("source == null");
    }

    HashMap<String, E> sourceElems = source.elements;
    Iterator<String> i = sourceElems.keySet().iterator();
    
    while (i.hasNext()) {
      if (!contains(sourceElems.get(i.next()))) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean containsSome(Set<E> source) {
    if (source == null) {
      throw new NullPointerException("source == null");
    }

    HashMap<String, E> sourceElems = source.elements;
    Iterator<String> i = sourceElems.keySet().iterator();
    
    while (i.hasNext()) {
      if (contains(sourceElems.get(i.next()))) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isEmpty() {
    return elements.isEmpty();
  }

  @Override
  public boolean isEqual(Set<E> source) {
    return source != null 
      && source.elements.size() == elements.size() 
      && containsAll(source);
  }

  /**
   * Removes all elements not in the {@code source} set. This set will keep the
   * intersection of {@code this} and {@code source} sets.
   * 
   * @param source set of elements to keep
   * @exception NullPointerException if {@code source} is {@code null}
   */
  public void keepAll(Set<E> source) {
    if (source == null) {
      throw new NullPointerException("source == null");
    }

    Iterator<String> i = elements.keySet().iterator();
    
    while (i.hasNext()) {
      if (!source.contains(elements.get(i.next()))) {
        i.remove();
      }
    }
  }

  /**
   * Removes an {@code element} from this set. {@code element} must be a value
   * accepted by the underlying adapter; that is, a call to {@code
   * adapt(element)} produces a non-null result.
   * 
   * @param element element to remove
   */
  public void remove(E element) {
    String key = adapt(element);
    if (key == null) {
      return;
    }
    elements.remove(key);
  }

  /**
   * Removes all elements contained in {@code source} from this set. The result
   * will be the subtraction of {@code source} from {@code this} set.
   * 
   * @param source a set whose contents will be removed from this set
   * @exception NullPointerException if {@code source} is {@code null}
   */
  public void removeAll(Set<E> source) {
    if (source == null) {
      throw new NullPointerException("source == null");
    }
    
    HashMap<String, E> sourceElems = source.elements;
    Iterator<String> i = sourceElems.keySet().iterator();
    
    while (i.hasNext()) {
      remove(sourceElems.get(i.next()));
    }
  }
  
  /**
   * Sets the {@link Relation} to use to translate elements of the Set into
   * Strings.
   * 
   * @param adapter {@link Relation} from Object to String
   * @exception IllegalStateException if an adapter has already been set or this
   *            set is not empty
   * @exception NullPointerException if {@code adapter} is {@code null}
   */
  void setAdapter(Relation<Object, String> adapter) {
    // TODO Consider allowing re-indexing the set
    assert this.adapter == null : Assertions.INIT_ADAPTER_TWICE;
    
    if (adapter == null) {
      throw new NullPointerException("adapter == null");
    }
    
    if (this.adapter != null) {
      throw new IllegalStateException("Adapter already set");
    }
    
    if (!isEmpty()) {
      throw new IllegalStateException("Adapter set on non-empty set");
    }

    this.adapter = adapter;
  }

}
