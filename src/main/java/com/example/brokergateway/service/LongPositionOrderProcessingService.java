package com.example.brokergateway.service;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.NewOCO;
import com.binance.api.client.domain.account.NewOrder;
import com.binance.api.client.domain.account.request.OrderRequest;
import com.example.brokergateway.dto.*;
import com.example.brokergateway.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.example.brokergateway.dto.RequestOrderType.*;

@Service
@Slf4j
public class LongPositionOrderProcessingService implements OrderProcessingService {
    private final BinanceApiRestClient client;
    private final OrderMapper mapper;
    private final Map<RequestOrderType, Consumer<BaseOrderRequest>> handlers = Map.of(
            STOP_LIMIT, request -> processStopLimitOrderRequest((StopLimitOrderRequest) request),
            LIMIT, request -> processLimitOrderRequest((LimitOrderRequest) request),
            OCO, request -> processOcoOrderRequest((OcoOrderRequest) request),
            CANCEL, request -> processCancelOrderRequest((CancelOrderRequest) request)
    );

    public LongPositionOrderProcessingService(@Value(value = "${binance.api.key}") String apiKey,
                                              @Value(value = "${binance.secret.key}") String secretKey,
                                              OrderMapper mapper) {
        this.client = BinanceApiClientFactory.newInstance(apiKey, secretKey).newRestClient();
        this.mapper = mapper;
    }

    @Override
    public void processOrderRequest(BaseOrderRequest orderRequest) {
        wait(orderRequest.getDelay());
        handlers.get(orderRequest.getType()).accept(orderRequest);
    }

    private void processStopLimitOrderRequest(StopLimitOrderRequest orderRequest) {
        NewOrder stopLimitOrder = mapper.toStopLimitOrder(orderRequest);
        log.info("Long: calling Binance api client 'newOrder', process StopLimitOrderRequest, payload: " + stopLimitOrder.toString());
        client.newOrder(stopLimitOrder);
    }

    private void processLimitOrderRequest(LimitOrderRequest orderRequest) {
        if (orderRequest.getQuantity() == null) {
            orderRequest.setQuantity(getQuantity(orderRequest));
        }
        NewOrder limitOrder = mapper.toLimitOrder(orderRequest);
        log.info("Long: calling Binance api client 'newOrder', process LimitOrderRequest, payload: " + limitOrder.toString());
        client.newOrder(limitOrder);
    }

    private void processOcoOrderRequest(OcoOrderRequest orderRequest) {
        if (orderRequest.getQuantity() == null) {
            orderRequest.setQuantity(getQuantity(orderRequest));
        }
        NewOCO newOCO = mapper.toOcoOrder(orderRequest);
        log.info("Long: calling Binance api client 'newOCO', processing OcoOrderRequest, payload: " + newOCO.toString());
        client.newOCO(newOCO);
    }

    private void processCancelOrderRequest(CancelOrderRequest orderRequest) {
        log.info("Long: processing cancel order request");
        log.info("Long: calling Binance api client 'getOpenOrders' for symbol: " + orderRequest.getSymbol());
        client.getOpenOrders(new OrderRequest(orderRequest.getSymbol().replace("/", "")))
                .forEach(order -> {
                    com.binance.api.client.domain.account.request.CancelOrderRequest cancelOrder = mapper.toCancelOrder(order);
                    log.info("Long: calling Binance api client 'cancelOrder', payload: " + cancelOrder.toString());
                    client.cancelOrder(cancelOrder);
                });
    }

    private <T extends BaseOrderRequest> BigDecimal getQuantity(T orderRequest) {
        BigDecimal free = getFree(orderRequest.getSymbol());

        if (orderRequest.getQuantityPercentage() != null) {
            return free.multiply(BigDecimal.valueOf(orderRequest.getQuantityPercentage())).divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP).setScale(5, RoundingMode.HALF_UP);
        }

        return free;
    }

    private BigDecimal getFree(String symbol) {
        log.info("Long: calling Binance api client getAssetBalance for symbol " + symbol);
        return new BigDecimal(client.getAccount().getAssetBalance(getFirstSymbol(symbol)).getFree()).setScale(5, RoundingMode.FLOOR);
    }

    private String getFirstSymbol(String tradingSymbolPair) {
        return tradingSymbolPair.substring(0, tradingSymbolPair.indexOf("/"));
    }
}
