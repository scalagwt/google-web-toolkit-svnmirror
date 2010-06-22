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
package com.google.gwt.valuestore.client;

import com.google.gwt.valuestore.shared.Record;
import com.google.gwt.valuestore.shared.SyncResult;

import java.util.Map;

/**
 * Concrete implementation of SyncResult.
 */
public class SyncResultImpl implements SyncResult {

  private final Record record;
  private final Map<String, String> violations;
  private final String futureId;
  
  public SyncResultImpl(Record record, Map<String, String> violations, String futureId) {
    this.record = record;
    this.violations = violations;
    this.futureId = futureId;
  }

  public String getFutureId() {
    return futureId;
  }

  public Record getRecord() {
    return record;
  }
  
  public Map<String, String> getViolations() {
    return violations;
  }

  public boolean hasViolations() {
    return violations != null && violations.size() > 0;
  }
  
}
