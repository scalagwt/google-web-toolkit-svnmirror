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
package com.google.gwt.requestfactory.server;

import com.google.gwt.dev.util.Util;
import com.google.gwt.requestfactory.shared.RequestFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

/**
 * Class to populate the datastore with sample data in a JSON file.
 */
public class SampleDataPopulator {

  public static void main(String args[]) throws HttpException, JSONException, IOException {
    if (args.length < 2) {
      System.err.println("Require two arguments: URL string and fileName");
      System.exit(-1);
    }
    SampleDataPopulator populator = new SampleDataPopulator(args[0], args[1]);
    populator.populate();
  }

  private final String url;

  private final String filePathName;

  SampleDataPopulator(String url, String filePathName) {
    this.url = url;
    this.filePathName = filePathName;
  }

  public void populate() throws JSONException, HttpException, IOException {
    JSONObject jsonObject = readAsJsonObject(readFileAsString(filePathName));
    postJsonFile(jsonObject);
  }

  private void postJsonFile(JSONObject contentData) throws HttpException,
      IOException, JSONException {
    PostMethod post = new PostMethod(url);
    JSONObject request = new JSONObject();
    request.put("operation", RequestFactory.SYNC);
    request.put("contentData", contentData);
    post.setRequestBody(request.toString());
    HttpClient client = new HttpClient();
    int status = client.executeMethod(post);
    String response = post.getResponseBodyAsString();
    if (status == 200) {
      System.out.println("SUCCESS: Put all the records in the datastore!");
    } else {
      System.err.println("Error: Status code " + status + " returned");
    }
  }

  private JSONObject readAsJsonObject(String string) throws JSONException {
    JSONObject jsonObject = new JSONObject(string);
    return jsonObject;
  }

  private String readFileAsString(String filePathName) {
    File file = new File(filePathName);
    return Util.readFileAsString(file);
  }

}
