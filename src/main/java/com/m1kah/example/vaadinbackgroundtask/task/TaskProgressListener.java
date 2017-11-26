package com.m1kah.example.vaadinbackgroundtask.task;

@FunctionalInterface
public interface TaskProgressListener {
    void onTaskProgress(double progress);
}
