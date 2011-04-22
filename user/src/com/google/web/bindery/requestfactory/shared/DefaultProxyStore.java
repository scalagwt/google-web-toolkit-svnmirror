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
package com.google.web.bindery.requestfactory.shared;

import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.Splittable;
import com.google.web.bindery.requestfactory.shared.impl.MessageFactoryHolder;
import com.google.web.bindery.requestfactory.shared.messages.OperationMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * An in-memory ProxyStore store that can encode its state as a JSON object
 * literal.
 */
public class DefaultProxyStore implements ProxyStore {
  /**
   * Provide a little bit of future-proofing to at least detect an encoded
   * payload that can't be decoded.
   */
  private static final String EXPECTED_VERSION = "211";
  private final AutoBean<OperationMessage> messageBean;
  private final Map<String, Splittable> map;

  /**
   * Construct an empty DefaultProxyStore.
   */
  public DefaultProxyStore() {
    messageBean = MessageFactoryHolder.FACTORY.operation();
    map = new HashMap<String, Splittable>();

    OperationMessage message = messageBean.as();
    message.setPropertyMap(map);
    message.setVersion(EXPECTED_VERSION);
  }

  /**
   * Construct a DefaultProxyStore using the a value returned from
   * {@link #encode()}.
   * 
   * @param payload a String previously returned from {@link #encode()}
   * @throws IllegalArgumentException if the payload cannot be parsed
   */
  public DefaultProxyStore(String payload) throws IllegalArgumentException {
    messageBean = AutoBeanCodex.decode(MessageFactoryHolder.FACTORY,
        OperationMessage.class, payload);

    OperationMessage message = messageBean.as();
    if (!EXPECTED_VERSION.equals(message.getVersion())) {
      throw new IllegalArgumentException(
          "Unexpected version string in payload " + message.getVersion());
    }
    map = message.getPropertyMap();
  }

  /**
   * Return a JSON object literal with the contents of the store.
   */
  public String encode() {
    return AutoBeanCodex.encode(messageBean).getPayload();
  }

  public Splittable get(String key) {
    return map.get(key);
  }

  public int nextId() {
    return map.size();
  }

  public void put(String key, Splittable value) {
    map.put(key, value);
  }
}
