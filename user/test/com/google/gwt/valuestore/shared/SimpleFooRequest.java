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
package com.google.gwt.valuestore.shared;

import com.google.gwt.requestfactory.shared.Instance;
import com.google.gwt.requestfactory.shared.RecordListRequest;
import com.google.gwt.requestfactory.shared.RecordRequest;
import com.google.gwt.requestfactory.shared.RequestObject;
import com.google.gwt.requestfactory.shared.Service;

/**
 * Do nothing test interface.
 */
@Service(com.google.gwt.valuestore.server.SimpleFoo.class)
public interface SimpleFooRequest {
  RequestObject<Long> countSimpleFoo();

  @Instance
  RequestObject<Long> countSimpleFooWithUserNameSideEffect(SimpleFooRecord record);

  RecordListRequest<SimpleFooRecord> findAll();

  RecordRequest<SimpleFooRecord> findSimpleFooById(Long id);

  RequestObject<Integer> privateMethod();

  @Instance
  RequestObject<Void> persist(SimpleFooRecord record);
  
  @Instance
  RecordRequest<SimpleFooRecord> persistAndReturnSelf(SimpleFooRecord record);

  @Instance
  RequestObject<String> hello(SimpleFooRecord instance, SimpleBarRecord record);
}
