/*
 * Copyright 2007 Google Inc.
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
package com.google.gwt.user.server.rpc;

import java.lang.reflect.Method;

/**
 * Describes an incoming RPC request in terms of a resolved {@link Method} and
 * an array of arguments.
 */
public final class RPCRequest {

  /**
   * The method for this request.
   */
  private final Method method;

  /**
   * The parameters for this request.
   */
  private final Object[] parameters;

  /**
   * {@link SerializationPolicy} used for decoding this request and for encoding
   * the responses.
   */
  private final SerializationPolicy serializationPolicy;

  /**
   * Construct an RPCRequest.
   */
  public RPCRequest(Method method, Object[] parameters,
      SerializationPolicy serializationPolicy) {
    this.method = method;
    this.parameters = parameters;
    this.serializationPolicy = serializationPolicy;
  }

  /**
   * Get the request's method.
   */
  public Method getMethod() {
    return method;
  }

  /**
   * Get the request's parameters.
   */
  public Object[] getParameters() {
    return parameters;
  }

  /**
   * Returns the {@link SerializationPolicy} used to decode this request. This
   * is also the <code>SerializationPolicy</code> that should be used to
   * encode responses.
   * 
   * @return {@link SerializationPolicy} used to decode this request
   */
  public SerializationPolicy getSerializationPolicy() {
    return serializationPolicy;
  }
}
