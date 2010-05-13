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
package com.google.gwt.sample.expenses.gwt.server;

import com.google.apphosting.api.DeadlineExceededException;
import com.google.gwt.sample.expenses.gwt.client.DataGenerationService;
import com.google.gwt.sample.expenses.server.domain.Employee;
import com.google.gwt.sample.expenses.server.domain.Expense;
import com.google.gwt.sample.expenses.server.domain.Report;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Server-side implementation.
 */
public class DataGenerationServiceImpl extends RemoteServiceServlet implements
    DataGenerationService {

  private static final Logger log = Logger.getLogger(DataGenerationServiceImpl.class.getName());

  private long endTime;

  private ReportGenerator reportGenerator;
  
  public void delete() {
    try {
      log.info("Expenses before: " + Expense.countExpenses());
      List<Expense> expenses = Expense.findAllExpenses();
      log.info("ids from " + expenses.get(0).getId() + " to " +
          expenses.get(expenses.size() - 1).getId());
      for (Expense e : expenses) {
        log.info("Deleting expense " + e.getId());
        e.remove();
      }
      for (Report r : Report.findAllReports()) {
        log.info("Deleting report " + r.getId());
        r.remove();
      }
      for (Employee e : Employee.findAllEmployees()) {
        log.info("Deleting employee " + e.getId());
        e.remove();
      }
    } catch (DeadlineExceededException e) {
      log.info("Expenses after: " + Expense.countExpenses());
      return;
    }
  }

  public void generate(int millis) {
    long startTime = System.currentTimeMillis();
    endTime = startTime + millis;
    int startEmployees = (int) Employee.countEmployees();
    int startReports = (int) Report.countReports();
    int numEmployees;
    
    synchronized (DataGenerationServiceImpl.class) {
      if (reportGenerator == null) {
        reportGenerator = new ReportGenerator() {
          @Override
          public boolean shouldContinue() {
            return System.currentTimeMillis() < endTime;
          }
          
          @Override
          public long storeEmployee(EmployeeDTO employee) {
            Employee e = new Employee();
            e.setDepartment(employee.department);
            e.setDisplayName(employee.displayName);
            e.setPassword(employee.password);
            e.setSupervisorKey(employee.supervisorKey);
            e.setUserName(employee.userName);
            e.persist();
            return e.getId();
          }
          
          @Override
          public long storeExpense(ExpenseDTO expense) {
            Expense e = new Expense();
            e.setReportId(expense.reportId);
            e.setDescription(expense.description);
            e.setCreated(expense.created);
            e.setAmount(expense.amount);
            e.setCategory(expense.category);
            e.setApproval(expense.approval);
            e.setReasonDenied(expense.reasonDenied);
            e.persist();
            return e.getId();
          }

          @Override
          public long storeReport(ReportDTO report) {
            Report r = new Report();
            r.setApprovedSupervisorKey(report.approvedSupervisorKey);
            r.setCreated(report.created);
            r.setDepartment(report.department);
            r.setNotes(report.notes);
            r.setPurpose(report.purpose);
            r.setReporterKey(report.reporterKey);
            r.persist();
            return r.getId();
          }
        };
      }

      try {
        reportGenerator.init("dist.all.last.txt", "dist.female.first.txt", "dist.male.first.txt");
      } catch (IOException e) {
        throw new RuntimeException(e.getMessage());
      }
    }
    
    // Choose department and make a manager
    int department = reportGenerator.getDepartment();
    long supervisorId = 1;
    
    while (System.currentTimeMillis() < endTime) {
      reportGenerator.makeEmployee(department, supervisorId, true, 0, false);
    }
    
    numEmployees = (int) Employee.countEmployees();
    int numReports = (int) Report.countReports();
    int reportsCreated = numReports - startReports;
    int employeesCreated = numEmployees - startEmployees;
    log.info("Generated " + employeesCreated + " employees and " +
        reportsCreated + " reports in " +
        (System.currentTimeMillis() - startTime) + " milliseconds");
  }

  public List<Integer> getCounts() {
    synchronized (DataGenerationServiceImpl.class) {
      List<Integer> counts = new ArrayList<Integer>(3);
      counts.add((int) Employee.countEmployees());
      counts.add((int) Report.countReports());
      counts.add((int) Expense.countExpenses());
      return counts;
    }
  }
}
