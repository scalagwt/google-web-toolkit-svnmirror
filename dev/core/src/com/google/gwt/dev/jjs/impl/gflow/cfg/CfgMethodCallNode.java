package com.google.gwt.dev.jjs.impl.gflow.cfg;

import com.google.gwt.dev.jjs.ast.JMethodCall;

/**
 * Node corresponding to code blocks.
 */
public class CfgMethodCallNode extends CfgSimpleNode<JMethodCall> {
  public CfgMethodCallNode(CfgNode<?> parent, JMethodCall node) {
    super(parent, node);
  }

  @Override
  public void accept(CfgVisitor visitor) {
    visitor.visitMethodCallNode(this);
  }

  @Override
  public String toDebugString() {
    return "CALL(" + getJNode().getTarget().getName() + ")";
  }

  @Override
  protected CfgNode<?> cloneImpl() {
    return new CfgMethodCallNode(getParent(), getJNode());
  }
}