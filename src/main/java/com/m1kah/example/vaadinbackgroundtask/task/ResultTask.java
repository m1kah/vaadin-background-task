package com.m1kah.example.vaadinbackgroundtask.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.concurrent.Callable;

public class ResultTask implements Callable<String> {
    private static final Logger logger = LoggerFactory.getLogger(ResultTask.class);
    private static final int taskDuration = 5;
    private final TaskDoneListener taskDoneListener;
    private final TaskCancelListener taskCancelListener;
    private final TaskProgressListener taskProgressListener;
    private int timeout = taskDuration;

    ResultTask(TaskDoneListener taskDoneListener,
               TaskCancelListener taskCancelListener,
               TaskProgressListener taskProgressListener) {
        this.taskDoneListener = taskDoneListener;
        this.taskCancelListener = taskCancelListener;
        this.taskProgressListener = taskProgressListener;
    }

    @Override
    public String call() {
        timeout = (int) (Math.random() * 2 * taskDuration);
        logger.info("Background task started working. Timeout after {} s", timeout);
        simulateLongAndSlowCalculation();
        // Calculation is now done. It has either got cancelled in which case
        // thread has been interrupted or it has completed. Check status from
        // thread interrupted status.
        if (Thread.currentThread().isInterrupted()) {
            // We got cancelled. Notify Vaadin UI from current thread. Current
            // thread in this class is always background task.
            taskCancelListener.onTaskCancel();
            return null;
        } else {
            // We completed calculation. Notify Vaadin UI from current thread.
            BigDecimal result = new BigDecimal(Math.random() * 1000);
            result = result.setScale(2, BigDecimal.ROUND_HALF_UP);
            String resultText = result.toPlainString();
            taskDoneListener.onTaskDone(resultText);
            return resultText;
        }
    }

    private void simulateLongAndSlowCalculation() {
        int totalWait = taskDuration;
        for (int second = 0; second < totalWait; second++) {
            // We have a time limit for this task. Let's see if we can still
            // continue the task.
            if (second > timeout) {
                logger.info("Timeout reached");
                Thread.currentThread().interrupt();
                return;
            }
            // Some other thread has requested this task to be interrupted.
            // We are using interruption to notify us about task
            // cancellation.
            if (Thread.currentThread().isInterrupted()) {
                logger.info("Task got interrupted in loop.");
                return;
            }
            try {
                Thread.sleep(second * 1000);
                notifyProgressListener(second / (double) totalWait);
            } catch (InterruptedException e) {
                // We got interruption request while we were waiting on
                // some blocking method call. Now we were just sleeping
                // but we could have waited on data to be read from
                // somewhere.
                logger.info("Task got interrupted while waiting on blocking call.");
                // We do not own current thread so let's not consume
                // interruption. Let's restore interruption status
                // and let the executor pool take care of the
                // thread.
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void notifyProgressListener(double progress) {
        taskProgressListener.onTaskProgress(progress);
    }

    public void cancel() {
        Thread.currentThread().interrupt();
    }
}
