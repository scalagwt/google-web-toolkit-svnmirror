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
package com.google.gwt.dev.jjs;

import com.google.gwt.core.ext.PropertyOracle;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.dev.jjs.ast.JProgram;
import com.google.gwt.dev.js.ast.JsProgram;
import com.google.gwt.dev.util.DiskCache;
import com.google.gwt.dev.util.Util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Represents a unified, non-permutation specific AST. This AST is used to drive
 * per-permutation compiles.
 */
public class UnifiedAst implements Serializable {

  /**
   * Encapsulates the combined programs.
   */
  static final class AST implements Serializable {
    private final JProgram jProgram;
    private final JsProgram jsProgram;

    public AST(JProgram jProgram, JsProgram jsProgram) {
      this.jProgram = jProgram;
      this.jsProgram = jsProgram;
    }

    JProgram getJProgram() {
      return jProgram;
    }

    JsProgram getJsProgram() {
      return jsProgram;
    }
  }

  private static final DiskCache diskCache = new DiskCache();

  /**
   * The original AST; nulled out once consumed (by the first call to
   * {@link #getFreshAst()}.
   */
  private transient AST initialAst;

  /**
   * Used for internal synchronization.
   */
  private transient Object myLockObject = new Object();

  /**
   * The compilation options.
   */
  private final JJSOptions options;

  /**
   * The set of all live rebind request types in the AST.
   */
  private final SortedSet<String> rebindRequests;

  /**
   * The serialized AST.
   */
  private transient long serializedAstToken;

  public UnifiedAst(JJSOptions options, AST initialAst,
      boolean singlePermutation, Set<String> rebindRequests) {
    this.options = new JJSOptionsImpl(options);
    this.initialAst = initialAst;
    this.rebindRequests = Collections.unmodifiableSortedSet(new TreeSet<String>(
        rebindRequests));
    this.serializedAstToken = singlePermutation ? -1
        : diskCache.writeObject(initialAst);
  }

  /**
   * Copy constructor, invalidates the original.
   */
  UnifiedAst(UnifiedAst other) {
    this.options = other.options;
    this.initialAst = other.initialAst;
    other.initialAst = null; // steal its copy
    this.rebindRequests = other.rebindRequests;
    this.serializedAstToken = other.serializedAstToken;
  }

  /**
   * Compiles a particular permutation.
   * 
   * @param logger the logger to use
   * @param rebindAnswers the set of rebind answers to resolve all outstanding
   *          rebind decisions for this permutation
   * @param propertyOracles all property oracles corresponding to this
   *          permutation
   * @param permutationId the unique id of this permutation
   * @return the permutation result
   * @throws UnableToCompleteException if an error other than
   *           {@link OutOfMemoryError} occurs
   */
  public PermutationResult compilePermutation(TreeLogger logger,
      Map<String, String> rebindAnswers, PropertyOracle[] propertyOracles,
      int permutationId) throws UnableToCompleteException {
    return JavaToJavaScriptCompiler.compilePermutation(logger, this,
        rebindAnswers, propertyOracles, permutationId);
  }

  /**
   * Returns the active set of JJS options associated with this compile.
   */
  public JJSOptions getOptions() {
    return new JJSOptionsImpl(options);
  }

  /**
   * Returns the set of live rebind requests in the AST.
   */
  public SortedSet<String> getRebindRequests() {
    return rebindRequests;
  }

  /**
   * Internally prepares a new AST for compilation if one is not already
   * prepared.
   */
  public void prepare() {
    synchronized (myLockObject) {
      if (initialAst == null) {
        initialAst = diskCache.readObject(serializedAstToken, AST.class);
      }
    }
  }

  AST getFreshAst() {
    synchronized (myLockObject) {
      if (initialAst != null) {
        AST result = initialAst;
        initialAst = null;
        return result;
      } else {
        if (serializedAstToken < 0) {
          throw new IllegalStateException(
              "No serialized AST was cached and AST was already consumed.");
        }
        return diskCache.readObject(serializedAstToken, AST.class);
      }
    }
  }

  /**
   * Re-initialize lock object; copy serialized AST straight to cache.
   */
  private void readObject(ObjectInputStream stream) throws IOException,
      ClassNotFoundException {
    stream.defaultReadObject();
    myLockObject = new Object();
    serializedAstToken = diskCache.transferFromStream(stream);
  }

  /**
   * Force byte serialization of AST before writing.
   */
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    if (serializedAstToken >= 0) {
      // Copy the bytes.
      diskCache.transferToStream(serializedAstToken, stream);
    } else if (initialAst != null) {
      // Serialize into raw bytes.
      Util.writeObjectToStream(stream, initialAst);
    } else {
      throw new IllegalStateException(
          "No serialized AST was cached and AST was already consumed.");
    }
  }
}
