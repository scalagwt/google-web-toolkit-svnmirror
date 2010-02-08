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
package com.google.gwt.dev.jjs.impl.gflow.inlinevar;

import com.google.gwt.dev.jjs.ast.JExpression;
import com.google.gwt.dev.jjs.ast.JVariable;
import com.google.gwt.dev.jjs.impl.gflow.Assumption;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 */
public class InlineVarAssumption implements Assumption<InlineVarAssumption> {
  private Map<JVariable, JExpression> values = new HashMap<JVariable, JExpression>();
  
  public InlineVarAssumption() {
    super();
  }

  public InlineVarAssumption(InlineVarAssumption in) {
    if (in != null) {
      values.putAll(in.values);
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    InlineVarAssumption other = (InlineVarAssumption) obj;
    if (values == null) {
      if (other.values != null) {
        return false;
      }
    } else if (!values.equals(other.values)) {
      return false;
    }
    return true;
  }

  public JExpression get(JVariable target) {
    return values.get(target);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((values == null) ? 0 : values.hashCode());
    return result;
  }

  public InlineVarAssumption join(InlineVarAssumption other) {
    if (other == null || other.values.isEmpty()) {
      return this;
    }
    
    if (values.isEmpty()) {
      return other;
    }
    
    InlineVarAssumption result = new InlineVarAssumption();
    
    for (JVariable v : values.keySet()) {
      JExpression value = values.get(v);
      if (value == other.values.get(v)) {
        result.values.put(v, value);
      } else {
        result.values.put(v, null);
      }
    }
    
    for (JVariable v : other.values.keySet()) {
      if (!result.values.containsKey(v)) {
        result.values.put(v, null);
      }
    }

    return result;
  }

  public void kill(JVariable targetVariable) {
    values.put(targetVariable, null);
  }

  public void killAll() {
    for (JVariable v : values.keySet()) {
      kill(v);
    }
  }
  
  public void setValue(JVariable targetVariable, JExpression value) {
    values.put(targetVariable, value);
  }

  @Override
  public String toString() {
    StringBuffer result = new StringBuffer();
    
    result.append("{");
    List<JVariable> variables = new ArrayList<JVariable>(values.keySet());
    Collections.sort(variables, new Comparator<JVariable>() {
      public int compare(JVariable o1, JVariable o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });
    for (JVariable variable : variables) {
      if (result.length() > 1) {
        result.append(", ");
      }
      result.append(variable.getName());
      result.append(" = ");
      if (values.get(variable) == null) {
        result.append("T");
      } else {
        result.append(values.get(variable));
      }
    }
    result.append("}");
    
    return result.toString();  
  }
}
