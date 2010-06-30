/*
 * Copyright 2009 Google Inc.
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
 * A Map whose contents may be modified.
 * 
 * The result of calling {@code put} or {@code remove} with {@code null} keys
 * depends on the adapter Relation in use.
 * 
 * Elements in {@code this} map are indexed by Strings. Every access using a key
 * of type {@code <K>} is converted into a String by an "adapter"
 * {@link Relation} provided when creating the Set with
 * {@link CollectionFactory#createMutableMap(Relation)}.
 * 
 * Calls to {@link Relation#applyTo(Object)} should return {@code null} for any
 * object that cannot be a member of the key set. For any two keys in the Map
 * {@code a} and {@code b}, the adapter must meet the condition that {@code
 * adapt(a) == adapt(b)} iff. {@code a.equals(b)}.
 * 
 * For example, suppose we need use Foo objects as keys, which are uniquely
 * identified by an index property provided by {@code Foo.getIndex()}. The
 * following adapter would provide support for these objects as keys in a Map:
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
 * {@link CollectionFactory#createMutableMap()}. This adapter uses
 * {@link Object#toString()} to obtain the index string for any value. {@code
 * null} values are not supported (i.e. the adapter returns {@code null}).
 * 
 * @param <K> the type used to access values stored in the Map
 * @param <V> the type of value stored in the Map
 */
public final class MutableMap<K, V> extends Map<K, V> {
  
  protected MutableMap() {
  }

  /**
   * Removes all entries from this map.
   */
  @LinearTime
  public void clear() {
    Assertions.assertNotFrozen(this);
    jsniClear();
  }

  /**
   * Creates an {@link ImmutableMap} based on this one. Also marks this object
   * as read-only. After calling {@code freeze()}, only use read-only methods to
   * access the elements in this Map.
   */
  @ConstantTime
  public ImmutableMap<K, V> freeze() {
    Assertions.markFrozen(this);
    return this.<ImmutableMap<K,V>>cast();
  }

  // Only meant to be called from within Assertions
  public native boolean isFrozen() /*-{
    return !!this.frozen;
  }-*/;
  
  // Only meant to be called from within Assertions
  public native void markFrozen() /*-{
    this.frozen = true;
  }-*/;
  
  /**
   * Put the value in the map at the given key. {@code key} must be a value
   * accepted by the underlying adapter; that is, a call to {@code
   * adapt(element)} produces a non-null result.
   * 
   * @param key index to the value
   * @param value value to be stored
   */
  @ConstantTime
  public void put(K key, V value) {
    Assertions.assertNotFrozen(this);
    String index = adapt(key);
    assert index != null : Assertions.ACCESS_UNSUPPORTED_VALUE;
    jsniPut(index, value);      
  }

  /**
   * Deletes a key-value entry if the key is a member of the key set. {@code
   * key} must be such that a call to {@code adapt(element)} successfully
   * completes.
   * 
   * @param key index to the key-value
   */
  @ConstantTime
  public void remove(K key) {
    Assertions.assertNotFrozen(this);
    String index = adapt(key);
    if (index != null) {
      jsniRemove(index);
    }
  }

  /**
   * Sets the {@link Relation} to use to translate keys of the Map
   * into Strings. This method should not be called directly. It is used
   * by {@link CollectionFactory#createMutableMap(Relation)}.
   * 
   * @param adapter {@link Relation} from Object to String
   * @exception NullPointerException if {@code adapter} is {@code null}
   */
  void setAdapter(Relation<Object, String> adapter) {
    // TODO Consider allow re-indexing the map
    assert adapter != null : Assertions.ADAPTER_NULL;
    assert isEmpty() : Assertions.INIT_ADAPTER_NON_EMPTY;
    assert !jsniIsAdapterPresent() : Assertions.INIT_ADAPTER_TWICE;
    
    jsniSetAdapter(adapter);
  }
  
  private native void jsniClear() /*-{
    for (k in this) {
      if (k != "adapter" && k != "frozen") {
        delete this[k];
      }
    }
  }-*/;
  
  private native void jsniPut(String index, V value) /*-{
    this[index] = value;
  }-*/;
  
  private native void jsniRemove(String index) /*-{
    delete this[index];
  }-*/;
  
  private native <K,V> void jsniSetAdapter(Relation<K,V> a) /*-{
    this.adapter = a;
  }-*/;

}
