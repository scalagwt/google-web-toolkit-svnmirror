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

import com.google.gwt.sample.expenses.gwt.client.DataGenerationService;
import com.google.gwt.sample.expenses.server.domain.Employee;
import com.google.gwt.sample.expenses.server.domain.Expense;
import com.google.gwt.sample.expenses.server.domain.Report;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Server-side implementation.
 */
public class DataGenerationServiceImpl extends RemoteServiceServlet implements DataGenerationService {
  
  private static final double AIRFARE = 600;
  
  private static final double BREAKFAST = 15;

  // Must be in sync with DESCRIPTIONS
  private static final String[] CATEGORIES = {
      "Local Transportation", "Local Transportation", "Local Transportation",
      "Office Supplies", "Office Supplies", "Office Supplies", "Office Supplies",
      "Office Supplies", "Office Supplies", "Office Supplies", "Office Supplies",
      "Dues and Fees", "Dues and Fees", "Dues and Fees",
      "Books", "Books", "Books", "Books"
  };

  private static final String[] CITIES = {
      "New York, New York",
      "Los Angeles, California",
      "Chicago, Illinois",
      "Houston, Texas",
      "Phoenix, Arizona",
      "Philadelphia, Pennsylvania",
      "San Antonio, Texas",
      "San Diego, California",
      "Dallas, Texas",
      "Detroit, Michigan",
      "San Jose, California",
      "Indianapolis, Indiana",
      "Jacksonville, Florida",
      "San Francisco, California",
      "Hempstead, New York",
      "Columbus, Ohio",
      "Austin, Texas",
      "Memphis, Tennessee",
      "Baltimore, Maryland",
      "Charlotte, North Carolina",
      "Fort Worth, Texas",
      "Milwaukee, Wisconsin",
      "Boston, Massachusetts",
      "El Paso, Texas",
      "Washington, District of Columbia",
      "Nashville-Davidson, Tennessee",
      "Seattle, Washington",
      "Denver, Colorado",
      "Las Vegas, Nevada",
      "Portland, Oregon",
      "Oklahoma City, Oklahoma",
      "Tucson, Arizona",
      "Albuquerque, New Mexico",
      "Atlanta, Georgia",
      "Long Beach, California",
      "Brookhaven, New York",
      "Fresno, California",
      "New Orleans, Louisiana",
      "Sacramento, California",
      "Cleveland, Ohio",
      "Mesa, Arizona",
      "Kansas City, Missouri",
      "Virginia Beach, Virginia",
      "Omaha, Nebraska",
      "Oakland, California",
      "Miami, Florida",
      "Tulsa, Oklahoma",
      "Honolulu, Hawaii",
      "Minneapolis, Minnesota",
      "Colorado Springs, Colorado",
      "Arlington, Texas",
      "Wichita, Kansas",
      "St. Louis, Missouri",
      "Raleigh, North Carolina",
      "Santa Ana, California",
      "Anaheim, California",
      "Cincinnati, Ohio",
      "Tampa, Florida",
      "Islip, New York",
      "Pittsburgh, Pennsylvania",
      "Toledo, Ohio",
      "Aurora, Colorado",
      "Oyster Bay, New York",
      "Bakersfield, California",
      "Riverside, California",
      "Stockton, California",
      "Corpus Christi, Texas",
      "Buffalo, New York",
      "Newark, New Jersey",
      "St. Paul, Minnesota",
      "Anchorage, Alaska",
      "Lexington-Fayette, Kentucky",
      "Plano, Texas",
      "St. Petersburg, Florida",
      "Fort Wayne, Indiana",
      "Glendale, Arizona",
      "Lincoln, Nebraska",
      "Jersey City, New Jersey",
      "Greensboro, North Carolina",
      "Norfolk, Virginia",
      "Chandler, Arizona",
      "Henderson, Nevada",
      "Birmingham, Alabama",
      "Scottsdale, Arizona",
      "Madison, Wisconsin",
      "Baton Rouge, Louisiana",
      "North Hempstead, New York",
      "Hialeah, Florida",
      "Chesapeake, Virginia",
      "Garland, Texas",
      "Orlando, Florida",
      "Babylon, New York",
      "Lubbock, Texas",
      "Chula Vista, California",
      "Akron, Ohio",
      "Rochester, New York",
      "Winston-Salem, North Carolina",
      "Durham, North Carolina",
      "Reno, Nevada",
      "Laredo, Texas"
  };
  
  private static final String[] DEPARTMENTS = { "Operations", "Engineering", "Finance", "Marketing", "Sales" };
  
  // Must be in sync with CATEGORIES
  private static final String[] DESCRIPTIONS = {
      "Train fare", "Taxi fare", "Bus ticket",
      "Paperclips", "Stapler", "Scissors", "Paste",
      "Notebooks", "Pencils", "Whiteboard Markers", "Tissues",
      "ACM Membership", "IEEE Membership", "Google I/O Ticket",
      "Book on AJAX", "Book on Python", "Book on JavaScript", "Book on C++"
  };
  
  private static final double DINNER = 60;
  
  // 11% of females hyphenate their last names
  private static final double FEMALE_HYPHENATE = 0.11;
  
  private static List<Double> femaleNameFreqs = new ArrayList<Double>();
  
  private static List<String> femaleNames = new ArrayList<String>();
  
  private static final double HOTEL = 300;

  private static boolean initialized = false;

  private static final DataGenerationServiceImpl instance = new DataGenerationServiceImpl();

  private static List<Double> lastNameFreqs = new ArrayList<Double>();

  private static List<String> lastNames = new ArrayList<String>();

  private static final double LUNCH = 25;

  // 2% of males hyphenate their last names
  private static final double MALE_HYPHENATE = 0.02;

  private static List<Double> maleNameFreqs = new ArrayList<Double>();

  private static List<String> maleNames = new ArrayList<String>();
  
  // 10% of employees are managers
  private static final double MANAGER = 0.10;
  
  // managers can have up to 20 direct reports
  private static final int MAX_DIRECTS = 20;
  
  private static final long MILLIS_PER_DAY = 24L * 60L * 60L * 1000L;
  
  private static final String[] NOTES = {
      // Some entries do not have notes.
      "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
      "Need approval by Monday", "Show me the money",
      "Please bill to the Widgets project", "High priority", "Review A.S.A.P."};
  
  private static final String[] PURPOSES = {
      "Spending lots of money", "Team building diamond cutting offsite",
      "Visit to Istanbul", "ISDN modem for telecommuting", "Sushi offsite",
      "Baseball card research", "Potato chip cooking offsite",
      "Money laundering", "Donut day"};

  private static final double SUNDRY = 100;

  private static final long MILLIS_PER_HOUR = 60L * 60L * 1000L;
  
  public static DataGenerationServiceImpl getInstance() {
    return instance;
  }
  
  private long idEmp;
  private long idExp;
  private long idRep;
  private Random rand = new Random();
  
  public synchronized int generate(int millis) {
    try {
      if (!initialized) {
        readFile("dist.all.last.txt", lastNames, lastNameFreqs);
        readFile("dist.female.first.txt", femaleNames, femaleNameFreqs);
        readFile("dist.male.first.txt", maleNames, maleNameFreqs);
        initialized = true;
      }

      idEmp = Employee.countEmployees() + 1;
      idRep = 100000000 + Report.countReports();
      idExp = 200000000 + Expense.countExpenses();

      long now = System.currentTimeMillis();
      while (System.currentTimeMillis() - now < millis) {
        int department = rand.nextInt(DEPARTMENTS.length);
        int supervisorId = idEmp == 1 ? 1 : rand.nextInt((int) idEmp - 1) + 1;
        makeEmployee(department, supervisorId, 0);
      }

      return (int) Report.countReports();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  public synchronized int getNumReports() {
    return (int) Report.countReports();
  }

  private double amount(double max) {
    return (1.0 + rand.nextDouble()) * max * 0.5;
  }

  private String chooseName(List<String> names, List<Double> freqs) {
    double lastFreq = freqs.get(freqs.size() - 1);
    double freq = rand.nextDouble() * lastFreq;
    
    int index = Collections.binarySearch(freqs, freq);
    if (index < 0) {
      index = -index - 1;
    }
    String name = names.get(index);
    return name;
  }
  
  private long makeEmployee(int department, long supervisorId, int level) {
    long id = idEmp++;
    String firstName;
    String lastName = chooseName(lastNames, lastNameFreqs);
    if (rand.nextInt(2) == 0) {
      firstName = chooseName(femaleNames, femaleNameFreqs);
      if (rand.nextDouble() < FEMALE_HYPHENATE) {
        lastName += "-" + chooseName(lastNames, lastNameFreqs);
      }
    } else {
      firstName = chooseName(maleNames, maleNameFreqs);
      if (rand.nextDouble() < MALE_HYPHENATE) {
        lastName += "-" + chooseName(lastNames, lastNameFreqs);
      }
    }
    
    Employee employee = new Employee();
    employee.setId(id);
    employee.setSupervisorKey(supervisorId);
    employee.setUserName(userName(firstName, lastName));
    employee.setDisplayName(firstName + " " + lastName);
    employee.setDepartment(DEPARTMENTS[department]);
    employee.setPassword("");
    employee.persist();
    
    int numExpenseReports = rand.nextInt(96) + 5;
    for (int i = 0; i < numExpenseReports; i++) {
      makeExpenseReport(id, supervisorId);
    }
    
    int numEmployeeReports = rand.nextInt(MAX_DIRECTS);
    if (rand.nextDouble() < MANAGER) {
      for (int i = 0; i < numEmployeeReports; i++) {
        makeEmployee(department, id, level + 1);
      }
    }
    return id;
  }
  
  private void makeExpenseDetail(long reportId, Date date, String category,
      String description, double amount) {
    long id = idExp++;
    
    Expense expense = new Expense();
    expense.setId(id);
    expense.setReportId(reportId);
    expense.setDescription(description);
    expense.setDate(date);
    expense.setAmount(amount);
    expense.setCategory(category);
    expense.setApproval("");
    expense.setReasonDenied("");
    expense.persist();
  }

  private void makeExpenseReport(long employeeId, long supervisorId) {
    long offset = rand.nextInt(60 * 60 * 24 * 90) * 1000L;
    long millis = new Date().getTime() - offset;
    
    long id = idRep++;
    Date createdDate = new Date(millis);

    Report report = new Report();
    report.setId(id);
    report.setReporterKey(employeeId);
    report.setApprovedSupervisorKey(supervisorId);
    report.setCreated(createdDate);
    report.setReporterKey(employeeId);
    boolean travel = rand.nextInt(4) == 0;
    int days = 1;
    if (travel) {
      days = rand.nextInt(10) + 1;
      int index1 = rand.nextInt(CITIES.length);
      int index2 = index1;
      while (index2 == index1) {
        index2 = rand.nextInt(CITIES.length);
      }
      
      report.setPurpose("Travel from " + CITIES[index1] + " to " + CITIES[index2]);
      report.setNotes("Travel for " + days + " days");
      
      makeExpenseDetail(id, new Date(millis - days * MILLIS_PER_DAY),
          "Air Travel", "Outbound flight", amount(AIRFARE));
      makeExpenseDetail(id, new Date(millis - MILLIS_PER_DAY / 2),
          "Air Travel", "Return flight", amount(AIRFARE));
      for (int i = 0; i < days; i++) {
        makeExpenseDetail(id, new Date(millis - (days - i) * MILLIS_PER_DAY - 10 * MILLIS_PER_HOUR),
            "Dining", "Breakfast", amount(BREAKFAST));
        makeExpenseDetail(id, new Date(millis - (days - i) * MILLIS_PER_DAY - 6 * MILLIS_PER_HOUR),
            "Dining", "Lunch", amount(LUNCH));
        makeExpenseDetail(id, new Date(millis - (days - i) * MILLIS_PER_DAY - 2 * MILLIS_PER_HOUR),
            "Dining", "Dinner", amount(DINNER));
        makeExpenseDetail(id, new Date(millis - (days - i) * MILLIS_PER_DAY),
            "Lodging", "Hotel", amount(HOTEL));
      }
    } else {
      report.setPurpose(PURPOSES[rand.nextInt(PURPOSES.length)]);
      report.setNotes(NOTES[rand.nextInt(NOTES.length)]);
      
      int numExpenses = rand.nextInt(8) + 3;
      for (int i = 0; i < numExpenses; i++) {
        int index = rand.nextInt(CATEGORIES.length);
        long detailOffset = rand.nextInt(60 * 60 * 24 * days) * 1000L;
        Date date = new Date(createdDate.getTime() - detailOffset);
        makeExpenseDetail(id, date, CATEGORIES[index], DESCRIPTIONS[index],
            amount(SUNDRY));
      }
    }
    
    report.persist();
  }

  private void readFile(String filename, List<String> names,
      List<Double> frequencies) throws IOException {
    Reader reader = new FileReader(filename);
    BufferedReader br = new BufferedReader(reader);
    
    names.clear();
    frequencies.clear();
    
    String s;
    while ((s = br.readLine()) != null) {
      String[] split = s.split("\\s+");
      String name = split[0];
      if (name.startsWith("MC") && name.length() > 2) {
        name = "Mc" + name.charAt(2) + name.substring(3).toLowerCase();
      } else {
        name = "" + name.charAt(0) + name.substring(1).toLowerCase();
      }
      names.add(name);
      frequencies.add(Double.parseDouble(split[2]));
    }
    
    // Disambiguate names with equal cumulative frequencies
    double lastFreq = 0;
    int count = 1;
    int len = frequencies.size();
    for (int i = 0; i < len; i++) {
      Double freq = frequencies.get(i);
      if (freq == lastFreq) {
        count++;
        continue;
      } else {
        if (count > 1) {
          for (int c = 0; c < count; c++) {
            double newFreq = lastFreq + (.001 * c) / count;
            frequencies.set(i - count + c, newFreq);
          }
          count = 1;
        }

        lastFreq = freq;
      }
    }
  }
  
  private String userName(String firstName, String lastName) {
    return ("" + firstName.charAt(0) + lastName + rand.nextInt(100)).toLowerCase();
  }
}
