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

import java.util.Date;
import java.util.Random;

/**
 * Dwindling interim servlet that calls our mock storage backend directly
 * instead of reflectively. Should soon vanish completely.
 */
@SuppressWarnings("serial")
public class ExpensesDataServlet extends RequestFactoryServlet {

  // Must be in sync with DESCRIPTIONS
  private static final String[] CATEGORIES = {
    "Dining", "Dining", "Dining",
    "Lodging", "Lodging",
    "Local Transportation", "Local Transportation", "Local Transportation",
    "Air Travel", "Air Travel",
    "Office Supplies", "Office Supplies", "Office Supplies", "Office Supplies",
  };

  // Must be in sync with CATEGORIES
  private static final String[] DESCRIPTIONS = {
    "Breakfast", "Lunch", "Dinner",
    "Hotel", "Bed & Breakfast",
    "Train fare", "Taxi fare", "Bus ticket",
    "Flight from ATL to SFO", "Flight from SFO to ATL",
    "Paperclips", "Stapler", "Scissors", "Paste",
  };
  
  private static final String[] NOTES = {
    "Need approval by Monday", "Show me the money",
    "Please bill to the Widgets project",
    "High priority",
    "Review A.S.A.P."
  };

  Random rand = new Random();
  
  @Override
  protected void initDb() {
    long size = Employee.countEmployees();
    if (size > 1) {
      return;
    }
    // initialize
    Employee abc = new Employee();
    abc.setUserName("abc");
    abc.setDisplayName("Able B. Charlie");
    abc.persist();

    Employee def = new Employee();
    def.setUserName("def");
    def.setDisplayName("Delta E. Foxtrot");
    def.setSupervisorKey(abc.getId());
    def.persist();

    Employee ghi = new Employee();
    ghi.setUserName("ghi");
    ghi.setDisplayName("George H. Indigo");
    ghi.setSupervisorKey(abc.getId());
    ghi.persist();

    for (String purpose : new String[] {
        "Spending lots of money", "Team building diamond cutting offsite",
        "Visit to Istanbul"}) {
      Report report = new Report();
      report.setReporterKey(abc.getId());
      report.setCreated(getDate());
      report.setPurpose(purpose);
      report.setNotes(getNote());
      report.persist();
      
      addExpenses(report.getId());
    }

    for (String purpose : new String[] {"Money laundering", "Donut day"}) {
      Report report = new Report();
      report.setCreated(getDate());
      report.setReporterKey(def.getId());
      report.setPurpose(purpose);
      report.setNotes(getNote());
      report.persist();
      
      addExpenses(report.getId());
    }

    for (String purpose : new String[] {
        "ISDN modem for telecommuting", "Sushi offsite",
        "Baseball card research", "Potato chip cooking offsite"}) {
      Report report = new Report();
      report.setCreated(getDate());
      report.setReporterKey(ghi.getId());
      report.setPurpose(purpose);
      report.setNotes(getNote());
      report.persist();
      
      addExpenses(report.getId());
    }

    for (int i = 0; i < 1000; i++) {
      Report report = new Report();
      report.setCreated(getDate());
      report.setReporterKey(ghi.getId());
      report.setPurpose("Report " + i);
      report.setNotes(getNote());
      report.persist();

      addExpenses(report.getId());
    }
  }
  
  private void addExpenses(Long reportId) {
    int num = rand.nextInt(5) + 1;
    for (int i = 0; i < num; i++) {
      String[] descCat = getDescriptionAndCategory();

      Expense detail = new Expense();
      detail.setReportId(reportId);
      detail.setDescription(descCat[0]);
      detail.setDate(getDate());
      detail.setAmount(rand.nextInt(25000) / 100.0);
      detail.setCategory(descCat[1]);
      detail.setApproval("");
      detail.setReasonDenied("");
      detail.persist();
    }
  }
  
  private Date getDate() {
    long now = new Date().getTime();
    // Go back up to 90 days from the current date
    long dateOffset = rand.nextInt(60 * 60 * 24 * 90) * 1000L;
    return new Date(now - dateOffset);
  }

  private String[] getDescriptionAndCategory() {
    String[] dc = new String[2];
    int index = rand.nextInt(DESCRIPTIONS.length);
    dc[0] = DESCRIPTIONS[index];
    dc[1] = CATEGORIES[index];
    return dc;
  }
  
  private String getNote() {
    return NOTES[rand.nextInt(NOTES.length)];
  }
}
