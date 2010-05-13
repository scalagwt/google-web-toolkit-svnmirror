package com.google.gwt.sample.expenses.gwt.client.place;

import com.google.gwt.sample.expenses.gwt.request.EmployeeRecord;
import com.google.gwt.sample.expenses.gwt.request.ReportRecord;
import com.google.gwt.valuestore.shared.Record;

/**
 * Filters an {@link ScaffoldPlace} to the corresponding record
 * type.
 */
public final class ScaffoldPlaceToRecordType implements
    ScaffoldPlaceFilter<Class<? extends Record>> {
  public Class<? extends Record> filter(EmployeeScaffoldPlace place) {
    return EmployeeRecord.class;
  }

  public Class<? extends Record> filter(ListScaffoldPlace place) {
    return place.getType(); 
  }

  public Class<? extends Record> filter(ReportScaffoldPlace place) {
    return ReportRecord.class;
  }
}