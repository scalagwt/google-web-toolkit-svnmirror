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
package com.google.gwt.dev.jjs.impl.gflow.cfg;

import com.google.gwt.dev.jjs.impl.gflow.Assumption;

import java.util.Map;

/**
 * Special CfgPrinter implementation which prints assumptions along every edge.
 * 
 * @param <A> assumptions type.
 */
public class AssumptionsPrinter<A extends Assumption<?>> extends CfgPrinter {
  private final Map<CfgEdge, A> assumptions;

  public AssumptionsPrinter(Cfg graph, Map<CfgEdge, A> assumptions) {
    super(graph);
    this.assumptions = assumptions;
  }

  @Override
  protected void appendEdgeInfo(StringBuffer result, CfgEdge edge) {
    A a = assumptions.get(edge);
    if (a != null) {
      result.append(" ");
      result.append(a.toString());
    }
  }
}