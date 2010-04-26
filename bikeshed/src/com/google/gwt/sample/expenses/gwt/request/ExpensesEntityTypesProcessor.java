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
package com.google.gwt.sample.expenses.gwt.request;

import com.google.gwt.valuestore.shared.Record;

import java.util.HashSet;
import java.util.Set;

/**
 * "API generated" class gives implementors of {@link EntityTypesProcessor}
 * access to all types of {@link Record} without hardcoding them.
 * <p>
 * IRL this class will be generated by a JPA-savvy tool run before compilation.
 */
public class ExpensesEntityTypesProcessor {

  /**
   * Implemented by objects that need to process {@link Record} types.
   */
  public interface EntityTypesProcessor {
    void processType(Class<? extends Record> recordType);
  }

  private static Set<Class<? extends Record>> set;

  public static void processAll(EntityTypesProcessor processor) {
    for (Class<? extends Record> type : get()) {
      processor.processType(type);
    }
  }

  private static Set<Class<? extends Record>> get() {
    if (set == null) {
      HashSet<Class<? extends Record>> newInstance = new HashSet<Class<? extends Record>>();
      newInstance.add(ReportRecord.class);
      newInstance.add(EmployeeRecord.class);
      set = newInstance;
    }
    return set;
  }
}
