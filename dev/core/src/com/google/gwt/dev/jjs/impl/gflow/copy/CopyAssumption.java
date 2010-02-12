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
package com.google.gwt.dev.jjs.impl.gflow.copy;

import com.google.gwt.dev.jjs.ast.HasName;
import com.google.gwt.dev.jjs.ast.JVariable;
import com.google.gwt.dev.jjs.impl.gflow.Assumption;
import com.google.gwt.dev.util.collect.Lists;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Assumption class for CopyAnalysis.
 */
public class CopyAssumption implements Assumption<CopyAssumption> {
  /**
   * Updates the assumption by copying it on first write.
   */
  public static class Updater {
    private CopyAssumption assumption;
    private boolean copied = false;
    
    public Updater(CopyAssumption assumption) {
      this.assumption = assumption;
    }

    public void addCopy(JVariable original, JVariable targetVariable) {
      copyIfNeeded();
      assumption.addCopy(original, targetVariable);
    }

    public void kill(JVariable targetVariable) {
      copyIfNeeded();
      assumption.kill(targetVariable);
    }

    public CopyAssumption unwrap() {
      if (assumption != null && assumption.copyToOriginal.isEmpty()) {
        return null;
      }
      return assumption;
    }

    private void copyIfNeeded() {
      if (!copied) {
        assumption = new CopyAssumption(assumption);
        copied = true;
      }
    }
  }
    
  /**
   * Map from copies to original values.
   */
  private final Map<JVariable, JVariable> copyToOriginal;
  
  public CopyAssumption() {
    copyToOriginal = new IdentityHashMap<JVariable, JVariable>();
  }

  public CopyAssumption(CopyAssumption result) {
    if (result != null) {
      copyToOriginal = new IdentityHashMap<JVariable, JVariable>(result.copyToOriginal);
    } else {
      copyToOriginal = new IdentityHashMap<JVariable, JVariable>();
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
    CopyAssumption other = (CopyAssumption) obj;
    return other.copyToOriginal.equals(copyToOriginal);
  }

  public JVariable getOriginal(JVariable v) {
    return copyToOriginal.get(v);
  }

  @Override
  public int hashCode() {
    return copyToOriginal.hashCode();
  }

  public CopyAssumption join(CopyAssumption value) {
    if (value == null || value.copyToOriginal.isEmpty()) {
      return this;
    }
    
    if (copyToOriginal.isEmpty()) {
      return value;
    }
    
    CopyAssumption result = new CopyAssumption();
    
    for (JVariable v : copyToOriginal.keySet()) {
      JVariable original = copyToOriginal.get(v);
      if (original == value.copyToOriginal.get(v)) {
        result.copyToOriginal.put(v, original);
      } else {
        result.copyToOriginal.put(v, null);
      }
    }
    
    return result;
  }

  @Override
  public String toString() {
    StringBuffer result = new StringBuffer();
    
    result.append("{");
    List<JVariable> variables = new ArrayList<JVariable>(
        copyToOriginal.keySet());
    HasName.Util.sortByName(variables);
    for (JVariable variable : variables) {
      if (result.length() > 1) {
        result.append(", ");
      }
      result.append(variable.getName());
      result.append(" = ");
      if (copyToOriginal.get(variable) == null) {
        result.append("T");
      } else {
        result.append(copyToOriginal.get(variable).getName());
      }
    }
    result.append("}");
    
    return result.toString();  
  }

  private void addCopy(JVariable original, JVariable copy) {
    copyToOriginal.put(copy, original);
  }

  private void kill(JVariable variable) {
    copyToOriginal.put(variable, null);
    
    for (JVariable v : Lists.create(copyToOriginal.keySet())) {
      JVariable original = copyToOriginal.get(v);
      if (original == variable) {
        copyToOriginal.put(v, null);
      }
    }
  }
}
