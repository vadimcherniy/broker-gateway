package com.example.brokergateway.service;

import com.example.brokergateway.dto.BaseOrderRequest;

import java.util.concurrent.TimeUnit;

public interface OrderProcessingService {

    void processOrderRequest(BaseOrderRequest orderRequest);

    default void wait(Integer seconds) {
        if (seconds != null) {
            try {
                TimeUnit.SECONDS.sleep(seconds);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
