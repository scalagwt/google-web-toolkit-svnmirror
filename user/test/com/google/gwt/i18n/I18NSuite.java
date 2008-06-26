/*
 * Copyright 2008 Google Inc.
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
package com.google.gwt.i18n;

import com.google.gwt.i18n.client.AnnotationsTest;
import com.google.gwt.i18n.client.ArabicPluralsTest;
import com.google.gwt.i18n.client.DateTimeFormat_de_Test;
import com.google.gwt.i18n.client.DateTimeParse_en_Test;
import com.google.gwt.i18n.client.DateTimeParse_zh_CN_Test;
import com.google.gwt.i18n.client.I18N2Test;
import com.google.gwt.i18n.client.I18NTest;
import com.google.gwt.i18n.client.LocaleInfoTest;
import com.google.gwt.i18n.client.LocaleInfo_ar_Test;
import com.google.gwt.i18n.client.NumberFormat_en_Test;
import com.google.gwt.i18n.client.NumberFormat_fr_Test;
import com.google.gwt.i18n.client.NumberParse_en_Test;
import com.google.gwt.i18n.client.NumberParse_fr_Test;
import com.google.gwt.i18n.rebind.AbstractResourceTest;
import com.google.gwt.junit.tools.GWTTestSuite;

import junit.framework.Test;

/**
 * All I18N tests.
 */
public class I18NSuite {
  public static Test suite() {
    GWTTestSuite suite = new GWTTestSuite("All I18N tests");

    // $JUnit-BEGIN$
    suite.addTestSuite(AbstractResourceTest.class);
    suite.addTestSuite(ArabicPluralsTest.class);
    suite.addTestSuite(AnnotationsTest.class);
    suite.addTestSuite(ConstantMapTest.class);
    suite.addTestSuite(DateTimeFormat_de_Test.class);
    suite.addTestSuite(DateTimeParse_en_Test.class);
    suite.addTestSuite(DateTimeParse_zh_CN_Test.class);
    suite.addTestSuite(I18NTest.class);
    suite.addTestSuite(I18N2Test.class);
    suite.addTestSuite(LocaleInfo_ar_Test.class);    
    suite.addTestSuite(LocaleInfoTest.class);
    suite.addTestSuite(NumberFormat_en_Test.class);
    suite.addTestSuite(NumberFormat_fr_Test.class);
    suite.addTestSuite(NumberParse_en_Test.class);
    suite.addTestSuite(NumberParse_fr_Test.class);
    // $JUnit-END$

    return suite;
  }
}
