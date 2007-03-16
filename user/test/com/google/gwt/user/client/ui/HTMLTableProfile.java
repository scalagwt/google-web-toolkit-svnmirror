// Copyright 2006 Google Inc. All Rights Reserved.
package com.google.gwt.user.client.ui;

import com.google.gwt.user.client.ui.HTMLTable.ColumnFormatter;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class HTMLTableProfile extends WidgetProfile {

  public abstract HTMLTable createTable(int rows, int columns);

  public void addStyleName(int rows, int columns) {
    HTMLTable HTMLTable = createTable(rows, columns);
    resetTimer();
    for (int row = 0; row < rows; row++) {
      for (int column = 0; column < columns; column++) {
        HTMLTable.getCellFormatter().addStyleName(row, column, "foo");
      }
    }
    timing("addStyleName(" + rows + "," + columns + ")");
  }

  public void setStyleName(int rows, int columns) {
    System.out.println("Hybrid mode");
    HTMLTable HTMLTable = createTable(rows, columns);
    resetTimer();
    for (int row = 0; row < rows; row++) {
      for (int column = 0; column < columns; column++) {
        HTMLTable.getCellFormatter().setStyleName(row, column,
            row + "," + column);
      }
    }
    timing("setStyleName(" + rows + "," + columns + ")");
  }

  public void createTableTiming(int rows, int columns) {
    resetTimer();
    createTable(rows, columns);
    timing("createTable(" + rows + "," + columns + ")");
  }

  public void getStyleName(int rows, int columns) {
    System.out.println("Hybrid mode");
    HTMLTable HTMLTable = createTable(rows, columns);
    resetTimer();
    for (int row = 0; row < rows; row++) {
      for (int column = 0; column < columns; column++) {
        HTMLTable.getCellFormatter().getStyleName(row, column);
      }
    }
    timing("getStyleName(" + rows + "," + columns + ")");
  }

  public void hashMapShare(int rows, int columns) {
    resetTimer();
    HashMap m = new HashMap();
    for (int row = 0; row < rows; row++) {
      for (int column = 0; column < columns; column++) {
        Label label = new Label(column + "i");
        m.put(row + "," + column, label);
      }

    }
    timing("hashMapShare(" + rows + "," + columns + ")");
  }

  public void setHTMLTiming(int rows, int columns) {
    resetTimer();
    HTMLTable HTMLTable = createTable(rows, columns);
    for (int row = 0; row < rows; row++) {
      for (int column = 0; column < columns; column++) {
        HTMLTable.setHTML(row, column, "test");
      }
    }
    timing("setHTML(" + rows + "," + columns + ")");
  }

  public void setTextTiming(int rows, int columns) {
    resetTimer();
    HTMLTable HTMLTable = createTable(rows, columns);
    for (int row = 0; row < rows; row++) {
      for (int column = 0; column < columns; column++) {
        HTMLTable.setText(row, column, "test");
      }
    }
    timing(" setText(" + rows + "," + columns + ")");
  }

  public void setWidgetTiming(int rows, int columns) {
    resetTimer();
    HTMLTable HTMLTable = createTable(rows, columns);
    ArrayList l = new ArrayList();
    for (int row = 0; row < rows; row++) {
      for (int column = 0; column < columns; column++) {
        l.add(new Label(column + "i"));
      }
    }
    timing("Creating label widgets");
    int count = 0;
    resetTimer();

    RootPanel.get().add(HTMLTable);

    for (int row = 0; row < rows; row++) {
      for (int column = 0; column < columns; column++) {
        Label label = (Label) l.get(count++);
        HTMLTable.setWidget(row, column, label);
      }
    }
    timing("setWidgetTiming(" + rows + "," + columns + ")");
  }

  public void columnAddStyleName(int rows, int cols) {
    HTMLTable table = createTable(rows, cols);
    resetTimer();
    ColumnFormatter formatter = table.getColumnFormatter();
    for (int i = 0; i < cols; i++) {
      formatter.addStyleName(i, "fooStyle");
    }
    timing("column.addStyleName(" + rows + ", " + cols + ")");

  }

  public void testTimings() throws Exception {
    timing(20, 50);
    /**
     * timing(10, 10); timing(20, 10); timing(20, 20); timing(40, 20);
     * timing(40, 40); timing(80, 40); timing(80, 80);
     */
    throw new Exception("Finished Profile");
  }

  public void timing(int rows, int columns) {
    setHTMLTiming(rows, columns);
  }

}
