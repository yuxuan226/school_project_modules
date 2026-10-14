package com.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.Executor;

@SpringBootTest
public class AsyncTest {

    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    @Test
    public void testThreadPool() throws InterruptedException {
        for (int i = 0; i < 20; i++) {
            int taskNum = i;
            taskExecutor.execute(() -> {
                try {
                    System.out.println(Thread.currentThread().getName() + " 执行任务 " + taskNum);
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }

        Thread.sleep(10000);
    }
}
