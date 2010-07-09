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
package com.google.gwt.i18n.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.constants.DateTimeConstants;
import com.google.gwt.i18n.client.constants.NumberConstants;
import com.google.gwt.i18n.client.impl.CldrImpl;
import com.google.gwt.i18n.client.impl.LocaleInfoImpl;

/**
 * Provides access to the currently-active locale and the list of available
 * locales.
 */
@SuppressWarnings("deprecation")
public class LocaleInfo {

  /**
   * Currently we only support getting the currently running locale, so this
   * is a static.  In the future, we would need a hash map from locale names
   * to LocaleInfo instances.
   */
  private static LocaleInfo instance  = new LocaleInfo(
      (LocaleInfoImpl) GWT.create(LocaleInfoImpl.class),
      (CldrImpl) GWT.create(CldrImpl.class));

  /**
   * @return an array of available locale names
   */
  public static final String[] getAvailableLocaleNames() {
    /*
     * The set of all locales is constant across all permutations, so this
     * is static.  Ideally, the set of available locales would be generated
     * by a different GWT.create but that would slow the compilation process
     * unnecessarily.
     * 
     * This is static, and accesses infoImpl this way, with an eye towards
     * when we implement static LocaleInfo getLocale(String localeName) as
     * you might want to get the list of available locales in order to create
     * instances of each of them.
     */
    return instance.infoImpl.getAvailableLocaleNames();
  }
  
  /**
   * @return a LocaleInfo instance for the current locale
   */
  public static final LocaleInfo getCurrentLocale() {
    /*
     * In the future, we could make additional static methods which returned a
     * LocaleInfo instance for a specific locale (from the set of those the app
     * was compiled with), accessed via a method like:
     *    public static LocaleInfo getLocale(String localeName)
     */
    return instance;
  }

  /**
   * Return the display name of the requested locale in its native locale, if
   * possible. If no native localization is available, the English name will
   * be returned, or as a last resort just the locale name will be returned.  If
   * the locale name is unknown (including an user overrides) or is not a valid
   * locale property value, null is returned.
   * 
   * If the I18N module has not been imported, this will always return null.
   * 
   * @param localeName the name of the locale to lookup.
   * @return the name of the locale in its native locale
   */
  public static String getLocaleNativeDisplayName(String localeName) {
    /*
     * See the comment from getAvailableLocaleNames() above.
     */
    return instance.infoImpl.getLocaleNativeDisplayName(localeName);
  }

  /**
   * @return true if any locale supported by this build of the app is RTL.
   */
  public static boolean hasAnyRTL() {
    return instance.infoImpl.hasAnyRTL();
  }

  private final LocaleInfoImpl infoImpl;

  private final CldrImpl cldrImpl;

  private DateTimeConstants dateTimeConstants;

  private DateTimeFormatInfo dateTimeFormatInfo;
  
  private NumberConstants numberConstants;

  /**
   * Constructor to be used by subclasses, such as mock classes for testing.
   * Any such subclass should override all methods.
   */
  protected LocaleInfo() {
    infoImpl = null;
    cldrImpl = null;
  }

  /**
   * Create a LocaleInfo instance, passing in the implementation classes.
   * 
   * @param impl LocaleInfoImpl instance to use
   * @param cldr CldrImpl instance to use
   */
  private LocaleInfo(LocaleInfoImpl impl, CldrImpl cldr) {
    this.infoImpl = impl;
    this.cldrImpl = cldr;
  }

  /**
   * @return a DateTimeConstants instance for this locale.
   */
  public final DateTimeConstants getDateTimeConstants() {
    ensureDateTimeConstants();
    return dateTimeConstants;
  }

  /**
   * @return a DateTimeConstants instance for this locale.
   */
  public final DateTimeFormatInfo getDateTimeFormatInfo() {
    ensureDateTimeFormatInfo();
    return dateTimeFormatInfo;
  }

  /**
   * @return the name of this locale, such as "default, "en_US", etc
   */
  public final String getLocaleName() {
    return infoImpl.getLocaleName();
  }

  /**
   * @return a NumberConstants instance for this locale.
   */
  public final NumberConstants getNumberConstants() {
    ensureNumberConstants();
    return numberConstants;
  }

  /**
   * @return true if this locale is right-to-left instead of left-to-right
   */
  public final boolean isRTL() {
    return cldrImpl.isRTL();
  }

  private void ensureDateTimeConstants() {
    if (dateTimeConstants == null) {
      ensureDateTimeFormatInfo();
      dateTimeConstants = new DateTimeConstantsAdapter(dateTimeFormatInfo);
    }
  }

  private void ensureDateTimeFormatInfo() {
    if (dateTimeFormatInfo == null) {
      dateTimeFormatInfo = infoImpl.getDateTimeFormatInfo();
    }
  }
  
  private void ensureNumberConstants() {
    if (numberConstants == null) {
      numberConstants = infoImpl.getNumberConstants();
    }
  }
}
