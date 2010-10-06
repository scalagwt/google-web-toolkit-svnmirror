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
package com.google.gwt.editor.client.adapters;

import com.google.gwt.editor.client.CompositeEditor;
import com.google.gwt.editor.client.Editor;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * Synchronizes a list of objects and a list of Editors.
 * 
 * @param <T> the type of data being edited
 * @param <E> the type of Editor
 */
class ListEditorWrapper<T, E extends Editor<T>> extends AbstractList<T> {
  private final List<T> backing;
  private final CompositeEditor.EditorChain<T, E> chain;
  private final List<E> editors;
  private final EditorSource<E> editorSource;

  public ListEditorWrapper(List<T> backing,
      CompositeEditor.EditorChain<T, E> chain, EditorSource<E> editorSource) {
    this.backing = backing;
    this.chain = chain;
    this.editorSource = editorSource;
    editors = new ArrayList<E>(backing.size());
  }

  @Override
  public void add(int index, T element) {
    backing.add(index, element);
    E subEditor = editorSource.create(index);
    editors.add(index, subEditor);
    for (int i = index + 1, j = editors.size(); i < j; i++) {
      editorSource.setIndex(editors.get(i), i);
    }
    chain.attach(element, subEditor);
  }

  @Override
  public T get(int index) {
    return backing.get(index);
  }

  @Override
  public T remove(int index) {
    // Try to mutate the list first, in case it is immutable
    T toReturn = backing.remove(index);
    E subEditor = editors.remove(index);
    editorSource.dispose(subEditor);
    for (int i = index, j = editors.size(); i < j; i++) {
      editorSource.setIndex(editors.get(i), i);
    }
    chain.detach(subEditor);
    return toReturn;
  }

  @Override
  public T set(int index, T element) {
    // Try to mutate the list first, in case it is immutable
    T toReturn = backing.set(index, element);
    chain.attach(element, editors.get(index));
    return toReturn;
  }

  @Override
  public int size() {
    return backing.size();
  }

  /**
   * Must be called after construction. This is a two-phase initialization so
   * that ListEditor can assign its list field before any sub-editors might call
   * {@link ListEditor#getList()}
   */
  void attach() {
    editors.addAll(editorSource.create(backing.size(), 0));
    for (int i = 0, j = backing.size(); i < j; i++) {
      chain.attach(backing.get(i), editors.get(i));
    }
  }

  void detach() {
    for (int i = 0, j = editors.size(); i < j; i++) {
      chain.detach(editors.get(i));
      editorSource.dispose(editors.get(i));
    }
  }

  void flush() {
    for (int i = 0, j = backing.size(); i < j; i++) {
      E subEditor = editors.get(i);
      T value = chain.getValue(subEditor);
      // Use of object-identity intentional
      if (backing.get(i) != value) {
        backing.set(i, value);
      }
    }
  }

  /**
   * For testing only.
   */
  List<? extends E> getEditors() {
    return editors;
  }
}