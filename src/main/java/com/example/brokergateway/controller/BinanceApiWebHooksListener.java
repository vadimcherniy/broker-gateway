package com.example.brokergateway.controller;

import com.example.brokergateway.dto.BaseOrderRequest;
import com.example.brokergateway.service.LongPositionOrderProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BinanceApiWebHooksListener {
    private final LongPositionOrderProcessingService service;

    @GetMapping("/test")
    public String test() {
        return "Hello world!";
    }

    @PostMapping("/binance/order")
    public void handleTradingViewAlert(@RequestBody BaseOrderRequest orderRequest) {
        service.processOrderRequest(orderRequest);
    }
}
