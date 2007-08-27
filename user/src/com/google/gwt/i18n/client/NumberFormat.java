/*
 * Copyright 2007 Google Inc.
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
import com.google.gwt.i18n.client.constants.CurrencyCodeMapConstants;
import com.google.gwt.i18n.client.constants.NumberConstants;

import java.util.Map;

/**
 * Formats and parses numbers using locale-sensitive patterns.
 * 
 * This class provides comprehensive and flexible support for a wide variety of
 * localized formats, including
 * <ul>
 * <li><b>Locale-specific symbols</b> such as decimal point, group separator,
 * digit representation, currency symbol, percent, and permill</li>
 * <li><b>Numeric variations</b> including integers ("123"), fixed-point
 * numbers ("123.4"), scientific notation ("1.23E4"), percentages ("12%"), and
 * currency amounts ("$123")</li>
 * <li><b>Predefined standard patterns</b> that can be used both for parsing
 * and formatting, including {@link #getDecimalFormat() decimal},
 * {@link #getCurrencyFormat() currency},
 * {@link #getPercentFormat() percentages}, and
 * {@link #getScientificFormat() scientific}</li>
 * <li><b>Custom patterns</b> and supporting features designed to make it
 * possible to parse and format numbers in any locale, including support for
 * Western, Arabic, and Indic digits</li>
 * </ul>
 * 
 * <h3>Patterns</h3>
 * <p>
 * Formatting and parsing are based on customizable patterns that can include a
 * combination of literal characters and special characters that act as
 * placeholders and are replaced by their localized counterparts. Many
 * characters in a pattern are taken literally; they are matched during parsing
 * and output unchanged during formatting. Special characters, on the other
 * hand, stand for other characters, strings, or classes of characters. For
 * example, the '<code>#</code>' character is replaced by a localized digit.
 * </p>
 * 
 * <p>
 * Often the replacement character is the same as the pattern character. In the
 * U.S. locale, for example, the '<code>,</code>' grouping character is
 * replaced by the same character '<code>,</code>'. However, the replacement
 * is still actually happening, and in a different locale, the grouping
 * character may change to a different character, such as '<code>.</code>'.
 * Some special characters affect the behavior of the formatter by their
 * presence. For example, if the percent character is seen, then the value is
 * multiplied by 100 before being displayed.
 * </p>
 * 
 * <p>
 * The characters listed below are used in patterns. Localized symbols use the
 * corresponding characters taken from corresponding locale symbol collection,
 * which can be found in the properties files residing in the
 * <code><nobr>com.google.gwt.i18n.client.constants</nobr></code>. To insert
 * a special character in a pattern as a literal (that is, without any special
 * meaning) the character must be quoted. There are some exceptions to this
 * which are noted below.
 * </p>
 * 
 * <table>
 * <tr>
 * <th>Symbol</th>
 * <th>Location</th>
 * <th>Localized?</th>
 * <th>Meaning</th>
 * </tr>
 * 
 * <tr>
 * <td><code>0</code></td>
 * <td>Number</td>
 * <td>Yes</td>
 * <td>Digit</td>
 * </tr>
 * 
 * <tr>
 * <td><code>#</code></td>
 * <td>Number</td>
 * <td>Yes</td>
 * <td>Digit, zero shows as absent</td>
 * </tr>
 * 
 * <tr>
 * <td><code>.</code></td>
 * <td>Number</td>
 * <td>Yes</td>
 * <td>Decimal separator or monetary decimal separator</td>
 * </tr>
 * 
 * <tr>
 * <td><code>-</code></td>
 * <td>Number</td>
 * <td>Yes</td>
 * <td>Minus sign</td>
 * </tr>
 * 
 * <tr>
 * <td><code>,</code></td>
 * <td>Number</td>
 * <td>Yes</td>
 * <td>Grouping separator</td>
 * </tr>
 * 
 * <tr>
 * <td><code>E</code></td>
 * <td>Number</td>
 * <td>Yes</td>
 * <td>Separates mantissa and exponent in scientific notation; need not be
 * quoted in prefix or suffix</td>
 * </tr>
 * 
 * <tr>
 * <td><code>E</code></td>
 * <td>Subpattern boundary</td>
 * <td>Yes</td>
 * <td>Separates positive and negative subpatterns</td>
 * </tr>
 * 
 * <tr>
 * <td><code>%</code></td>
 * <td>Prefix or suffix</td>
 * <td>Yes</td>
 * <td>Multiply by 100 and show as percentage</td>
 * </tr>
 * 
 * <tr>
 * <td><nobr><code>\u2030</code> (\u005Cu2030)</nobr></td>
 * <td>Prefix or suffix</td>
 * <td>Yes</td>
 * <td>Multiply by 1000 and show as per mille</td>
 * </tr>
 * 
 * <tr>
 * <td><nobr><code>\u00A4</code> (\u005Cu00A4)</nobr></td>
 * <td>Prefix or suffix</td>
 * <td>No</td>
 * <td>Currency sign, replaced by currency symbol; if doubled, replaced by
 * international currency symbol; if present in a pattern, the monetary decimal
 * separator is used instead of the decimal separator</td>
 * </tr>
 * 
 * <tr>
 * <td><code>'</code></td>
 * <td>Prefix or suffix</td>
 * <td>No</td>
 * <td>Used to quote special characters in a prefix or suffix; for example,
 * <code>"'#'#"</code> formats <code>123</code> to <code>"#123"</code>;
 * to create a single quote itself, use two in succession, such as
 * <code>"# o''clock"</code></td>
 * </tr>
 * 
 * </table>
 * 
 * <p>
 * A <code>NumberFormat</code> pattern contains a postive and negative
 * subpattern separated by a semicolon, such as
 * <code>"#,##0.00;(#,##0.00)"</code>. Each subpattern has a prefix, a
 * numeric part, and a suffix. If there is no explicit negative subpattern, the
 * negative subpattern is the localized minus sign prefixed to the positive
 * subpattern. That is, <code>"0.00"</code> alone is equivalent to
 * <code>"0.00;-0.00"</code>. If there is an explicit negative subpattern, it
 * serves only to specify the negative prefix and suffix; the number of digits,
 * minimal digits, and other characteristics are ignored in the negative
 * subpattern. That means that <code>"#,##0.0#;(#)"</code> has precisely the
 * same result as <code>"#,##0.0#;(#,##0.0#)"</code>.
 * </p>
 * 
 * <p>
 * The prefixes, suffixes, and various symbols used for infinity, digits,
 * thousands separators, decimal separators, etc. may be set to arbitrary
 * values, and they will appear properly during formatting. However, care must
 * be taken that the symbols and strings do not conflict, or parsing will be
 * unreliable. For example, the decimal separator and thousands separator should
 * be distinct characters, or parsing will be impossible.
 * </p>
 * 
 * <p>
 * The grouping separator is a character that separates clusters of integer
 * digits to make large numbers more legible. It commonly used for thousands,
 * but in some locales it separates ten-thousands. The grouping size is the
 * number of digits between the grouping separators, such as 3 for "100,000,000"
 * or 4 for "1 0000 0000".
 * </p>
 * 
 * <h3>Pattern Grammar (BNF)</h3>
 * <p>
 * The pattern itself uses the following grammar:
 * </p>
 * 
 * <table>
 * <tr>
 * <td>pattern</td>
 * <td>:=</td>
 * <td style="white-space: nowrap">subpattern ('<code>;</code>'
 * subpattern)?</td>
 * </tr>
 * <tr>
 * <td>subpattern</td>
 * <td>:=</td>
 * <td>prefix? number exponent? suffix?</td>
 * </tr>
 * <tr>
 * <td>number</td>
 * <td>:=</td>
 * <td style="white-space: nowrap">(integer ('<code>.</code>' fraction)?) |
 * sigDigits</td>
 * </tr>
 * <tr>
 * <td>prefix</td>
 * <td>:=</td>
 * <td style="white-space: nowrap">'<code>\u005Cu0000</code>'..'<code>\u005CuFFFD</code>' -
 * specialCharacters</td>
 * </tr>
 * <tr>
 * <td>suffix</td>
 * <td>:=</td>
 * <td style="white-space: nowrap">'<code>\u005Cu0000</code>'..'<code>\u005CuFFFD</code>' -
 * specialCharacters</td>
 * </tr>
 * <tr>
 * <td>integer</td>
 * <td>:=</td>
 * <td style="white-space: nowrap">'<code>#</code>'* '<code>0</code>'*'<code>0</code>'</td>
 * </tr>
 * <tr>
 * <td>fraction</td>
 * <td>:=</td>
 * <td style="white-space: nowrap">'<code>0</code>'* '<code>#</code>'*</td>
 * </tr>
 * <tr>
 * <td>sigDigits</td>
 * <td>:=</td>
 * <td style="white-space: nowrap">'<code>#</code>'* '<code>@</code>''<code>@</code>'* '<code>#</code>'*</td>
 * </tr>
 * <tr>
 * <td>exponent</td>
 * <td>:=</td>
 * <td style="white-space: nowrap">'<code>E</code>' '<code>+</code>'? '<code>0</code>'* '<code>0</code>'</td>
 * </tr>
 * <tr>
 * <td>padSpec</td>
 * <td>:=</td>
 * <td style="white-space: nowrap">'<code>*</code>' padChar</td>
 * </tr>
 * <tr>
 * <td>padChar</td>
 * <td>:=</td>
 * <td>'<code>\u005Cu0000</code>'..'<code>\u005CuFFFD</code>' - quote</td>
 * </tr>
 * </table>
 * 
 * <p>
 * Notation:
 * </p>
 * 
 * <table>
 * <tr>
 * <td>X*</td>
 * <td style="white-space: nowrap">0 or more instances of X</td>
 * </tr>
 * 
 * <tr>
 * <td>X?</td>
 * <td style="white-space: nowrap">0 or 1 instances of X</td>
 * </tr>
 * 
 * <tr>
 * <td>X|Y</td>
 * <td style="white-space: nowrap">either X or Y</td>
 * </tr>
 * 
 * <tr>
 * <td>C..D</td>
 * <td style="white-space: nowrap">any character from C up to D, inclusive</td>
 * </tr>
 * 
 * <tr>
 * <td>S-T</td>
 * <td style="white-space: nowrap">characters in S, except those in T</td>
 * </tr>
 * </table>
 * 
 * <p>
 * The first subpattern is for positive numbers. The second (optional)
 * subpattern is for negative numbers.
 * </p>
 */
public class NumberFormat {

  // Sets of constants as defined for the default locale.
  private static final NumberConstants defaultNumberConstants = (NumberConstants) GWT.create(NumberConstants.class);
  private static final CurrencyCodeMapConstants defaultCurrencyCodeMapConstants = (CurrencyCodeMapConstants) GWT.create(CurrencyCodeMapConstants.class);

  // Constants for characters used in programmatic (unlocalized) patterns.
  private static final char PATTERN_ZERO_DIGIT = '0';
  private static final char PATTERN_GROUPING_SEPARATOR = ',';
  private static final char PATTERN_DECIMAL_SEPARATOR = '.';
  private static final char PATTERN_PER_MILLE = '\u2030';
  private static final char PATTERN_PERCENT = '%';
  private static final char PATTERN_DIGIT = '#';
  private static final char PATTERN_SEPARATOR = ';';
  private static final char PATTERN_EXPONENT = 'E';
  private static final char PATTERN_MINUS = '-';
  private static final char CURRENCY_SIGN = '\u00A4';
  private static final char QUOTE = '\'';

  // Cached instances of standard formatters.
  private static NumberFormat cachedDecimalFormat;
  private static NumberFormat cachedScientificFormat;
  private static NumberFormat cachedPercentFormat;
  private static NumberFormat cachedCurrencyFormat;

  /**
   * Provides the standard currency format for the default locale.
   * 
   * @return a <code>NumberFormat</code> capable of producing and consuming
   *         currency format for the default locale
   */
  public static NumberFormat getCurrencyFormat() {
    if (cachedCurrencyFormat == null) {
      cachedCurrencyFormat = new NumberFormat(
          defaultNumberConstants.currencyPattern(),
          defaultNumberConstants.defCurrencyCode());
    }
    return cachedCurrencyFormat;
  }

  /**
   * Provides the standard decimal format for the default locale.
   * 
   * @return a <code>NumberFormat</code> capable of producing and consuming
   *         decimal format for the default locale
   */
  public static NumberFormat getDecimalFormat() {
    if (cachedDecimalFormat == null) {
      cachedDecimalFormat = new NumberFormat(
          defaultNumberConstants.decimalPattern(),
          defaultNumberConstants.defCurrencyCode());
    }
    return cachedDecimalFormat;
  }

  /**
   * Gets a <code>NumberFormat</code> instance for the default locale using
   * the specified pattern and the default currencyCode.
   * 
   * @param pattern pattern for this formatter
   * @return a NumberFormat instance
   * @throws IllegalArgumentException if the specified pattern is invalid
   */
  public static NumberFormat getFormat(String pattern) {
    return new NumberFormat(pattern, defaultNumberConstants.defCurrencyCode());
  }

  /**
   * Gets a custom <code>NumberFormat</code> instance for the default locale
   * using the specified pattern and currency code.
   * 
   * @param pattern pattern for this formatter
   * @param currencyCode international currency code
   * @return a NumberFormat instance
   * @throws IllegalArgumentException if the specified pattern is invalid
   */
  public static NumberFormat getFormat(String pattern, String currencyCode) {
    return new NumberFormat(pattern, currencyCode);
  }

  /**
   * Provides the standard percent format for the default locale.
   * 
   * @return a <code>NumberFormat</code> capable of producing and consuming
   *         percent format for the default locale
   */
  public static NumberFormat getPercentFormat() {
    if (cachedPercentFormat == null) {
      cachedPercentFormat = new NumberFormat(
          defaultNumberConstants.percentPattern(),
          defaultNumberConstants.defCurrencyCode());
    }
    return cachedPercentFormat;
  }

  /**
   * Provides the standard scientific format for the default locale.
   * 
   * @return a <code>NumberFormat</code> capable of producing and consuming
   *         scientific format for the default locale
   */
  public static NumberFormat getScientificFormat() {
    if (cachedScientificFormat == null) {
      cachedScientificFormat = new NumberFormat(
          defaultNumberConstants.scientificPattern(),
          defaultNumberConstants.defCurrencyCode());
    }
    return cachedScientificFormat;
  }

  // Locale specific symbol collection.
  private final NumberConstants numberConstants;

  private int maximumIntegerDigits = 40;
  private int minimumIntegerDigits = 1;
  private int maximumFractionDigits = 3; // invariant, >= minFractionDigits.
  private int minimumFractionDigits = 0;
  private int minExponentDigits;

  private String positivePrefix = "";
  private String positiveSuffix = "";
  private String negativePrefix = "-";
  private String negativeSuffix = "";

  // The multiplier for use in percent, per mille, etc.
  private int multiplier = 1;

  // The number of digits between grouping separators in the integer
  // portion of a number.
  private int groupingSize = 3;

  // Forces the decimal separator to always appear in a formatted number.
  private boolean decimalSeparatorAlwaysShown = false;

  private boolean isCurrencyFormat = false;

  // True to force the use of exponential (i.e. scientific) notation.
  private boolean useExponentialNotation = false;

  // Currency setting.
  private final String currencySymbol;

  // The currency code.
  private final String currencyCode;

  // The pattern to use for formatting and parsing.
  private final String pattern;

  /**
   * Constructs a format object based on the specified settings.
   * 
   * @param numberConstants the locale-specific number constants to use for this
   *          format
   * @param currencyCodeMapConstants the locale-specific currency code map to
   *          use for this format
   * @param pattern pattern that specify how number should be formatted
   * @param currencyCode currency that should be used
   * @skip
   */
  protected NumberFormat(NumberConstants numberConstants,
      CurrencyCodeMapConstants currencyCodeMapConstants, String pattern,
      String currencyCode) {
    this.numberConstants = numberConstants;
    this.pattern = pattern;
    this.currencyCode = currencyCode;

    Map currencyMap = currencyCodeMapConstants.currencyMap();
    currencySymbol = (String) currencyMap.get(currencyCode);

    parsePattern(this.pattern);
  }

  /**
   * Constructs a format object for the default locale based on the specified
   * settings.
   * 
   * @param pattern pattern that specify how number should be formatted
   * @param currencyCode currency that should be used
   * @skip
   */
  protected NumberFormat(String pattern, String currencyCode) {
    this(defaultNumberConstants, defaultCurrencyCodeMapConstants, pattern,
        currencyCode);
  }

  /**
   * This method formats a double to produce a string.
   * 
   * @param number The double to format
   * @return the formatted number string
   */
  public String format(double number) {
    StringBuffer result = new StringBuffer();

    if (Double.isNaN(number)) {
      result.append(numberConstants.notANumber());
      return result.toString();
    }

    boolean isNegative = ((number < 0.0) || (number == 0.0 && 1 / number < 0.0));

    result.append(isNegative ? negativePrefix : positivePrefix);
    if (Double.isInfinite(number)) {
      result.append(numberConstants.infinity());
    } else {
      if (isNegative) {
        number = -number;
      }
      number *= multiplier;
      if (useExponentialNotation) {
        subformatExponential(number, result);
      } else {
        subformatFixed(number, result, minimumIntegerDigits);
      }
    }

    result.append(isNegative ? negativeSuffix : positiveSuffix);

    return result.toString();
  }

  /**
   * Returns the pattern used by this number format.
   */
  public String getPattern() {
    return pattern;
  }

  /**
   * Parses text to produce a numeric value. A {@link NumberFormatException} is
   * thrown if either the text is empty or if the parse does not consume all
   * characters of the text.
   * 
   * @param text the string being parsed
   * @return a parsed number value
   * @throws NumberFormatException if the entire text could not be converted
   *           into a number
   */
  public double parse(String text) {
    int[] pos = {0};
    double result = parse(text, pos);
    if (pos[0] == 0 || pos[0] != text.length()) {
      throw new NumberFormatException(text);
    }
    return result;
  }

  /**
   * Parses text to produce a numeric value.
   * 
   * <p>
   * The method attempts to parse text starting at the index given by pos. If
   * parsing succeeds, then the index of <code>pos</code> is updated to the
   * index after the last character used (parsing does not necessarily use all
   * characters up to the end of the string), and the parsed number is returned.
   * The updated <code>pos</code> can be used to indicate the starting point
   * for the next call to this method. If an error occurs, then the index of
   * <code>pos</code> is not changed.
   * </p>
   * 
   * @param text the string to be parsed
   * @param inOutPos position to pass in and get back
   * @return a double value representing the parsed number, or <code>0.0</code>
   *         if the parse fails
   */
  public double parse(String text, int[] inOutPos) {
    int start = inOutPos[0];
    boolean gotPositive, gotNegative;
    double ret = 0.0;

    gotPositive = (text.indexOf(positivePrefix, inOutPos[0]) == inOutPos[0]);
    gotNegative = (text.indexOf(negativePrefix, inOutPos[0]) == inOutPos[0]);

    if (gotPositive && gotNegative) {
      if (positivePrefix.length() > negativePrefix.length()) {
        gotNegative = false;
      } else if (positivePrefix.length() < negativePrefix.length()) {
        gotPositive = false;
      }
    }

    if (gotPositive) {
      inOutPos[0] += positivePrefix.length();
    } else if (gotNegative) {
      inOutPos[0] += negativePrefix.length();
    }

    // Process digits or Inf, and find decimal position.
    if (text.indexOf(numberConstants.infinity(), inOutPos[0]) == inOutPos[0]) {
      inOutPos[0] += numberConstants.infinity().length();
      ret = Double.POSITIVE_INFINITY;
    } else if (text.indexOf(numberConstants.notANumber(), inOutPos[0]) == inOutPos[0]) {
      inOutPos[0] += numberConstants.notANumber().length();
      ret = Double.NaN;
    } else {
      ret = parseNumber(text, inOutPos);
    }

    // Check for suffix.
    if (gotPositive) {
      if (!(text.indexOf(positiveSuffix, inOutPos[0]) == inOutPos[0])) {
        inOutPos[0] = start;
        return 0.0;
      }
      inOutPos[0] += positiveSuffix.length();
    } else if (gotNegative) {
      if (!(text.indexOf(negativeSuffix, inOutPos[0]) == inOutPos[0])) {
        inOutPos[0] = start;
        return 0.0;
      }
      inOutPos[0] += negativeSuffix.length();
    }

    if (gotNegative) {
      ret = -ret;
    }

    return ret;
  }

  /**
   * This method formats the exponent part of a double.
   * 
   * @param exponent exponential value
   * @param result formatted exponential part will be append to it
   */
  private void addExponentPart(int exponent, StringBuffer result) {
    result.append(numberConstants.exponentialSymbol());

    if (exponent < 0) {
      exponent = -exponent;
      result.append(numberConstants.minusSign());
    }

    String exponentDigits = String.valueOf(exponent);
    for (int i = exponentDigits.length(); i < minExponentDigits; ++i) {
      result.append(numberConstants.zeroDigit());
    }
    result.append(exponentDigits);
  }

  /**
   * This method return the digit that represented by current character, it
   * could be either '0' to '9', or a locale specific digit.
   * 
   * @param ch character that represents a digit
   * @return the digit value
   */
  private int getDigit(char ch) {
    if ('0' <= ch && ch <= '0' + 9) {
      return (ch - '0');
    } else {
      char zeroChar = numberConstants.zeroDigit().charAt(0);
      return ((zeroChar <= ch && ch <= zeroChar + 9) ? (ch - zeroChar) : -1);
    }
  }

  /**
   * This method parses affix part of pattern.
   * 
   * @param pattern pattern string that need to be parsed
   * @param start start position to parse
   * @param affix store the parsed result
   * @return how many characters parsed
   */
  private int parseAffix(String pattern, int start, StringBuffer affix) {
    affix.delete(0, affix.length());
    boolean inQuote = false;
    int len = pattern.length();

    for (int pos = start; pos < len; ++pos) {
      char ch = pattern.charAt(pos);
      if (ch == QUOTE) {
        if ((pos + 1) < len && pattern.charAt(pos + 1) == QUOTE) {
          ++pos;
          affix.append("'"); // 'don''t'
        } else {
          inQuote = !inQuote;
        }
        continue;
      }

      if (inQuote) {
        affix.append(ch);
      } else {
        switch (ch) {
          case PATTERN_DIGIT:
          case PATTERN_ZERO_DIGIT:
          case PATTERN_GROUPING_SEPARATOR:
          case PATTERN_DECIMAL_SEPARATOR:
          case PATTERN_SEPARATOR:
            return pos - start;
          case CURRENCY_SIGN:
            isCurrencyFormat = true;
            if ((pos + 1) < len && pattern.charAt(pos + 1) == CURRENCY_SIGN) {
              ++pos;
              affix.append(currencyCode);
            } else {
              affix.append(currencySymbol);
            }
            break;
          case PATTERN_PERCENT:
            if (multiplier != 1) {
              throw new IllegalArgumentException(
                  "Too many percent/per mille characters in pattern \""
                      + pattern + '"');
            }
            multiplier = 100;
            affix.append(numberConstants.percent());
            break;
          case PATTERN_PER_MILLE:
            if (multiplier != 1) {
              throw new IllegalArgumentException(
                  "Too many percent/per mille characters in pattern \""
                      + pattern + '"');
            }
            multiplier = 1000;
            affix.append(numberConstants.perMill());
            break;
          case PATTERN_MINUS:
            affix.append("-");
            break;
          default:
            affix.append(ch);
        }
      }
    }
    return len - start;
  }

  /**
   * This function parses a "localized" text into a <code>double</code>. It
   * needs to handle locale specific decimal, grouping, exponent and digit.
   * 
   * @param text the text that need to be parsed
   * @param pos in/out parsing position. in case of failure, this shouldn't be
   *          changed
   * @return double value, could be 0.0 if nothing can be parsed
   */
  private double parseNumber(String text, int[] pos) {
    double ret;
    boolean sawDecimal = false;
    boolean sawExponent = false;
    boolean sawDigit = false;
    int scale = 1;
    String decimal = isCurrencyFormat ? numberConstants.monetarySeparator()
        : numberConstants.decimalSeparator();
    String grouping = isCurrencyFormat
        ? numberConstants.monetaryGroupingSeparator()
        : numberConstants.groupingSeparator();
    String exponentChar = numberConstants.exponentialSymbol();

    StringBuffer normalizedText = new StringBuffer();
    for (; pos[0] < text.length(); ++pos[0]) {
      char ch = text.charAt(pos[0]);
      int digit = getDigit(ch);
      if (digit >= 0 && digit <= 9) {
        normalizedText.append((char) (digit + '0'));
        sawDigit = true;
      } else if (ch == decimal.charAt(0)) {
        if (sawDecimal || sawExponent) {
          break;
        }
        normalizedText.append('.');
        sawDecimal = true;
      } else if (ch == grouping.charAt(0)) {
        if (sawDecimal || sawExponent) {
          break;
        }
        continue;
      } else if (ch == exponentChar.charAt(0)) {
        if (sawExponent) {
          break;
        }
        normalizedText.append('E');
        sawExponent = true;
      } else if (ch == '+' || ch == '-') {
        normalizedText.append(ch);
      } else if (ch == numberConstants.percent().charAt(0)) {
        if (scale != 1) {
          break;
        }
        scale = 100;
        if (sawDigit) {
          ++pos[0];
          break;
        }
      } else if (ch == numberConstants.perMill().charAt(0)) {
        if (scale != 1) {
          break;
        }
        scale = 1000;
        if (sawDigit) {
          ++pos[0];
          break;
        }
      } else {
        break;
      }
    }
    
    try {
      ret = Double.parseDouble(normalizedText.toString());
      ret = ret / scale;
      return ret;
    } catch (NumberFormatException e) {
      return 0.0;
    }
  }

  /**
   * Method parses provided pattern, result is stored in member variables.
   * 
   * @param pattern
   */
  private void parsePattern(String pattern) {
    int pos = 0;
    StringBuffer affix = new StringBuffer();

    pos += parseAffix(pattern, pos, affix);
    positivePrefix = affix.toString();
    int posPartLen = parseTrunk(pattern, pos);
    pos += posPartLen;
    pos += parseAffix(pattern, pos, affix);
    positiveSuffix = affix.toString();

    if (pos < pattern.length() && pattern.charAt(pos) == PATTERN_SEPARATOR) {
      ++pos;
      pos += parseAffix(pattern, pos, affix);
      negativePrefix = affix.toString();
      // The assumption made here is that negative part is identical to
      // positive part. User must make sure pattern is correctly constructed.
      pos += posPartLen;
      pos += parseAffix(pattern, pos, affix);
      negativeSuffix = affix.toString();
    }
  }

  /**
   * This method parses the trunk part of a pattern.
   * 
   * @param pattern pattern string that need to be parsed
   * @param start where parse started
   * @return how many characters parsed
   */
  private int parseTrunk(String pattern, int start) {
    int decimalPos = -1;
    int digitLeftCount = 0, zeroDigitCount = 0, digitRightCount = 0;
    byte groupingCount = -1;

    int len = pattern.length();
    int pos = start;
    boolean loop = true;
    for (; (pos < len) && loop; ++pos) {
      char ch = pattern.charAt(pos);
      switch (ch) {
        case PATTERN_DIGIT:
          if (zeroDigitCount > 0) {
            ++digitRightCount;
          } else {
            ++digitLeftCount;
          }
          if (groupingCount >= 0 && decimalPos < 0) {
            ++groupingCount;
          }
          break;
        case PATTERN_ZERO_DIGIT:
          if (digitRightCount > 0) {
            throw new IllegalArgumentException("Unexpected '0' in pattern \""
                + pattern + '"');
          }
          ++zeroDigitCount;
          if (groupingCount >= 0 && decimalPos < 0) {
            ++groupingCount;
          }
          break;
        case PATTERN_GROUPING_SEPARATOR:
          groupingCount = 0;
          break;
        case PATTERN_DECIMAL_SEPARATOR:
          if (decimalPos >= 0) {
            throw new IllegalArgumentException(
                "Multiple decimal separators in pattern \"" + pattern + '"');
          }
          decimalPos = digitLeftCount + zeroDigitCount + digitRightCount;
          break;
        case PATTERN_EXPONENT:
          if (useExponentialNotation) {
            throw new IllegalArgumentException("Multiple exponential "
                + "symbols in pattern \"" + pattern + '"');
          }
          useExponentialNotation = true;
          minExponentDigits = 0;

          // Use lookahead to parse out the exponential part
          // of the pattern, then jump into phase 2.
          while ((pos + 1) < len
              && pattern.charAt(pos + 1) == numberConstants.zeroDigit().charAt(
                  0)) {
            ++pos;
            ++minExponentDigits;
          }

          if ((digitLeftCount + zeroDigitCount) < 1 || minExponentDigits < 1) {
            throw new IllegalArgumentException("Malformed exponential "
                + "pattern \"" + pattern + '"');
          }
          loop = false;
          break;
        default:
          --pos;
          loop = false;
          break;
      }
    }

    if (zeroDigitCount == 0 && digitLeftCount > 0 && decimalPos >= 0) {
      // Handle "###.###" and "###." and ".###".
      int n = decimalPos;
      if (n == 0) { // Handle ".###"
        ++n;
      }
      digitRightCount = digitLeftCount - n;
      digitLeftCount = n - 1;
      zeroDigitCount = 1;
    }

    // Do syntax checking on the digits.
    if ((decimalPos < 0 && digitRightCount > 0)
        || (decimalPos >= 0 && (decimalPos < digitLeftCount || decimalPos > (digitLeftCount + zeroDigitCount)))
        || groupingCount == 0) {
      throw new IllegalArgumentException("Malformed pattern \"" + pattern + '"');
    }
    int totalDigits = digitLeftCount + zeroDigitCount + digitRightCount;

    maximumFractionDigits = (decimalPos >= 0 ? (totalDigits - decimalPos) : 0);
    if (decimalPos >= 0) {
      minimumFractionDigits = digitLeftCount + zeroDigitCount - decimalPos;
      if (minimumFractionDigits < 0) {
        minimumFractionDigits = 0;
      }
    }

    /*
     * The effectiveDecimalPos is the position the decimal is at or would be at
     * if there is no decimal. Note that if decimalPos<0, then digitTotalCount ==
     * digitLeftCount + zeroDigitCount.
     */
    int effectiveDecimalPos = decimalPos >= 0 ? decimalPos : totalDigits;
    minimumIntegerDigits = effectiveDecimalPos - digitLeftCount;
    if (useExponentialNotation) {
      maximumIntegerDigits = digitLeftCount + minimumIntegerDigits;

      // In exponential display, integer part can't be empty.
      if (maximumFractionDigits == 0 && minimumIntegerDigits == 0) {
        minimumIntegerDigits = 1;
      }
    }

    this.groupingSize = (groupingCount > 0) ? groupingCount : 0;
    decimalSeparatorAlwaysShown = (decimalPos == 0 || decimalPos == totalDigits);

    return pos - start;
  }

  /**
   * This method formats a <code>double</code> in exponential format.
   * 
   * @param number value need to be formated
   * @param result where the formatted string goes
   */
  private void subformatExponential(double number, StringBuffer result) {
    if (number == 0.0) {
      subformatFixed(number, result, minimumIntegerDigits);
      addExponentPart(0, result);
      return;
    }

    int exponent = (int) Math.floor(Math.log(number) / Math.log(10));
    number /= Math.pow(10, exponent);

    int minIntDigits = minimumIntegerDigits;
    if (maximumIntegerDigits > 1 && maximumIntegerDigits > minimumIntegerDigits) {
      // A repeating range is defined; adjust to it as follows.
      // If repeat == 3, we have 6,5,4=>3; 3,2,1=>0; 0,-1,-2=>-3;
      // -3,-4,-5=>-6, etc. This takes into account that the
      // exponent we have here is off by one from what we expect;
      // it is for the format 0.MMMMMx10^n.
      while ((exponent % maximumIntegerDigits) != 0) {
        number *= 10;
        exponent--;
      }
      minIntDigits = 1;
    } else {
      // No repeating range is defined; use minimum integer digits.
      if (minimumIntegerDigits < 1) {
        exponent++;
        number /= 10;
      } else {
        for (int i = 1; i < minimumIntegerDigits; i++) {
          exponent--;
          number *= 10;
        }
      }
    }

    subformatFixed(number, result, minIntDigits);
    addExponentPart(exponent, result);
  }

  /**
   * This method formats a <code>double</code> into a fractional
   * representation.
   * 
   * @param number value need to be formated
   * @param result result will be written here
   * @param minIntDigits minimum integer digits
   */
  private void subformatFixed(double number, StringBuffer result,
      int minIntDigits) {
    // Round the number.
    double power = Math.pow(10, maximumFractionDigits);
    number = Math.round(number * power);
    long intValue = (long) Math.floor(number / power);
    long fracValue = (long) Math.floor(number - intValue * power);

    boolean fractionPresent = (minimumFractionDigits > 0) || (fracValue > 0);

    String intPart = String.valueOf(intValue);
    String grouping = isCurrencyFormat
        ? numberConstants.monetaryGroupingSeparator()
        : numberConstants.groupingSeparator();
    String decimal = isCurrencyFormat ? numberConstants.monetarySeparator()
        : numberConstants.decimalSeparator();

    int zeroDelta = numberConstants.zeroDigit().charAt(0) - '0';
    int digitLen = intPart.length();

    if (intValue > 0 || minIntDigits > 0) {
      for (int i = digitLen; i < minIntDigits; i++) {
        result.append(numberConstants.zeroDigit());
      }

      for (int i = 0; i < digitLen; i++) {
        result.append((char) (intPart.charAt(i) + zeroDelta));

        if (digitLen - i > 1 && groupingSize > 0
            && ((digitLen - i) % groupingSize == 1)) {
          result.append(grouping);
        }
      }
    } else if (!fractionPresent) {
      // If there is no fraction present, and we haven't printed any
      // integer digits, then print a zero.
      result.append(numberConstants.zeroDigit());
    }

    // Output the decimal separator if we always do so.
    if (decimalSeparatorAlwaysShown || fractionPresent) {
      result.append(decimal);
    }

    // To make sure it lead zero will be kept.
    String fracPart = String.valueOf(fracValue + (long) power);
    int fracLen = fracPart.length();
    while (fracPart.charAt(fracLen - 1) == '0'
        && fracLen > minimumFractionDigits + 1) {
      fracLen--;
    }

    for (int i = 1; i < fracLen; i++) {
      result.append((char) (fracPart.charAt(i) + zeroDelta));
    }
  }
}
