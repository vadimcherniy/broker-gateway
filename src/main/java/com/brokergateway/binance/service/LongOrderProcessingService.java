package com.brokergateway.binance.service;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.NewOCO;
import com.binance.api.client.domain.account.NewOrder;
import com.binance.api.client.domain.account.request.OrderRequest;
import com.binance.api.client.exception.BinanceApiException;
import com.brokergateway.binance.dto.*;
import com.brokergateway.binance.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.function.Consumer;

import static com.brokergateway.binance.dto.RequestOrderType.*;

@Service
@Slf4j
public class LongOrderProcessingService implements OrderProcessingService {
    private static final int NON_EXISTENT_ORDER_ERROR_CODE = -2011;
    private final BinanceApiRestClient client;
    private final OrderMapper mapper;
    private final Map<RequestOrderType, Consumer<BinanceBaseOrderRequest>> handlers = Map.of(
            STOP_LIMIT, request -> processStopLimitOrderRequest((BinanceStopLimitOrderRequest) request),
            LIMIT, request -> processLimitOrderRequest((BinanceLimitOrderRequest) request),
            OCO, request -> processOcoOrderRequest((BinanceOcoOrderRequest) request),
            CANCEL, request -> processCancelOrderRequest((BinanceCancelOrderRequest) request)
    );

    public LongOrderProcessingService(@Value(value = "${binance.api.key}") String apiKey,
                                      @Value(value = "${binance.secret.key}") String secretKey,
                                      OrderMapper mapper) {
        this.client = BinanceApiClientFactory.newInstance(apiKey, secretKey).newRestClient();
        this.mapper = mapper;
    }

    @Override
    public void processOrderRequest(BinanceBaseOrderRequest orderRequest) {
        wait(orderRequest.getDelay());
        handlers.get(orderRequest.getType()).accept(orderRequest);
    }

    private void processStopLimitOrderRequest(BinanceStopLimitOrderRequest orderRequest) {
        NewOrder stopLimitOrder = mapper.toStopLimitOrder(orderRequest);
        log.info("Long: calling Binance api client 'newOrder', process StopLimitOrderRequest, payload: " + stopLimitOrder.toString());
        client.newOrder(stopLimitOrder);
    }

    private void processLimitOrderRequest(BinanceLimitOrderRequest orderRequest) {
        if (orderRequest.getQuantity() == null) {
            orderRequest.setQuantity(getFreeQuantity(orderRequest));
        }
        NewOrder limitOrder = mapper.toLimitOrder(orderRequest);
        log.info("Long: calling Binance api client 'newOrder', process LimitOrderRequest, payload: " + limitOrder.toString());
        client.newOrder(limitOrder);
    }

    private void processOcoOrderRequest(BinanceOcoOrderRequest orderRequest) {
        if (orderRequest.getQuantity() == null) {
            orderRequest.setQuantity(getFreeQuantity(orderRequest));
        }
        NewOCO newOCO = mapper.toOcoOrder(orderRequest);
        log.info("Long: calling Binance api client 'newOCO', processing OcoOrderRequest, payload: " + newOCO.toString());
        client.newOCO(newOCO);
    }

    private void processCancelOrderRequest(BinanceCancelOrderRequest orderRequest) {
        log.info("Long: processing cancel order request");
        log.info("Long: calling Binance api client 'getOpenOrders' for symbol: " + orderRequest.getSymbol());
        client.getOpenOrders(new OrderRequest(orderRequest.getSymbol().replace("/", "")))
                .forEach(order -> {
                    com.binance.api.client.domain.account.request.CancelOrderRequest cancelOrder = mapper.toCancelOrder(order);
                    log.info("Long: calling Binance api client 'cancelOrder', payload: " + cancelOrder.toString());
                    try {
                        client.cancelOrder(cancelOrder);
                    } catch (BinanceApiException e) {
                        if (e.getError().getCode() != NON_EXISTENT_ORDER_ERROR_CODE) {
                            log.error(e.getError().toString());
                        }
                    }
                });
    }

    private <T extends BinanceBaseOrderRequest> BigDecimal getFreeQuantity(T orderRequest) {
        BigDecimal free = getFreeAsset(orderRequest.getSymbol());

        if (orderRequest.getQuantityPercentage() != null) {
            return free.multiply(BigDecimal.valueOf(orderRequest.getQuantityPercentage())).divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP).setScale(5, RoundingMode.HALF_UP);
        }

        return free;
    }

    private BigDecimal getFreeAsset(String symbol) {
        log.info("Long: calling Binance api client get free asset for symbol " + symbol);
        return new BigDecimal(client.getAccount().getAssetBalance(getFirstSymbol(symbol)).getFree()).setScale(5, RoundingMode.FLOOR);
    }

    private String getFirstSymbol(String tradingSymbolPair) {
        return tradingSymbolPair.substring(0, tradingSymbolPair.indexOf("/"));
    }
}
