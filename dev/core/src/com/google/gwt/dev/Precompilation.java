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
package com.google.gwt.dev;

import com.google.gwt.core.ext.linker.ArtifactSet;
import com.google.gwt.dev.jjs.UnifiedAst;

import java.io.Serializable;
import java.util.Collection;

/**
 * The result of compilation phase 1, includes a unified AST and metadata
 * relevant to each permutation.
 */
public class Precompilation implements Serializable {
  /*
   * TODO: don't make this whole class serializable, instead dump the
   * independent members out to a file so that the generated artifacts are
   * optional to deserialize.
   */
  private ArtifactSet generatedArtifacts;
  private final Permutation[] permutations;
  private final UnifiedAst unifiedAst;

  /**
   * Constructs a new precompilation.  We create new Permutations with
   * a new id so that the ids are consecutive and correspond to the index
   * in the array.
   * 
   * @param unifiedAst the unified AST used by
   *          {@link com.google.gwt.dev.jjs.JavaToJavaScriptCompiler}
   * @param permutations the set of permutations that can be run
   * @param generatedArtifacts the set of artifacts created by generators
   */
  public Precompilation(UnifiedAst unifiedAst,
      Collection<Permutation> permutations, ArtifactSet generatedArtifacts) {
    this.unifiedAst = unifiedAst;
    this.permutations = new Permutation[permutations.size()];
    int i = 0;
    for (Permutation permutation : permutations) {
      this.permutations[i] = new Permutation(i, permutation);
      ++i;
    }
    this.generatedArtifacts = generatedArtifacts;
  }

  /**
   * Returns the set of generated artifacts from the precompile phase.
   */
  public ArtifactSet getGeneratedArtifacts() {
    return generatedArtifacts;
  }

  /**
   * Returns the set of permutations to run.
   */
  public Permutation[] getPermutations() {
    return permutations;
  }

  /**
   * Returns the unified AST used by
   * {@link com.google.gwt.dev.jjs.JavaToJavaScriptCompiler}.
   */
  public UnifiedAst getUnifiedAst() {
    return unifiedAst;
  }
}
