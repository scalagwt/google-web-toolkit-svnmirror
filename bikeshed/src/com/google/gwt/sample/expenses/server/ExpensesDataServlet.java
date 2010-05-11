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
package com.google.gwt.sample.expenses.server;

import com.google.gwt.requestfactory.server.RequestFactoryServlet;
import com.google.gwt.sample.expenses.server.domain.Employee;
import com.google.gwt.sample.expenses.server.domain.Expense;
import com.google.gwt.sample.expenses.server.domain.Report;

import java.util.List;

/**
 * Dwindling interim servlet that calls our mock storage backend directly
 * instead of reflectively. Should soon vanish completely.
 */
@SuppressWarnings("serial")
public class ExpensesDataServlet extends RequestFactoryServlet {

  private static final boolean DISABLED = true;

  @Override
  protected void initDb() {
    if (DISABLED) {
      return;
    }

    long size = Employee.countEmployees();
    if (size > 1) {
      return;
    }
    List<Object> objectList = new DataGenerator().generateData(100);
    for (Object object : objectList) {
      if (object instanceof Employee) {
        ((Employee) object).persist();
      }
      if (object instanceof Report) {
        ((Report) object).persist();
      }
      if (object instanceof Expense) {
        ((Expense) object).persist();
      }
    }
  }
}
