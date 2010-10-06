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
package com.google.gwt.validation.example.client;

import com.google.gwt.validation.client.GwtValidation;

import javax.validation.Validator;

/**
 * Example top level class to create a {@link Validator}.
 *
 * GWT.create instances of this class
 */
@GwtValidation(Author.class)
public interface ExampleGwtValidator extends Validator {
}
