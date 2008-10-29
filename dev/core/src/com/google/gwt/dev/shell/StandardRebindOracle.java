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
package com.google.gwt.dev.shell;

import com.google.gwt.core.ext.PropertyOracle;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.linker.ArtifactSet;
import com.google.gwt.dev.cfg.PublicOracle;
import com.google.gwt.dev.cfg.Rule;
import com.google.gwt.dev.cfg.Rules;
import com.google.gwt.dev.javac.CompilationState;
import com.google.gwt.dev.jdt.RebindOracle;
import com.google.gwt.dev.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implements rebind logic in terms of a variety of other well-known oracles.
 */
public class StandardRebindOracle implements RebindOracle {

  /**
   * A call-back interface to be notified when new types are generated.
   * 
   */
  public interface ArtifactAcceptor {
    /**
     * Called if new artifacts are generated.
     */
    void accept(ArtifactSet newlyGeneratedArtifacts)
        throws UnableToCompleteException;
  }

  /**
   * Makes the actual deferred binding decision by examining rules.
   */
  private final class Rebinder {

    private final StandardGeneratorContext genCtx;

    private final Set<Rule> usedRules = new HashSet<Rule>();

    private final List<String> usedTypeNames = new ArrayList<String>();

    public Rebinder() {
      genCtx = new StandardGeneratorContext(compilationState, propOracle,
          publicOracle, genDir, generatorResourcesDir, allGeneratedArtifacts);
    }

    public String rebind(TreeLogger logger, String typeName,
        ArtifactAcceptor artifactAcceptor) throws UnableToCompleteException {

      String result = tryRebind(logger, typeName);
      ArtifactSet newlyGeneratedArtifacts = genCtx.finish(logger);
      if (!newlyGeneratedArtifacts.isEmpty() && artifactAcceptor != null) {
        artifactAcceptor.accept(newlyGeneratedArtifacts);
      }
      if (result == null) {
        result = typeName;
      }
      return result;
    }

    private String tryRebind(TreeLogger logger, String typeName)
        throws UnableToCompleteException {
      if (usedTypeNames.contains(typeName)) {
        // Found a cycle.
        //
        String[] cycle = Util.toArray(String.class, usedTypeNames);
        Messages.UNABLE_TO_REBIND_DUE_TO_CYCLE_IN_RULES.log(logger, cycle, null);
        throw new UnableToCompleteException();
      }

      // Remember that we've seen this one.
      //
      usedTypeNames.add(typeName);

      // Make the rebind decision.
      //
      if (rules.isEmpty()) {
        logger.log(TreeLogger.DEBUG,
            "No rules are defined, so no substitution can occur", null);
        return null;
      }

      for (Iterator<Rule> iter = rules.iterator(); iter.hasNext();) {
        Rule rule = iter.next();

        // Branch the logger.
        //
        TreeLogger branch = Messages.TRACE_CHECKING_RULE.branch(logger, rule,
            null);

        if (rule.isApplicable(branch, genCtx, typeName)) {
          // See if this rule has already been used. This is needed to prevent
          // infinite loops with 'when-assignable' conditions.
          //
          if (!usedRules.contains(rule)) {
            usedRules.add(rule);
            Messages.TRACE_RULE_MATCHED.log(logger, null);

            // Invoke the rule.
            //
            return rule.realize(logger, genCtx, typeName);

          } else {
            // We are skipping this rule because it has already been used
            // in a previous iteration.
            //
          }
        } else {
          Messages.TRACE_RULE_DID_NOT_MATCH.log(logger, null);
        }
      }

      // No matching rule for this type.
      //
      return null;
    }
  }

  private final ArtifactSet allGeneratedArtifacts;

  private final Map<String, String> cache = new HashMap<String, String>();

  private final CompilationState compilationState;

  private final File genDir;

  private final File generatorResourcesDir;

  private final PropertyOracle propOracle;

  private final PublicOracle publicOracle;

  private final Rules rules;

  public StandardRebindOracle(CompilationState compilationState,
      PropertyOracle propOracle, PublicOracle publicOracle, Rules rules,
      File genDir, File generatorResourcesDir, ArtifactSet allGeneratedArtifacts) {
    this.compilationState = compilationState;
    this.propOracle = propOracle;
    this.publicOracle = publicOracle;
    this.rules = rules;
    this.genDir = genDir;
    this.generatorResourcesDir = generatorResourcesDir;
    this.allGeneratedArtifacts = allGeneratedArtifacts;
  }

  public String rebind(TreeLogger logger, String typeName)
      throws UnableToCompleteException {
    return rebind(logger, typeName, null);
  }

  public String rebind(TreeLogger logger, String typeName,
      ArtifactAcceptor artifactAcceptor) throws UnableToCompleteException {

    String result = cache.get(typeName);
    if (result == null) {
      logger = Messages.TRACE_TOPLEVEL_REBIND.branch(logger, typeName, null);

      Rebinder rebinder = new Rebinder();
      result = rebinder.rebind(logger, typeName, artifactAcceptor);
      cache.put(typeName, result);

      Messages.TRACE_TOPLEVEL_REBIND_RESULT.log(logger, result, null);
    }
    return result;
  }
}
