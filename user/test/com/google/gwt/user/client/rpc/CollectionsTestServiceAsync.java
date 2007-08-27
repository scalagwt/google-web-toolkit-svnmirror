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
package com.google.gwt.user.client.rpc;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

/**
 * TODO: document me.
 */
public interface CollectionsTestServiceAsync {
  void echo(ArrayList value, AsyncCallback callback);

  void echo(boolean[] value, AsyncCallback callback);

  void echo(Boolean[] value, AsyncCallback callback);

  void echo(byte[] value, AsyncCallback callback);

  void echo(Byte[] value, AsyncCallback callback);

  void echo(char[] value, AsyncCallback callback);

  void echo(Character[] value, AsyncCallback callback);

  void echo(Date[] date, AsyncCallback callback);

  void echo(double[] value, AsyncCallback callback);

  void echo(Double[] value, AsyncCallback callback);

  void echo(float[] value, AsyncCallback callback);

  void echo(Float[] value, AsyncCallback callback);

  void echo(HashMap value, AsyncCallback callback);

  void echo(HashSet value, AsyncCallback callback);

  void echo(int[] value, AsyncCallback callback);

  void echo(Integer[] value, AsyncCallback callback);

  void echo(long[] value, AsyncCallback callback);

  void echo(Long[] value, AsyncCallback callback);

  void echo(short[] value, AsyncCallback callback);

  void echo(Short[] value, AsyncCallback callback);

  void echo(String[] value, AsyncCallback callback);

  void echo(String[][] value, AsyncCallback callback);

  void echo(Vector value, AsyncCallback callback);

  void getArraysAsList(List value, AsyncCallback callback);
}
