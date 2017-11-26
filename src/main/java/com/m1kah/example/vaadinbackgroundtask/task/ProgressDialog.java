package com.m1kah.example.vaadinbackgroundtask.task;

import com.vaadin.ui.*;

import java.util.concurrent.Future;

public class ProgressDialog extends Window {
    private final Future<String> futureResult;
    private ProgressBar progressBar;

    public ProgressDialog(Future<String> futureResult) {
        this.futureResult = futureResult;
        initWindow();
        initUi();
    }

    public void show() {
        UI.getCurrent().addWindow(this);
        center();
    }

    private void initWindow() {
        setClosable(false);
        setWidth("400px");
        setHeight("100px");
        setDraggable(false);
        setResizable(false);
        setModal(true);
    }

    private void initUi() {
        progressBar = new ProgressBar();
        progressBar.setWidth("360px");
        Button button = new Button("Cancel");
        button.addClickListener(this::onCancelClick);
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSpacing(true);
        layout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        layout.addComponent(progressBar);
        layout.addComponent(button);
        layout.setSpacing(true);
        layout.setWidth("100%");
        setContent(layout);
    }

    private void onCancelClick(Button.ClickEvent clickEvent) {
        // This method is called from Vaadin UI thread. We will signal
        // background task thread to stop.
        futureResult.cancel(true);
    }

    public void setProgress(double progress) {
        progressBar.setValue((float) progress);
    }
}
