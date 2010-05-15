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
package com.google.gwt.sample.expenses.gwt.server;

import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.KeyRange;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.requestfactory.server.RequestFactoryServlet;
import com.google.gwt.requestfactory.shared.RequestFactory.WriteOperation;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Custom servlet for the Expense app.
 */
public class ExpenseRequestFactoryServlet extends RequestFactoryServlet {
  private static final Set<String> USER_WHITE_LIST = initUserWhiteList();

  private static final String WHITELIST_MESSAGE = "The datastore cannot be modified until after Google I/O.";

  private static Set<String> initUserWhiteList() {
    Set<String> whiteList = new HashSet<String>();
    whiteList.add("jlabanca@google.com");
    whiteList.add("bruce@google.com");
    whiteList.add("amitmanjhi@google.com");
    whiteList.add("cramsdale@google.com");
    whiteList.add("jgw@google.com");
    whiteList.add("rjrjr@google.com");
    whiteList.add("cromwellian@google.com");
    whiteList.add("knorton@google.com");
    whiteList.add("scottb@google.com");
    whiteList.add("jaimeyap@google.com");
    whiteList.add("jasonparekh@google.com");
    whiteList.add("rdayal@google.com");
    whiteList.add("rice@google.com");
    whiteList.add("zundel@google.com");
    whiteList.add("ben.alex@gmail.com");
    return Collections.unmodifiableSet(whiteList);
  }

  /**
   * Check if the user is whitelisted to edit data..
   * 
   * @return true if white listed.
   */
  private static boolean isUserWhiteListed() {
    User curUser = UserServiceFactory.getUserService().getCurrentUser();
    return curUser != null
        && USER_WHITE_LIST.contains(curUser.getEmail().toLowerCase());
  }

  @Override
  protected Long generateIdForCreate(String key) {
    // TODO(jlabanca): Automatic IDs are being duplicated. Assigning an
    // ID manually for now.
    KeyRange range = DatastoreServiceFactory.getDatastoreService().allocateIds(
        key, 1);
    return range.getStart().getId();
  }

  @Override
  protected JSONObject updateRecordInDataStore(String recordToken,
      JSONObject recordObject, WriteOperation writeOperation) {

    // Ensure that the user is white listed.
    if (!isUserWhiteListed()) {
      JSONObject returnObject = new JSONObject();
      try {
        if (writeOperation == WriteOperation.DELETE
            || writeOperation == WriteOperation.UPDATE) {
          returnObject.put("id", recordObject.getString("id"));
        } else {
          returnObject.put("futureId", recordObject.getString("id"));
        }
        JSONObject violations = new JSONObject();
        violations.put("", WHITELIST_MESSAGE);
        returnObject.put("violations", violations);
        return returnObject;
      } catch (JSONException e) {
        // ignore.
        e.printStackTrace();
      }
    }

    return super.updateRecordInDataStore(recordToken, recordObject,
        writeOperation);
  }
}
