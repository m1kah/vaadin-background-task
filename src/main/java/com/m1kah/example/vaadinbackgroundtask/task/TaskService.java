package com.m1kah.example.vaadinbackgroundtask.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class TaskService {
    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);
    private static final ExecutorService executorService = Executors.newFixedThreadPool(4);

    public void shutdown() {
        logger.info("Shutdown task service");
        executorService.shutdown();
    }

    public Future<String> runTask(TaskDoneListener taskDoneListener,
                              TaskCancelListener taskCancelListener,
                              TaskProgressListener taskProgressListener) {
        ResultTask task = new ResultTask(
                taskDoneListener,
                taskCancelListener,
                taskProgressListener);
        Future<String> futureResult = executorService.submit(task);
        logThreadPoolStatus();
        return futureResult;
    }

    private void logThreadPoolStatus() {
        if (executorService instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executorService;
            logger.info("activeCount: {}, poolSize: {}",
                    threadPoolExecutor.getActiveCount(),
                    threadPoolExecutor.getPoolSize());
        }
    }
}
