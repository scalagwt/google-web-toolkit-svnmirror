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
package org.hibernate.jsr303.tck.tests.validation;

/**
 * Test wrapper for {@link ValidationTest}.
 */
public class ValidationGwtTest extends AbstractValidationTest {

  private final ValidationTest delegate = new ValidationTest();

  public void notGwtCompatibleTestVerifyMethodsOfValidationObjects() {
    // This test relies on reflection so it is not run.
    // delegate.testVerifyMethodsOfValidationObjects();
  }

  public void testBuildDefaultValidatorFactory() {
    delegate.testBuildDefaultValidatorFactory();
  }

  public void testCustomValidationProviderResolution() {
    delegate.testCustomValidationProviderResolution();
  }

  public void testSpecificValidationProvider() {
    delegate.testSpecificValidationProvider();
  }
}
