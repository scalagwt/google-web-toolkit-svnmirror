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
package com.google.gwt.dev.util.log;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.dev.util.log.SwingTreeLogger.LogEvent;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * Swing widget containing a tree logger.
 */
public class SwingLoggerPanel extends JPanel implements TreeSelectionListener {

  /**
   * Interface for notification when this panel is requested to close.
   */
  public interface CloseListener {
    void onClose();
  }

  private static class TreeRenderer extends DefaultTreeCellRenderer {
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
        boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
      super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row,
          hasFocus);
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
      Object userObject = node.getUserObject();
      setBackground(tree.getBackground());
      if (userObject instanceof LogEvent) {
        LogEvent event = (LogEvent) userObject;
        event.setDisplayProperties(this);
      }
      return this;
    }
  }

  // package protected for SwingTreeLogger to access

  final JTree tree;

  DefaultTreeModel treeModel;

  Type levelFilter;

  String regexFilter;

  private boolean autoScroll;

  private final JEditorPane details;

  private final AbstractTreeLogger logger;

  private DefaultMutableTreeNode root;

  private JTextField regexField;

  private JComboBox levelComboBox;

  private JPanel topPanel;

  private CloseListener closeListener;

  public SwingLoggerPanel(TreeLogger.Type maxLevel) {
    this(maxLevel, null);
  }

  public SwingLoggerPanel(TreeLogger.Type maxLevel, CloseListener closeListener) {
    super(new BorderLayout());
    this.closeListener = closeListener;
    regexFilter = "";
    levelFilter = maxLevel;
    topPanel = new JPanel();
    JButton expandButton = new JButton("Expand All");
    expandButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        expandAll();
      }
    });
    topPanel.add(expandButton);
    JButton collapsButton = new JButton("Collapse All");
    collapsButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        collapseAll();
      }
    });
    topPanel.add(collapsButton);
    topPanel.add(new JLabel("Filter Log Messages: "));
    levelComboBox = new JComboBox();
    for (TreeLogger.Type type : TreeLogger.Type.instances()) {
      if (type.compareTo(maxLevel) > 0) {
        break;
      }
      levelComboBox.addItem(type);
    }
    levelComboBox.setEditable(false);
    levelComboBox.setSelectedIndex(levelComboBox.getItemCount() - 1);
    topPanel.add(levelComboBox);
    levelComboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setLevelFilter((TreeLogger.Type) levelComboBox.getSelectedItem());
      }
    });
    regexField = new JTextField(20);
    topPanel.add(regexField);
    JButton applyRegexButton = new JButton("Apply Regex");
    applyRegexButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setRegexFilter(regexField.getText());
      }
    });
    topPanel.add(applyRegexButton);
    JButton clearRegexButton = new JButton("Clear Regex");
    clearRegexButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        regexField.setText("");
        setRegexFilter("");
      }
    });
    topPanel.add(clearRegexButton);
    add(topPanel, BorderLayout.NORTH);
    root = new DefaultMutableTreeNode();
    treeModel = new DefaultTreeModel(root);
    tree = new JTree(treeModel);
    tree.setRootVisible(false);
    tree.setEditable(false);
    tree.setExpandsSelectedPaths(true);
    tree.setShowsRootHandles(true);
    tree.setCellRenderer(new TreeRenderer());
    tree.getSelectionModel().setSelectionMode(
        TreeSelectionModel.SINGLE_TREE_SELECTION);
    tree.addTreeSelectionListener(this);
    JScrollPane treeView = new JScrollPane(tree);
    details = new JEditorPane() {
      @Override
      public boolean getScrollableTracksViewportWidth() {
        return true;
      }
    };
    details.setEditable(false);
    details.setContentType("text/html");
    details.setForeground(Color.BLACK);
    JScrollPane msgView = new JScrollPane(details);
    JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    splitter.setTopComponent(treeView);
    splitter.setBottomComponent(msgView);
    Dimension minSize = new Dimension(100, 50);
    msgView.setMinimumSize(minSize);
    treeView.setMinimumSize(minSize);
    splitter.setDividerLocation(0.80);
    add(splitter);
    logger = new SwingTreeLogger(this);
    logger.setMaxDetail(maxLevel);
  }

  @SuppressWarnings("unchecked")
  public void collapseAll() {
    Enumeration<DefaultMutableTreeNode> children = root.postorderEnumeration();
    while (children.hasMoreElements()) {
      DefaultMutableTreeNode node = children.nextElement();
      if (node != root) {
        tree.collapsePath(new TreePath(node.getPath()));
      }
    }
    tree.invalidate();
  }

  public void disconnected() {
    tree.setBackground(Color.decode("0xFFDDDD"));
    tree.repaint();
    if (closeListener != null) {
      JButton closeButton = new JButton("Close");
      closeButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          closeListener.onClose();
        }
      });
      topPanel.add(closeButton);
    }
  }

  @SuppressWarnings("unchecked")
  public void expandAll() {
    Enumeration<DefaultMutableTreeNode> children = root.postorderEnumeration();
    while (children.hasMoreElements()) {
      DefaultMutableTreeNode node = children.nextElement();
      if (node != root) {
        tree.expandPath(new TreePath(node.getPath()));
      }
    }
    tree.invalidate();
  }

  public boolean getAutoScroll() {
    return autoScroll;
  }

  public AbstractTreeLogger getLogger() {
    return logger;
  }

  public void notifyChange(DefaultMutableTreeNode node) {
    treeModel.nodeChanged(node);
  }

  public void removeAll() {
    tree.removeAll();
    details.setText("");
  }

  public void setAutoScroll(boolean autoScroll) {
    this.autoScroll = autoScroll;
  }

  public void valueChanged(TreeSelectionEvent e) {
    if (e.isAddedPath()) {
      TreePath path = e.getPath();
      Object treeNode = path.getLastPathComponent();
      Object userObject = ((DefaultMutableTreeNode) treeNode).getUserObject();
      String text = userObject.toString();
      if (userObject instanceof LogEvent) {
        LogEvent event = (LogEvent) userObject;
        text = event.getFullText();
      }
      details.setText(text);
    }
  }

  protected void setLevelFilter(Type selectedLevel) {
    levelFilter = selectedLevel;
    // TODO(jat): filter current tree
  }

  protected void setRegexFilter(String regex) {
    regexFilter = regex;
    // TODO(jat): filter current tree
  }
}
