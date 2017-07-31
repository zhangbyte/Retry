package com.retry.scheduletask;

/**
 * Created by zbyte on 17-7-28.
 */
public class RetryTask implements Runnable{

    @Override
    public void run() {
        System.out.println("RetryTask is running ......");
    }
}
