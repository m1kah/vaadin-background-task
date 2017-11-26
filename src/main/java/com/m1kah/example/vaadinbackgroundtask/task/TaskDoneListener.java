package com.m1kah.example.vaadinbackgroundtask.task;

@FunctionalInterface
public interface TaskDoneListener {
    void onTaskDone(String result);
}