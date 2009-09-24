/*
 * Copyright 2009 Google Inc.
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
package com.google.gwt.simplecrawler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Command-line settings for simple crawler.
 */
public class Settings {
  /**
   * An exception indicating that there is a problem in an argument list.
   */
  public static class ArgumentListException extends Exception {
    public ArgumentListException(String message) {
      super(message);
    }
  }

  /**
   * One individual setting.
   */
  public abstract static class Setting<T> {
    private final String help;
    private T value;

    public Setting(T initialValue, String help) {
      value = initialValue;
      this.help = help;
    }

    public T get() {
      return value;
    }

    public String getHelp() {
      return help;
    }

    public void set(T newValue) {
      value = newValue;
    }

    /**
     * Consume arguments from the front of the list. If the front of the
     * argument list is not a match, do nothing. If the front of the argument
     * list is a match but has some problem, then throw an exception.
     */
    abstract boolean consumeArguments(List<String> arguments)
        throws ArgumentListException;
  }

  /**
   * A setting that is an option followed by a string argument.
   */
  public static class StringSetting extends Setting<String> {
    private final String option;

    public StringSetting(String option, String argumentName,
        String defaultSetting, String description) {
      super(defaultSetting, option + " " + argumentName + "    " + description);
      this.option = option;
    }

    @Override
    public String toString() {
      return option + " " + get();
    }

    @Override
    boolean consumeArguments(List<String> arguments)
        throws ArgumentListException {
      if (arguments.get(0).equals(option)) {
        if (arguments.size() < 2) {
          throw new ArgumentListException("Option " + option + " requires an argument");
        }
        arguments.remove(0);
        set(arguments.remove(0));
        return true;
      }
      return false;
    }
  }

  /**
   * Processes the arguments from the command line.
   * 
   * @param allArguments
   * @return processed settings
   * @throws ArgumentListException
   */
  public static Settings fromArgumentList(String[] allArguments)
      throws ArgumentListException {

    Settings settings = new Settings();

    List<String> remainingArguments = new LinkedList<String>(
        Arrays.asList(allArguments));

    // Handle hyphenated options
    next_argument : while (!remainingArguments.isEmpty()) {
      for (Setting<?> setting : settings.allSettings) {
        if (setting.consumeArguments(remainingArguments)) {
          continue next_argument;
        }
      }
      System.err.println("Unknown argument: " + remainingArguments.get(0));
      break; // No setting wanted the remaining arguments
    }
    
    // Enforce that an initial URL or a sitemap file are supplied, not both.
    if ((settings.sitemap.get() == null) && (settings.initUrl.get() == null)) {
      throw new ArgumentListException(
          "Need to specify either a sitemap file or an initial URL.");
    } else if ((settings.sitemap.get() != null)
        && (settings.initUrl.get() != null)) {
      throw new ArgumentListException(
          "Please specify EITHER an initial URL OR a sitemap file.");
    }
    return settings;
  }

  /**
   * Displays usage information.
   * 
   * @return help message
   */
  public static String settingsHelp() {
    StringBuffer help = new StringBuffer();
    for (Setting<?> setting : new Settings().allSettings) {
      help.append(setting.getHelp() + "\n");
    }
    return help.toString();
  }

  private List<Setting<?>> allSettings = new ArrayList<Setting<?>>();

  public final Setting<String> sitemap = addSetting(new StringSetting(
      "-sitemap", "filename", null, "name of the file that contains a sitemap"));

  public final Setting<String> initUrl = addSetting(new StringSetting(
      "-initUrl", "URL", null, "initial URL to start crawl from"));

  public final Setting<String> out = addSetting(new StringSetting("-out",
      "filename", null, "file that output will be written to"));

  private <T> Setting<T> addSetting(Setting<T> setting) {
    allSettings.add(setting);
    return setting;
  }

}
