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

package com.google.gwt.logging.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * A representation of a LogRecord which can be used by GWT RPC. In addition
 * to the fact that LogRecord is not serializable, it has different
 * implementations on the server and client side, so we cannot pass it over
 * the wire directly.
 */
public class SerializableLogRecord implements IsSerializable {
  private String level;
  private String loggerName = "";
  private String msg;
  private long timestamp;
  private SerializableThrowable thrown = null;

  protected SerializableLogRecord() {
    // for serialization
  }
  
  /**
   * Create a new SerializableLogRecord from a LogRecord.
   */
  public SerializableLogRecord(LogRecord lr) {
    level = lr.getLevel().toString();
    loggerName = lr.getLoggerName();
    msg = lr.getMessage();
    timestamp = lr.getMillis();
    if (lr.getThrown() != null) {
      thrown = new SerializableThrowable(lr.getThrown());
    }
  }

  /**
   * Create a new LogRecord from this SerializableLogRecord.
   */
  public LogRecord getLogRecord() {
    LogRecord lr = new LogRecord(Level.parse(level), msg);
    lr.setLoggerName(loggerName);
    lr.setMillis(timestamp);
    if (thrown != null) {
      lr.setThrown(thrown.getThrowable());
    }
    return lr;
  }
}
