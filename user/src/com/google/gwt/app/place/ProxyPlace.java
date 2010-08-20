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
package com.google.gwt.app.place;

import com.google.gwt.requestfactory.shared.RequestFactory;
import com.google.gwt.valuestore.shared.Record;

/**
 * A place in the app that deals with a specific {@link RequestFactory} proxy.
 */
public class ProxyPlace extends Place {
  /**
   * The things you do with a record, each of which is a different bookmarkable
   * location in the scaffold app.
   */
  public enum Operation {
    CREATE, EDIT, DETAILS
  }

  /**
   * Tokenizer.
   */
  @Prefix("r")
  public static class Tokenizer implements PlaceTokenizer<ProxyPlace> {
    private final RequestFactory requests;

    public Tokenizer(RequestFactory requests) {
      this.requests = requests;
    }

    public ProxyPlace getPlace(String token) {
      String bits[] = token.split("@");
      return new ProxyPlace(requests.getProxy(bits[0]),
          Operation.valueOf(bits[1]));
    }

    public String getToken(ProxyPlace place) {
      return requests.getToken(place.getProxy()) + "@" + place.getOperation();
    }
  }

  private final Record proxy;

  private final Operation operation;

  public ProxyPlace(Record record) {
    this(record, Operation.DETAILS);
  }
  
  public ProxyPlace(Record record, Operation operation) {
    this.operation = operation;
    this.proxy = record;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    ProxyPlace other = (ProxyPlace) obj;
    if (!operation.equals(other.operation)) {
      return false;
    }
    if (!proxy.getId().equals(other.proxy.getId())) {
      return false;
    }
    if (!proxy.getClass().equals(other.proxy.getClass())) {
      return false;
    }
    return true;
  }
  
  public Operation getOperation() {
    return operation;
  }

  public Record getProxy() {
    return proxy;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + operation.hashCode();
    result = prime * result + proxy.getId().hashCode();
    result = prime * result + proxy.getClass().hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "ProxyPlace [operation=" + operation + ", proxy=" + proxy + "]";
  }
}
