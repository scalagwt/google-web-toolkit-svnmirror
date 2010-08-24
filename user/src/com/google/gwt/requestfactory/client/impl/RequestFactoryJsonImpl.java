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
package com.google.gwt.requestfactory.client.impl;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.requestfactory.client.RequestFactoryLogHandler;
import com.google.gwt.requestfactory.shared.RequestEvent;
import com.google.gwt.requestfactory.shared.RequestFactory;
import com.google.gwt.requestfactory.shared.RequestObject;
import com.google.gwt.requestfactory.shared.RequestEvent.State;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.valuestore.shared.Record;
import com.google.gwt.valuestore.shared.WriteOperation;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>
 * <span style="color:red">Experimental API: This class is still under rapid
 * development, and is very likely to be deleted. Use it at your own risk.
 * </span>
 * </p>
 * Base implementation of RequestFactory.
 */
public abstract class RequestFactoryJsonImpl implements RequestFactory {

  static final boolean IS_FUTURE = true;
  static final boolean NOT_FUTURE = false;
  private static Logger logger = Logger.getLogger(RequestFactory.class.getName());

  // A separate logger for wire activity, which does not get logged by the
  // remote log handler, so we avoid infinite loops. All log messages that
  // could happen every time a request is made from the server should be logged
  // to this logger.
  private static Logger wireLogger = Logger.getLogger("WireActivityLogger");

  private static String SERVER_ERROR = "Server Error";

  private static final Integer INITIAL_VERSION = 1;

  private long currentFutureId = 0;

  private ValueStoreJsonImpl valueStore;

  private EventBus eventBus;

  public <R extends Record> R create(Class<R> token,
      RecordToTypeMap recordToTypeMap) {

    RecordSchema<R> schema = recordToTypeMap.getType(token);
    if (schema == null) {
      throw new IllegalArgumentException("Unknown proxy type: " + token);
    }

    return createFuture(schema);
  }

  public void fire(final RequestObject<?> requestObject) {
    RequestBuilder builder = new RequestBuilder(RequestBuilder.POST,
        GWT.getHostPageBaseURL() + RequestFactory.URL);
    builder.setHeader("Content-Type", RequestFactory.JSON_CONTENT_TYPE_UTF8);
    builder.setHeader("pageurl", Location.getHref());
    builder.setRequestData(ClientRequestHelper.getRequestString(requestObject.getRequestData().getRequestMap(
        ((AbstractRequest<?, ?>) requestObject).deltaValueStore.toJson())));
    builder.setCallback(new RequestCallback() {

      public void onError(Request request, Throwable exception) {
        postRequestEvent(State.RECEIVED, null);
        wireLogger.log(Level.SEVERE, SERVER_ERROR, exception);
      }

      public void onResponseReceived(Request request, Response response) {
        wireLogger.finest("Response received");
        if (200 == response.getStatusCode()) {
          String text = response.getText();
          ((AbstractRequest<?, ?>) requestObject).handleResponseText(text);
        } else if (Response.SC_UNAUTHORIZED == response.getStatusCode()) {
          wireLogger.finest("Need to log in");
        } else if (response.getStatusCode() > 0) {
          // During the redirection for logging in, we get a response with no
          // status code, but it's not an error, so we only log errors with
          // bad status codes here.
          wireLogger.severe(SERVER_ERROR + " " + response.getStatusCode() + " "
              + response.getText());
        }
        postRequestEvent(State.RECEIVED, response);
      }

    });

    try {
      wireLogger.finest("Sending fire request");
      builder.send();
      postRequestEvent(State.SENT, null);
    } catch (RequestException e) {
      wireLogger.log(Level.SEVERE, SERVER_ERROR + " (" + e.getMessage() + ")",
          e);
    }
  }

  public Class<? extends Record> getClass(Record proxy) {
    return ((RecordImpl) proxy).getSchema().getProxyClass();
  }

  public abstract RecordSchema<?> getSchema(String token);

  /**
   * @param eventBus
   */
  public void init(EventBus eventBus) {
    this.valueStore = new ValueStoreJsonImpl();
    this.eventBus = eventBus;
    Logger.getLogger("").addHandler(
        new RequestFactoryLogHandler(this, Level.WARNING, wireLogger.getName()));
    logger.fine("Successfully initialized RequestFactory");
  }

  protected Class<? extends Record> getClass(String token,
      RecordToTypeMap recordToTypeMap) {
    String[] bits = token.split("-");
    RecordSchema<? extends Record> schema = recordToTypeMap.getType(bits[0]);
    if (schema == null) {
      return null;
    }
    return schema.getProxyClass();
  }

  protected Record getProxy(String token, RecordToTypeMap recordToTypeMap) {
    String[] bits = token.split("-");
    if (bits.length < 2 || bits.length > 3) {
      return null;
    }

    RecordSchema<? extends Record> schema = recordToTypeMap.getType(bits[0]);
    if (schema == null) {
      return null;
    }

    if (bits.length == 3) {
      return createFuture(schema);
    }

    Long id = null;
    try {
      id = Long.valueOf(bits[1]);
    } catch (NumberFormatException e) {
      return null;
    }

    return schema.create(RecordJsoImpl.create(id, -1, schema));
  }

  protected String getToken(Record record, RecordToTypeMap recordToTypeMap) {
    Class<? extends Record> proxyClass = ((RecordImpl) record).getSchema().getProxyClass();
    String rtn = recordToTypeMap.getClassToken(proxyClass) + "-";
    if (((RecordImpl) record).isFuture()) {
      rtn += "0-FUTURE";
    } else {
      rtn += record.getId();
    }
    return rtn;
  }

  ValueStoreJsonImpl getValueStore() {
    return valueStore;
  }

  void postChangeEvent(RecordJsoImpl newJsoRecord, WriteOperation op) {
    /*
     * Ensure event receivers aren't accidentally using cached info by making an
     * unpopulated copy of the record.
     */
    newJsoRecord = RecordJsoImpl.emptyCopy(newJsoRecord);
    Record javaRecord = newJsoRecord.getSchema().create(newJsoRecord);
    eventBus.fireEvent(newJsoRecord.getSchema().createChangeEvent(javaRecord,
        op));
  }

  private <R extends Record> R createFuture(RecordSchema<R> schema) {
    Long futureId = ++currentFutureId;
    RecordJsoImpl newRecord = RecordJsoImpl.create(futureId, INITIAL_VERSION,
        schema);
    return schema.create(newRecord, IS_FUTURE);
  }

  private void postRequestEvent(State received, Response response) {
    eventBus.fireEvent(new RequestEvent(received, response));
  }
}
