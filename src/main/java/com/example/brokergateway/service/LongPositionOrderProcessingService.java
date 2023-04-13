package com.example.brokergateway.service;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.OrderSide;
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
public class LongPositionOrderProcessingService {
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

    public void processOrderRequest(BaseOrderRequest orderRequest) {
        wait(orderRequest.getDelay());
        handlers.get(orderRequest.getType()).accept(orderRequest);
    }

    private void processStopLimitOrderRequest(StopLimitOrderRequest orderRequest) {
        log.info("Calling Binance api rest client newOrder: process StopLimitOrder request");
        client.newOrder(mapper.toStopLimitOrder(orderRequest));
    }

    private void processLimitOrderRequest(LimitOrderRequest orderRequest) {
        if (orderRequest.getSide() == OrderSide.SELL && orderRequest.getQuantity() == null) {
            orderRequest.setQuantity(getQuantity(orderRequest));
        }
        log.info("Calling Binance api rest client newOrder: process LimitOrder request");
        client.newOrder(mapper.toNewOrder(orderRequest));
    }

    private void processOcoOrderRequest(OcoOrderRequest orderRequest) {
        if (orderRequest.getQuantity() == null) {
            orderRequest.setQuantity(getQuantity(orderRequest));
        }
        log.info("Calling Binance api rest client newOCO: process OcoOrder request");
        client.newOCO(mapper.toOcoOrder(orderRequest));
    }

    private void processCancelOrderRequest(CancelOrderRequest orderRequest) {
        log.info("Process cancel order request");
        log.info("Calling Binance api rest client: getOpenOrders");
        client.getOpenOrders(new OrderRequest(orderRequest.getSymbol().replace("/", ""))).stream()
                .filter(order -> {
                    if (orderRequest.getSide() != null) {
                        return order.getSide() == orderRequest.getSide();
                    }
                    return true;
                })
                .forEach(order -> {
                    log.info("Calling Binance api rest client: cancelOrder");
                    client.cancelOrder(mapper.toCancelOrder(order));
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
        log.info("Calling Binance api rest client: getAccount");
        return new BigDecimal(client.getAccount().getAssetBalance(getFirstSymbol(symbol)).getFree()).setScale(5, RoundingMode.FLOOR);
    }

    private String getFirstSymbol(String tradingSymbolPair) {
        return tradingSymbolPair.substring(0, tradingSymbolPair.indexOf("/"));
    }

    private void wait(Integer seconds) {
        if (seconds != null) {
            try {
                TimeUnit.SECONDS.sleep(seconds);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
