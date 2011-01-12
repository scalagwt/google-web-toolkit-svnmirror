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
package com.google.gwt.sample.dynatablerf.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Hold the relevant data for a Schedule.
 */
public class Schedule {

  private List<TimeSlot> timeSlots = new ArrayList<TimeSlot>();

  public Schedule() {
  }

  public void addTimeSlot(TimeSlot timeSlot) {
    timeSlots.add(timeSlot);
  }

  public String getDescription(List<Boolean> daysFilter) {
    String s = null;
    for (TimeSlot timeSlot : timeSlots) {
      if (daysFilter.get(timeSlot.getDayOfWeek())) {
        if (s == null) {
          s = timeSlot.getDescription();
        } else {
          s += ", " + timeSlot.getDescription();
        }
      }
    }

    if (s != null) {
      return s;
    } else {
      return "";
    }
  }

  @Override
  public String toString() {
    return getDescription(null);
  }

}
