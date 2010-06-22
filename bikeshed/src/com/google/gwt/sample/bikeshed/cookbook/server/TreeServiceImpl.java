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
package com.google.gwt.sample.bikeshed.cookbook.server;

import com.google.gwt.sample.bikeshed.cookbook.client.TreeService;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import java.util.List;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class TreeServiceImpl extends RemoteServiceServlet implements
    TreeService {

  public List<String> getNext(String prefix) {
    return Dictionary.getNext(prefix);
  }
}
