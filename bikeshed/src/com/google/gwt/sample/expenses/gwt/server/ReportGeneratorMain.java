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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

/**
 * Command-line entry point for report generation.
 */
public class ReportGeneratorMain {

  protected static final int MAX_REPORTS = 100000;
  protected static final int VERSION = 1;
  
  /**
   * @param args
   */
  public static void main(String[] args) throws IOException {
    String dir = "io-expenses-" + MAX_REPORTS + dateToString(new Date());
    new File("/home/rice/www/" + dir).mkdir();
    final PrintWriter empWriter = new PrintWriter("/home/rice/www/" + dir + "/employees.csv");
    final PrintWriter repWriter = new PrintWriter("/home/rice/www/" + dir + "/reports.csv");
    final PrintWriter expWriter = new PrintWriter("/home/rice/www/" + dir + "/expenses.csv");
    
    ReportGenerator reportGenerator = new ReportGenerator() {
      long empids = 1L;
      long expids = 1000000000L;
      long repids = 2000000000L;
      
      @Override
      public boolean shouldContinue() {
        return getNumReports() < MAX_REPORTS;
      }
      
      @Override
      public long storeEmployee(EmployeeDTO employee) {
        long id = empids++;
        // userName,displayName,supervisorKey,VERSION,key,department,password
        empWriter.println(employee.userName + "," + employee.displayName + ","
            + employee.supervisorKey + "," + VERSION + "," + id + ","
            + employee.department + ",");
        return id;
      }
      
      @Override
      public long storeExpense(ExpenseDTO expense) {
        long id = expids++;
        // category,description,reasonDenied,amount,VERSION,reportId,key,created,approval"
        expWriter.println(expense.category + "," + expense.description + ",," + expense.amount + ","
            + VERSION + "," + expense.reportId + "," + id + "," + dateToString(expense.created)
            + ",");
        return id;
      }

      @Override
      public long storeReport(ReportDTO report) {
        long id = repids++;
        // created,notes,VERSION,approvedSupervisorKey,key,reporterKey,purposeLowerCase,purpose,department
        repWriter.println(dateToString(report.created) + ",\"" + report.notes + "\"," + VERSION + ","
            + report.approvedSupervisorKey + "," + id + "," + report.reporterKey + ",\""
            + report.purpose.toLowerCase() + "\",\"" + report.purpose + "\"," + report.department);
        return id;
      }
    };

    empWriter.println("userName,displayName,supervisorKey,VERSION,key,department,password");
    repWriter.println("created,notes,VERSION,approvedSupervisorKey,key,reporterKey,purposeLowerCase,purpose,department");
    expWriter.println("category,description,reasonDenied,amount,VERSION,reportId,key,created,approval");

    reportGenerator.init("/home/rice/www/dist.all.last.txt",
        "/home/rice/www/dist.female.first.txt",
        "/home/rice/www/dist.male.first.txt");

    reportGenerator.makeEmployee(0, 0, false, 0, false);
    while (reportGenerator.shouldContinue()) {
      reportGenerator.makeEmployee(reportGenerator.getDepartment(), 1, true, 1, true);
    }

    empWriter.close();
    repWriter.close();
    expWriter.close();
  }

  @SuppressWarnings("deprecation")
  private static String dateToString(Date date) {
    return (date.getYear() + 1900) + twoDigit(date.getMonth() + 1)
        + twoDigit(date.getDate()) + "T" + twoDigit(date.getHours()) + ":"
        + twoDigit(date.getMinutes());
  }
  
  private static String twoDigit(int i) {
    return i < 10 ? "0" + i : "" + i;
  }
}
