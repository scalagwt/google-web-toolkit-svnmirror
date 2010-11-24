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

package com.google.gwt.requestfactory.shared;

import com.google.gwt.requestfactory.server.UserInformation;

/**
 * "API Generated" request selector interface implemented by objects that give
 * client access to the methods of {@link UserInformation}.
 */
@Service(UserInformation.class)
public interface UserInformationRequest extends RequestContext {

  /**
   * Returns the current user information given a redirect URL.
   *
   * @param redirectUrl the redirect UR as a String
   * @return an instance of {@link Request}&lt;{@link UserInformationProxy}&gt;
   */
  Request<UserInformationProxy> getCurrentUserInformation(String redirectUrl);
}
