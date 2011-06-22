/*
 * Copyright 2011 Google Inc.
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
package com.google.gwt.sample.mobilewebapp.client.activity;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.sample.mobilewebapp.client.ClientFactory;
import com.google.gwt.sample.mobilewebapp.client.event.EditingCanceledEvent;
import com.google.gwt.sample.mobilewebapp.client.event.TaskSavedEvent;
import com.google.gwt.sample.mobilewebapp.client.place.TaskEditPlace;
import com.google.gwt.sample.mobilewebapp.client.ui.SoundEffects;
import com.google.gwt.sample.mobilewebapp.shared.TaskProxy;
import com.google.gwt.sample.mobilewebapp.shared.TaskRequest;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.ServerFailure;

import java.util.Set;
import java.util.logging.Logger;

import javax.validation.ConstraintViolation;

/**
 * Activity that presents a task to be edited.
 */
public class TaskEditActivity extends AbstractActivity implements TaskEditView.Presenter,
    TaskReadView.Presenter {

  private static final Logger log = Logger.getLogger(TaskEditActivity.class.getName());
  private final ClientFactory clientFactory;

  /**
   * A boolean indicating whether or not this activity is still active. The user
   * might move to another activity while this one is loading, in which case we
   * do not want to do any more work.
   */
  private boolean isDead = false;

  /**
   * Indicates whether the activity is editing an existing task or creating a
   * new task.
   */
  private boolean isEditing;

  /**
   * The current task being displayed, might not be possible to edit it.
   */
  private TaskProxy readOnlyTask;

  /**
   * The current task being edited, provided by RequestFactory.
   */
  private TaskProxy editTask;

  /**
   * The ID of the current task being edited.
   */
  private final Long taskId;

  /**
   * The request used to persist the modified task.
   */
  private Request<Void> taskPersistRequest;
  private AcceptsOneWidget container;

  /**
   * Construct a new {@link TaskEditActivity}.
   *
   * @param clientFactory the {@link ClientFactory} of shared resources
   * @param place configuration for this activity
   */
  public TaskEditActivity(ClientFactory clientFactory, TaskEditPlace place) {
    this.taskId = place.getTaskId();
    this.readOnlyTask = place.getTask();
    this.clientFactory = clientFactory;
  }

  public void deleteTask() {
    if (isEditing) {
      doDeleteTask();
    } else {
      doCancelTask();
    }
  }

  @Override
  public void editTask() {
    // Load the existing task.
    final TaskEditView editView = clientFactory.getTaskEditView();
  
    if (taskId == null) {
      isEditing = false;
      editView.setEditing(false);
      TaskRequest request = clientFactory.getRequestFactory().taskRequest();
      editTask = request.create(TaskProxy.class);
      editView.getEditorDriver().edit(editTask, request);
    } else {
      editView.setLocked(true);
      clientFactory.getRequestFactory().taskRequest().findTask(this.taskId).fire(
          new Receiver<TaskProxy>() {
            @Override
            public void onSuccess(TaskProxy response) {
              // Early exit if this activity has already been cancelled.
              if (isDead) {
                return;
              }
  
              // Task not found.
              if (response == null) {
                Window.alert("The task with id '" + taskId + "' could not be found."
                    + " Please select a different task from the task list.");
                doCancelTask();
                return;
              }
  
              // Show the task.
              editTask = response;
              editView.getEditorDriver().edit(response,
                  clientFactory.getRequestFactory().taskRequest());
              editView.setLocked(false);
            }
          });
    }
    container.setWidget(editView);
  }

  @Override
  public void onCancel() {
    // Ignore all incoming responses to the requests from this activity.
    isDead = true;
    clientFactory.getTaskEditView().setLocked(false);
  }

  @Override
  public void onStop() {
    // Ignore all incoming responses to the requests from this activity.
    isDead = true;
    clientFactory.getTaskEditView().setLocked(false);
  }

  public void saveTask() {
    // Flush the changes into the editable task.
    TaskRequest context = (TaskRequest) clientFactory.getTaskEditView().getEditorDriver().flush();

    /*
     * Create a persist request the first time we try to save this task. If a
     * request already exists, reuse it.
     */
    if (taskPersistRequest == null) {
      taskPersistRequest = context.persist().using(editTask);
    }

    // Fire the request.
    taskPersistRequest.fire(new Receiver<Void>() {
      @Override
      public void onConstraintViolation(Set<ConstraintViolation<?>> violations) {
        handleConstraintViolations(violations);
      }

      @Override
      public void onSuccess(Void response) {
        // Notify the user that the task was updated.
        TaskEditActivity.this.notify("Task Saved");

        // Return to the task list.
        clientFactory.getEventBus().fireEvent(new TaskSavedEvent());
      }
    });
  }

  public void start(AcceptsOneWidget container, EventBus eventBus) {
    this.container = container;
    // Prefetch the sounds used in this activity.
    SoundEffects.get().prefetchError();

    // Hide the 'add' button in the shell.
    clientFactory.getShell().setAddButtonHandler(null);

    // Set the presenter on the views.
    final TaskEditView editView = clientFactory.getTaskEditView();
    editView.setPresenter(this);
    editView.setNameViolation(null);

    final TaskReadView readView = clientFactory.getTaskReadView();
    readView.setPresenter(this);

    if (taskId == null) {
      editTask();
      return;
    } else {
      // Lock the display until the task is loaded.
      isEditing = true;
      editView.setEditing(true);

      // Try to load the task from local storage.
      if (readOnlyTask == null) {
        readOnlyTask = clientFactory.getTaskProxyLocalStorage().getTask(taskId);
      }

      if (readOnlyTask == null) {
        // Load the existing task.
        clientFactory.getRequestFactory().taskRequest().findTask(this.taskId).fire(
            new Receiver<TaskProxy>() {
              @Override
              public void onSuccess(TaskProxy response) {
                // Early exit if this activity has already been cancelled.
                if (isDead) {
                  return;
                }

                // Task not found.
                if (response == null) {
                  Window.alert("The task with id '" + taskId + "' could not be found."
                      + " Please select a different task from the task list.");
                  doCancelTask();
                  return;
                }

                // Show the task.
                readOnlyTask = response;
                readView.getEditorDriver().edit(response);
              }
            });
      } else {
        // Use the task that was passed with the place.
        readView.getEditorDriver().edit(readOnlyTask);
      }
    }

    // Display the view.
    container.setWidget(readView);
  }

  /**
   * Cancel the current task.
   */
  private void doCancelTask() {
    clientFactory.getEventBus().fireEvent(new EditingCanceledEvent());
  }

  /**
   * Delete the current task.
   */
  private void doDeleteTask() {
    if (editTask == null) {
      return;
    }

    // Delete the task in the data store.
    final TaskProxy toDelete = this.editTask;
    clientFactory.getRequestFactory().taskRequest().remove().using(toDelete).fire(
        new Receiver<Void>() {
          @Override
          public void onFailure(ServerFailure error) {
            Window.alert("An error occurred on the server while deleting this task: \"."
                + error.getMessage() + "\".");
          }

          @Override
          public void onSuccess(Void response) {
            onTaskDeleted();
          }
        });
  }

  /**
   * Handle constraint violations.
   */
  private void handleConstraintViolations(Set<ConstraintViolation<?>> violations) {
    // Display the violations.
    clientFactory.getTaskEditView().getEditorDriver().setConstraintViolations(violations);

    // Play a sound.
    SoundEffects.get().playError();
  }

  /**
   * Notify the user of a message.
   * 
   * @param message the message to display
   */
  private void notify(String message) {
    // TODO Add notification pop-up
    log.fine("Tell the user: " + message);
  }

  /**
   * Called when a task has been successfully deleted.
   */
  private void onTaskDeleted() {
    // Notify the user that the task was deleted.
    notify("Task Deleted");

    // Return to the task list.
    clientFactory.getEventBus().fireEvent(new TaskSavedEvent());
  }
}
