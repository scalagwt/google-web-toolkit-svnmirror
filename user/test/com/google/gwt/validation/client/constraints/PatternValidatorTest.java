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
package com.google.gwt.validation.client.constraints;

import java.util.Date;

import javax.validation.constraints.Pattern;

/**
 * Tests for {@link PastValidatorForDate}
 */
public class PatternValidatorTest extends
    ConstraintValidatorTestCase<Pattern, String> {


  @SuppressWarnings("unused")
  @Pattern(regexp = "good")
  private Date defaultField;

  protected PatternValidator createValidator() {
    return new PatternValidator();
  }

  public void testAssertIsValid_good() {
    assertConstraintValidator("this is good", true);
  }

  public void testAssertIsValid_bad() {
    assertConstraintValidator("this is bad", false);
  }

  @Override
  protected Class<Pattern> getAnnotationClass() {
    return Pattern.class;
  }
}
