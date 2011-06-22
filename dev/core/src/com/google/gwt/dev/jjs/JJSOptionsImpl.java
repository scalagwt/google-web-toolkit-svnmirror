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

import com.google.gwt.dev.util.arg.OptionOptimize;

import java.io.Serializable;

/**
 * Concrete class to implement all JJS options.
 */
public class JJSOptionsImpl implements JJSOptions, Serializable {

  private boolean aggressivelyOptimize = true;
  private boolean compilerMetricsEnabled = false;
  private boolean disableCastChecking = false;
  private boolean disableClassMetadata = false;
  private boolean enableAssertions;
  private boolean enableGeneratorResultCaching = false;
  private int optimizationLevel = OptionOptimize.OPTIMIZE_LEVEL_MAX;
  private boolean optimizePrecompile = false;
  private JsOutputOption output = JsOutputOption.OBFUSCATED;
  private boolean runAsyncEnabled = true;
  private boolean soycEnabled = false;
  private boolean soycExtra = false;
  private boolean soycHtmlDisabled = false;
  private boolean strict = false;

  public JJSOptionsImpl() {
  }

  public JJSOptionsImpl(JJSOptions other) {
    copyFrom(other);
  }

  public void copyFrom(JJSOptions other) {
    setAggressivelyOptimize(other.isAggressivelyOptimize());
    setCastCheckingDisabled(other.isCastCheckingDisabled());
    setClassMetadataDisabled(other.isClassMetadataDisabled());
    setCompilerMetricsEnabled(other.isCompilerMetricsEnabled());
    setEnableAssertions(other.isEnableAssertions());
    setGeneratorResultCachingEnabled(other.isGeneratorResultCachingEnabled());
    setOptimizationLevel(other.getOptimizationLevel());
    setOutput(other.getOutput());
    setRunAsyncEnabled(other.isRunAsyncEnabled());
    setSoycEnabled(other.isSoycEnabled());
    setSoycExtra(other.isSoycExtra());
    setSoycHtmlDisabled(other.isSoycHtmlDisabled());
    setStrict(other.isStrict());
  }

  public int getOptimizationLevel() {
    return optimizationLevel;
  }

  public JsOutputOption getOutput() {
    return output;
  }

  public boolean isAggressivelyOptimize() {
    return aggressivelyOptimize;
  }

  public boolean isCastCheckingDisabled() {
    return disableCastChecking;
  }

  public boolean isClassMetadataDisabled() {
    return disableClassMetadata;
  }

  public boolean isCompilerMetricsEnabled() {
    return compilerMetricsEnabled;
  }

  public boolean isDraftCompile() {
    return optimizationLevel == OptionOptimize.OPTIMIZE_LEVEL_DRAFT;
  }

  public boolean isEnableAssertions() {
    return enableAssertions;
  }

  public boolean isGeneratorResultCachingEnabled() {
    return enableGeneratorResultCaching;
  }

  public boolean isOptimizePrecompile() {
    return optimizePrecompile;
  }

  public boolean isRunAsyncEnabled() {
    return runAsyncEnabled;
  }

  public boolean isSoycEnabled() {
    return soycEnabled;
  }

  public boolean isSoycExtra() {
    return soycExtra;
  }
  
  public boolean isSoycHtmlDisabled() {
    return soycHtmlDisabled;
  }

  public boolean isStrict() {
    return strict;
  }

  public void setAggressivelyOptimize(boolean aggressivelyOptimize) {
    this.aggressivelyOptimize = aggressivelyOptimize;
  }

  public void setCastCheckingDisabled(boolean disabled) {
    disableCastChecking = disabled;
  }

  public void setClassMetadataDisabled(boolean disabled) {
    disableClassMetadata = disabled;
  }

  public void setCompilerMetricsEnabled(boolean enabled) {
    this.compilerMetricsEnabled = enabled;
  }

  public void setEnableAssertions(boolean enableAssertions) {
    this.enableAssertions = enableAssertions;
  }

  public void setGeneratorResultCachingEnabled(boolean enabled) {
    this.enableGeneratorResultCaching = enabled;
  }

  public void setOptimizationLevel(int level) {
    optimizationLevel = level;
  }

  public void setOptimizePrecompile(boolean optimize) {
    optimizePrecompile = optimize;
  }

  public void setOutput(JsOutputOption output) {
    this.output = output;
  }

  public void setRunAsyncEnabled(boolean enabled) {
    runAsyncEnabled = enabled;
  }

  public void setSoycEnabled(boolean enabled) {
    soycEnabled = enabled;
  }

  public void setSoycExtra(boolean enabled) {
    soycExtra = enabled;
  }
  
  public void setSoycHtmlDisabled(boolean disabled) {
    soycHtmlDisabled = disabled;
  }

  public void setStrict(boolean strict) {
    this.strict = strict;
  }
}
