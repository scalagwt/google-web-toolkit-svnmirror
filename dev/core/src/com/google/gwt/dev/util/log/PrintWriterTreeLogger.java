/*
 * Copyright 2006 Google Inc.
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
package com.google.gwt.dev.util.log;

import java.io.PrintWriter;

/**
 * Tree logger that logs to a print writer.
 */
public final class PrintWriterTreeLogger extends AbstractTreeLogger {

  private final PrintWriter out;

  private final String indent;

  public PrintWriterTreeLogger() {
    this(new PrintWriter(System.out, true));
  }

  public PrintWriterTreeLogger(PrintWriter out) {
    this(out, "");
  }

  protected PrintWriterTreeLogger(PrintWriter out, String indent) {
    this.out = out;
    this.indent = indent;
  }

  protected AbstractTreeLogger doBranch() {
    return new PrintWriterTreeLogger(out, indent + "   ");
  }

  protected void doCommitBranch(AbstractTreeLogger childBeingCommitted,
      Type type, String msg, Throwable caught) {
    doLog(childBeingCommitted.getBranchedIndex(), type, msg, caught);
  }

  protected void doLog(int indexOfLogEntryWithinParentLogger, Type type,
      String msg, Throwable caught) {
    out.print(indent);
    if (type.needsAttention()) {
      out.print("[");
      out.print(type.getLabel());
      out.print("] ");
    }

    out.println(msg);
    if (caught != null) {
      caught.printStackTrace(out);
    }
  }
}
