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
package com.google.gwt.valuestore.server;

import com.google.gwt.requestfactory.shared.Id;
import com.google.gwt.valuestore.shared.SimpleEnum;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Domain object for SimpleFooRequest.
 */
public class SimpleFoo {

  static SimpleFoo singleton = new SimpleFoo();

  private static Long nextId = 1L;

  public static Long countSimpleFoo() {
    return 1L;
  }

  public static List<SimpleFoo> findAll() {
    return Collections.singletonList(singleton);
  }

  public static SimpleFoo findSimpleFoo(Long id) {
    return findSimpleFooById(id);
  }

  public static SimpleFoo findSimpleFooById(Long id) {
    singleton.setId(id);
    return singleton;
  }

  public static SimpleFoo getSingleton() {
    return singleton;
  }

  public Long countSimpleFooWithUserNameSideEffect() {
    singleton.setUserName(userName);
    return 1L;
  }

  public String hello(SimpleBar bar) {
    return "Greetings " + bar.getUserName() + " from " + getUserName();  
  }

  public static void reset() {
    singleton = new SimpleFoo();
  }

  @SuppressWarnings("unused")
  private static Integer privateMethod() {
    return 0;
  }

  Integer version = 1;

  private Boolean boolField;
  private Date created;
  private SimpleEnum enumField;
  @Id
  private Long id = 1L;

  private Integer intId = -1;

  private Long longField;

  private String password;

  private String userName;

  private SimpleBar barField;

  private SimpleFoo fooField;

  public SimpleFoo() {
    intId = 42;
    version = 1;
    userName = "GWT";
    longField = 8L;
    enumField = SimpleEnum.FOO;
    created = new Date();
    barField = SimpleBar.getSingleton();
    boolField = true;
  }

  public SimpleBar getBarField() {
    return barField;
  }

  public Boolean getBoolField() {
    return boolField;
  }

  public Date getCreated() {
    return created;
  }

  public SimpleEnum getEnumField() {
    return enumField;
  }

  public SimpleFoo getFooField() {
    return fooField;
  }

  public Long getId() {
    return id;
  }

  public Integer getIntId() {
    return intId;
  }

  public Long getLongField() {
    return longField;
  }

  public String getPassword() {
    return password;
  }

  public String getUserName() {
    return userName;
  }

  public Integer getVersion() {
    return version;
  }

  public void persist() {
    setId(nextId++);
  }

  public SimpleFoo persistAndReturnSelf() {
    persist();
    return this;
  }

  public void setBarField(SimpleBar barField) {
    this.barField = barField;
  }

  public void setBoolField(Boolean bool) {
    boolField = bool;
  }
  
  public void setCreated(Date created) {
    this.created = created;
  }

  public void setEnumField(SimpleEnum enumField) {
    this.enumField = enumField;
  }

  public void setFooField(SimpleFoo fooField) {
    this.fooField = fooField;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setIntId(Integer id) {
    this.intId = id;
  }

  public void setLongField(Long longField) {
    this.longField = longField;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }
}
