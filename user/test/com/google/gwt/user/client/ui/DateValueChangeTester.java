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
package com.google.gwt.user.client.ui;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

import junit.framework.TestCase;

import java.util.Date;

/**
 * Handy tool for testing classes that implement {@link HasValue<Date>}.
 */
public class DateValueChangeTester {
  static class Handler implements ValueChangeHandler<Date> {
    Date received = null;
    
    public void onValueChange(ValueChangeEvent<Date> event) {
      received = event.getValue();
    }
  }

  private final HasValue<Date> subject;
  public DateValueChangeTester(HasValue<Date> subject) {
    this.subject = subject;
  }
  
  @SuppressWarnings("deprecation")
  public void run() {
    subject.setValue(null);
    TestCase.assertNull(subject.getValue());
    
    Date able = new Date(1999, 5, 15);
    subject.setValue(able);
    TestCase.assertEquals(able, subject.getValue());
    
    DateValueChangeTester.Handler h = new Handler();
    subject.addValueChangeHandler(h);
    
    subject.setValue(able);
    TestCase.assertNull(h.received);
    
    Date baker = new Date(1965, 12, 7);
    subject.setValue(baker);
    TestCase.assertNull(h.received);

    subject.setValue(baker, true);
    TestCase.assertNull(h.received);
    
    subject.setValue(able, true);
    TestCase.assertEquals(able, h.received);

    subject.setValue(baker, true);
    TestCase.assertEquals(baker, h.received);
  }
}