package com.brokergateway;

import com.brokergateway.binance.dto.BinanceBaseOrderRequest;
import com.brokergateway.binance.factory.BinanceOrderProcessingFactory;
import com.brokergateway.ib.dto.IBBaseOrderRequest;
import com.brokergateway.ib.service.IBOrderProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TradingViewAlertsListener {
    private final BinanceOrderProcessingFactory binanceOrderProcessingFactory;
    private final IBOrderProcessingService ibOrderProcessingService;

    @GetMapping("/test")
    public String test() {
        return "Keep calm, it works";
    }

    @PostMapping("/binance/order")
    public void handleBinanceAlert(@RequestBody BinanceBaseOrderRequest orderRequest) {
        binanceOrderProcessingFactory.getOrderProcessingService(orderRequest.getPositionType()).processOrderRequest(orderRequest);
    }

    @PostMapping("/interactive-brokers/order")
    public void handleInteractiveBrokersAlert(@RequestBody IBBaseOrderRequest orderRequest) {
        ibOrderProcessingService.processOrderRequest(orderRequest);
    }
}
