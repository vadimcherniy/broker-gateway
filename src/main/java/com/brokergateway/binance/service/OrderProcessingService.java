package com.brokergateway.binance.service;

import com.brokergateway.binance.dto.BinanceBaseOrderRequest;

import java.util.concurrent.TimeUnit;

public interface OrderProcessingService {

    void processOrderRequest(BinanceBaseOrderRequest orderRequest);

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
