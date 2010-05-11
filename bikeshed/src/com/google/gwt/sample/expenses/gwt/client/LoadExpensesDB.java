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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Entry point to create database entries for the Expenses app.
 */
public class LoadExpensesDB implements EntryPoint {

  private final DataGenerationServiceAsync dataService = GWT.create(DataGenerationService.class);

  private Label generateLabel;
  private Button generateButton;
  private Button deleteButton;
  private TextBox amountTextBox;

  public void onModuleLoad() {
    generateLabel = new Label("-- Expense Reports");
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
        deleteData();
      }
    });

    RootPanel root = RootPanel.get();
    root.add(generateButton);
    root.add(amountTextBox);
    root.add(generateLabel);
    // root.add(deleteButton);

    dataService.getNumReports(new AsyncCallback<Integer>() {
      public void onFailure(Throwable caught) {
      }

      public void onSuccess(Integer result) {
        generateLabel.setText("" + result + " Expense Reports");
      }
    });
  }

  private void deleteData() {
    dataService.delete(new AsyncCallback<Void>() {
      public void onFailure(Throwable caught) {
        generateLabel.setText("Deletion failed");
      }

      public void onSuccess(Void result) {
        generateLabel.setText("Deletion succeeded");
      }
    });
  }

  private void generateData(int amount) {
    dataService.generate(amount, new AsyncCallback<Integer>() {
      public void onFailure(Throwable caught) {
        generateButton.setEnabled(true);
        generateLabel.setText("Data generation failed");
      }

      public void onSuccess(Integer result) {
        generateLabel.setText("" + result + " Expense Reports");
        generateButton.setEnabled(true);
      }
    });
  }
}
