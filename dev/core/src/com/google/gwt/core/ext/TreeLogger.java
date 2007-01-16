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
package com.google.gwt.core.ext;

import java.util.HashMap;
import java.util.Map;

/**
 * An interface used to log messages in deferred binding generators.
 */
public interface TreeLogger {

  /**
   * A type-safe enum of all possible logging severity types.
   */
  class Type {

    /**
     * MUST lazy-init (@link #instances}. Some compilers (javac) will cause
     * Type's clinit to call TreeLogger's clinit <i>first</i>. This means
     * ERROR, WARN, etc run their constructors before instances can
     * self-initialize.
     */
    private static Map instances;
    private static final Type[] NO_TYPES = new Type[0];

    static {
      // ensure the standard types are actually registered
      Type type = ERROR;
    }

    /**
     * Gets all the possible severity types as an array.
     * 
     * @return an array of severity types
     */
    public static Type[] instances() {
      return (Type[]) instances.values().toArray(NO_TYPES);
    }

    /**
     * Looks up a severity type by label.
     * 
     * @param label the label of the desired severity
     * @return the severity type labelled <code>label</code>, or
     *         <code>null</code> if no such type exists
     */
    public static Type valueOf(String label) {
      return (Type) instances.get(label.toUpperCase());
    }

    private final String label;

    private final Type parent;

    private final boolean needsAttention;

    /**
     * Constructs a log type with an optional parent.
     */
    private Type(boolean needsAttention, String name, Type parent) {
      if (instances == null) {
        instances = new HashMap();
      }
      Object existing = instances.put(name.toUpperCase(), this);
      assert (existing == null);
      this.needsAttention = needsAttention;
      this.label = name;
      this.parent = parent;
    }

    /**
     * Gets the label for this severity type.
     * 
     * @return the label
     */
    public String getLabel() {
      return label;
    }

    /**
     * Gets the parent of this severity type.
     * 
     * @return the parent
     */
    public Type getParent() {
      return parent;
    }
    /**
     * Indicates whether this severity type represents a high severity that
     * should be highlighted for the user.
     * 
     * @return <code>true</code> if this severity is high, otherwise
     *         <code>false</code>.
     */
    public boolean needsAttention() {
      return needsAttention;
    }
    public String toString() {
      return label;
    }
  }

  /**
   * Logs an error.
   */
  Type ERROR = new Type(true, "ERROR", null);

  /**
   * Logs a warning.
   */
  Type WARN = new Type(true, "WARN", ERROR);

  /**
   * Logs information.
   */
  Type INFO = new Type(false, "INFO", WARN);

  /**
   * Logs information related to lower-level operation.
   */
  Type TRACE = new Type(false, "TRACE", INFO);

  /**
   * Logs detailed information that could be useful during debugging.
   */
  Type DEBUG = new Type(false, "DEBUG", TRACE);

  /**
   * Logs extremely verbose and detailed information that is typically useful
   * only to product implementors.
   */
  Type SPAM = new Type(false, "SPAM", DEBUG);

  /**
   * Logs everything -- quite a bit of stuff.
   */
  Type ALL = new Type(false, "ALL", SPAM);

  /**
   * A valid logger that ignores all messages. Occasionally useful when calling
   * methods that require a logger parameter.
   */
  TreeLogger NULL = new TreeLogger() {
    public TreeLogger branch(Type type, String msg, Throwable caught) {
      return this;
    }

    public boolean isLoggable(Type type) {
      return false;
    }

    public void log(Type type, String msg, Throwable caught) {
      // nothing
    }
  };

  /**
   * Produces a branched logger, which can be used to write messages that are
   * logically grouped together underneath the current logger. The details of
   * how/if the resulting messages are displayed is implementation-dependent.
   * 
   * <p>
   * The log message supplied when branching serves two purposes. First, the
   * message should be considered a heading for all the child messages below it.
   * Second, the <code>type</code> of the message provides a hint as to the
   * importance of the children below it. As an optimization, an implementation
   * could return a "no-op" logger if messages of the specified type weren't
   * being logged, which the implication being that all nested log messages were
   * no more important than the level of their branch parent.
   * </p>
   * 
   * <p>
   * As an example of how hierarchical logging can be used, a branched logger in
   * a GUI could write log message as child items of a parent node in a tree
   * control. If logging to streams, such as a text console, the branched logger
   * could prefix each entry with a unique string and indent its text so that it
   * could be sorted later to reconstruct a proper hierarchy.
   * </p>
   * 
   * @param type
   * @param msg An optional message to log, which can be <code>null</code> if
   *          only an exception is being logged
   * @param caught An optional exception to log, which can be <code>null</code>
   *          if only a message is being logged
   * @return an instance of {@link TreeLogger} representing the new branch of
   *         the log. May be the same instance on which this method is called
   */
  TreeLogger branch(TreeLogger.Type type, String msg, Throwable caught);

  /**
   * Determines whether or not a log entry of the specified type would actually
   * be logged. Caller use this method to avoid constructing log messages that
   * would be thrown away.
   */
  boolean isLoggable(TreeLogger.Type type);

  /**
   * Logs a message and/or an exception. It is also legal to call this method
   * using <code>null</code> arguments for <i>both</i> <code>msg</code> and
   * <code>caught</code>, in which case the log event can be ignored.
   * 
   * @param type
   * @param msg An optional message to log, which can be <code>null</code> if
   *          only an exception is being logged
   * @param caught An optional exception to log, which can be <code>null</code>
   *          if only a message is being logged
   */
  void log(TreeLogger.Type type, String msg, Throwable caught);
}
