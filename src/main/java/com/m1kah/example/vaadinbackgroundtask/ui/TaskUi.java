package com.m1kah.example.vaadinbackgroundtask.ui;

import com.m1kah.example.vaadinbackgroundtask.task.ProgressDialog;
import com.m1kah.example.vaadinbackgroundtask.task.TaskService;
import com.vaadin.annotations.Push;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.Future;

@Push
@SpringUI
public class TaskUi extends UI {
    private static final Logger logger = LoggerFactory.getLogger(TaskUi.class);
    @Autowired
    private TaskService taskService;
    private TextField resultField;
    private Button button;
    private ProgressDialog progressDialog;

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        initUi();
    }

    private void initUi() {
        resultField = new TextField("Result");
        resultField.setPlaceholder("Click on Run...");
        resultField.setReadOnly(true);
        button = new Button("Run");
        button.addClickListener(this::onButtonClick);
        Label guideLabel = new Label("Click on button to execute background task");
        FormLayout layout = new FormLayout(resultField, button);
        layout.setCaption("Vaadin Background Task Example");
        layout.setMargin(true);
        setContent(layout);
    }

    private void onButtonClick(Button.ClickEvent clickEvent) {
        setResult("Calculating...");
        // This runTask creates important link to current UI and the background task.
        // "this" object in these onTask methods is the UI object that we want
        // to update. We need to have someway to pass UI object to background
        // thread. UI.getCurrent() could be a parameter that is passed to the
        // task as well.
        Future<String> futureResult = taskService.runTask(
                this::onTaskDone,
                this::onTaskCancel,
                this::onTaskProgress);
        progressDialog = new ProgressDialog(futureResult);
        progressDialog.show();
    }

    private void onTaskProgress(double progress) {
        // This thread is being called from background thread. When we
        // write to log then we can see background thread ID. But we
        // cannot update UI without using UI.access method, only UI
        // threads are allowed to do that. So wrap any changes to UI
        // in Runnable that is passed to access method.
        logger.info("onTaskProgress: {}", progress);
        access(() -> progressDialog.setProgress(progress));
    }

    private void onTaskCancel() {
        logger.info("onTaskCancel");
        access(() -> {
            progressDialog.close();
            setResult("Cancelled");
        });
    }

    private void onTaskDone(String result) {
        logger.info("onTaskDone");
        access(() -> {
            progressDialog.close();
            setResult(result);
        });
    }

    private void setResult(String result) {
        resultField.setReadOnly(false);
        resultField.setValue(result);
        resultField.setReadOnly(true);
    }
}
