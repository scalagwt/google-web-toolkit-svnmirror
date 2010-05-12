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
package com.google.gwt.sample.expenses.gwt.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;

import java.util.List;

/**
 * Entry point to create database entries for the Expenses app.
 */
public class LoadExpensesDB implements EntryPoint {

  private final DataGenerationServiceAsync dataService = GWT.create(DataGenerationService.class);

  private Label statusLabel;
  private Label numEmployeesLabel;
  private Label numReportsLabel;
  private Label numExpensesLabel;
  private Button generateButton;
  private Button deleteButton;
  private TextBox amountTextBox;

  public void onModuleLoad() {
    statusLabel = new Label("");
    numEmployeesLabel = new Label("-- Employees");
    numReportsLabel = new Label("-- Reports");
    numExpensesLabel = new Label("-- Expenses");
    
    generateButton = new Button("Generate Data");
    deleteButton = new Button("Delete everything");
    amountTextBox = new TextBox();

    generateButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        generateButton.setEnabled(false);
        generateData(Integer.parseInt(amountTextBox.getText()));
      }
    });
    
    deleteButton.addClickHandler(new ClickHandler() {      
      public void onClick(ClickEvent event) {
        deleteButton.setEnabled(false);
        deleteData();
      }
    });

    RootPanel root = RootPanel.get();
    root.add(generateButton);
    root.add(amountTextBox);
    root.add(statusLabel);
    root.add(numEmployeesLabel);
    root.add(numReportsLabel);
    root.add(numExpensesLabel);
    
    root.add(new HTML("<br>"));
    root.add(new HTML("<br>"));
    root.add(new HTML("<br>"));
    root.add(new HTML("<br>"));
    root.add(new HTML("<br>"));
    root.add(new HTML("<br>"));

    // This button deletes the entire data store -- be careful
    root.add(deleteButton);

    updateCounts();
  }

  private void deleteData() {
    dataService.delete(new AsyncCallback<Void>() {
      public void onFailure(Throwable caught) {
        statusLabel.setText("Deletion failed");
        deleteButton.setEnabled(true);
        updateCounts();
      }

      public void onSuccess(Void result) {
        statusLabel.setText("Deletion succeeded");
        deleteButton.setEnabled(true);
        updateCounts();
      }
    });
  }
  
  private void updateCounts() {
    dataService.getCounts(new AsyncCallback<List<Integer>>() {
      public void onFailure(Throwable caught) {
        numEmployeesLabel.setText("? Employees");
        numReportsLabel.setText("? Reports");
        numExpensesLabel.setText("? Expenses");
      }

      public void onSuccess(List<Integer> result) {
        numEmployeesLabel.setText("" + result.get(0) + " Employees");
        numReportsLabel.setText("" + result.get(1) + " Reports");
        numExpensesLabel.setText("" + result.get(2) + " Expenses");
      }
    });
  }

  private void generateData(int amount) {
    dataService.generate(amount, new AsyncCallback<Void>() {
      public void onFailure(Throwable caught) {
        statusLabel.setText("Data generation failed");
        generateButton.setEnabled(true);
        updateCounts();
      }

      public void onSuccess(Void result) {
        statusLabel.setText("Data generation succeeded");
        generateButton.setEnabled(true);
        updateCounts();
      }
    });
  }
}
