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
package com.google.gwt.dev.js;

import com.google.gwt.core.ext.soyc.Range;
import com.google.gwt.dev.jjs.HasSourceInfo;
import com.google.gwt.dev.jjs.SourceInfo;
import com.google.gwt.dev.jjs.SourceOrigin;
import com.google.gwt.dev.jjs.ast.JDeclaredType;
import com.google.gwt.dev.jjs.impl.JavaToJavaScriptMap;
import com.google.gwt.dev.js.ast.JsName;
import com.google.gwt.dev.js.ast.JsVisitable;
import com.google.gwt.dev.util.TextOutput;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A variation on the standard source generation visitor that records the
 * locations of SourceInfo objects in the output.
 */
public class JsReportGenerationVisitor extends
    JsSourceGenerationVisitorWithSizeBreakdown {
  private final Map<Range, SourceInfo> sourceInfoMap = new HashMap<Range, SourceInfo>();
  private final TextOutput out;

  public JsReportGenerationVisitor(TextOutput out, JavaToJavaScriptMap map) {
    super(out, map);
    this.out = out;
  }

  @Override
  protected <T extends JsVisitable> T generateAndBill(T node, JsName nameToBillTo) {

    if (!(node instanceof HasSourceInfo)) {
      return super.generateAndBill(node, nameToBillTo);
    }

    // Remember the position before generating the JavaScript.
    int beforePosition = out.getPosition();
    int beforeLine = out.getLine();
    int beforeColumn = out.getColumn();

    // Write some JavaScript (changing the position).
    T toReturn = super.generateAndBill(node, nameToBillTo);

    Range javaScriptRange = new Range(beforePosition, out.getPosition(),
        beforeLine, beforeColumn, out.getLine(), out.getColumn());

    SourceInfo defaultTarget = ((HasSourceInfo) node).getSourceInfo();
    SourceInfo newTarget = findTarget(nameToBillTo, defaultTarget);
    sourceInfoMap.put(javaScriptRange, newTarget);

    return toReturn;
  }

  @Override
  public Map<Range, SourceInfo> getSourceInfoMap() {
    return Collections.unmodifiableMap(sourceInfoMap);
  }

  @Override
  protected <T extends JsVisitable> void doAcceptList(List<T> collection) {
    for (T t : collection) {
      doAccept(t);
    }
  }

  @Override
  protected <T extends JsVisitable> void doAcceptWithInsertRemove(
      List<T> collection) {
    for (T t : collection) {
      doAccept(t);
    }
  }

  /**
   * Finds the Java filename and line number that we want in the source map.
   * (This needs to be a relative path that makes sense as a URL.)
   */
  private SourceInfo findTarget(JsName nameToBillTo, SourceInfo defaultTarget) {
    String newFilename = findTargetFile(nameToBillTo, defaultTarget.getFileName());

    if (newFilename == defaultTarget.getFileName()) {
      return defaultTarget;
    } else {
      return SourceOrigin.create(defaultTarget.getStartLine(), newFilename);
    }
  }

  /**
   * Finds the name of the Java file that we want to put in the source map.
   */
  private String findTargetFile(JsName nameToBillTo, String defaultFilename) {
    // For the filename, we really want the path passed to ResourceLoader.getResource().
    // But for now, fake it based on the type name.
    // TODO(skybrian): fix

    JDeclaredType type = getDirectlyEnclosingType(nameToBillTo);
    if (type == null) {
      return defaultFilename;
    }

    // remove inner classes
    while (type.getEnclosingType() != null) {
      type = type.getEnclosingType();
    }

    return type.getName().replace('.', '/') + ".java";
  }
}
