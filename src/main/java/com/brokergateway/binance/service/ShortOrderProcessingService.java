package com.brokergateway.binance.service;

import com.binance.api.client.domain.account.MarginNewOrder;
import com.binance.api.client.domain.account.request.OrderRequest;
import com.brokergateway.binance.client.CustomBinanceApiMarginRestClient;
import com.brokergateway.binance.client.domain.NewMarginOCO;
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
public class ShortOrderProcessingService implements OrderProcessingService {
    private final CustomBinanceApiMarginRestClient marginRestClient;
    private final OrderMapper mapper;
    private final Map<RequestOrderType, Consumer<BinanceBaseOrderRequest>> handlers = Map.of(
            STOP_LIMIT, request -> processStopLimitOrderRequest((BinanceStopLimitOrderRequest) request),
            OCO, request -> processOcoOrderRequest((BinanceOcoOrderRequest) request),
            LIMIT, request -> processLimitOrderRequest((BinanceLimitOrderRequest) request),
            CANCEL, request -> processCancelOrderRequest((BinanceCancelOrderRequest) request)
    );

    public ShortOrderProcessingService(@Value(value = "${binance.api.key}") String apiKey,
                                       @Value(value = "${binance.secret.key}") String secretKey,
                                       OrderMapper mapper) {
        this.marginRestClient = new CustomBinanceApiMarginRestClient(apiKey, secretKey);
        this.mapper = mapper;
    }

    @Override
    public void processOrderRequest(BinanceBaseOrderRequest orderRequest) {
        wait(orderRequest.getDelay());
        handlers.get(orderRequest.getType()).accept(orderRequest);
    }

    private void processStopLimitOrderRequest(BinanceStopLimitOrderRequest orderRequest) {
        MarginNewOrder marginNewOrder = mapper.toMarginStopLimitOrder(orderRequest);
        log.info("Short: calling Binance margin api client 'newOrder', processing MarginStopLimitOrderRequest, payload: " + marginNewOrder.toString());
        marginRestClient.newOrder(marginNewOrder);
    }

    private void processOcoOrderRequest(BinanceOcoOrderRequest orderRequest) {
        String borrowed = getDebt(orderRequest).toPlainString();
        NewMarginOCO marginOCO = mapper.toNewMarginOCO(orderRequest);
        marginOCO.setQuantity(borrowed);
        log.info("Short: calling Binance margin api client 'newMarginOcoOrder', processing MarginOcoOrderRequest, payload: " + marginOCO);
        marginRestClient.newMarginOcoOrder(marginOCO);
    }

    private void processLimitOrderRequest(BinanceLimitOrderRequest orderRequest) {
        if (orderRequest.getQuantity() == null) {
            orderRequest.setQuantity(getQuantity(orderRequest));
        }
        MarginNewOrder marginNewOrder = mapper.toMarginLimitOrder(orderRequest);
        log.info("Short: calling Binance margin api client 'newOrder', processing LimitOrderRequest, payload: " + marginNewOrder.toString());
        marginRestClient.newOrder(marginNewOrder);
    }

    private void processCancelOrderRequest(BinanceCancelOrderRequest orderRequest) {
        log.info("Short: processing cancel margin order request");
        log.info("Short: calling Binance margin api client 'getOpenOrders' for symbol " + orderRequest.getSymbol());
        marginRestClient.getOpenOrders(new OrderRequest(orderRequest.getSymbol().replace("/", "")))
                .forEach(openedOrder -> {
                    com.binance.api.client.domain.account.request.CancelOrderRequest cancelOrder = mapper.toCancelOrder(openedOrder);
                    log.info("Short: calling Binance margin api client 'cancelOrder', payload: " + cancelOrder.toString());
                    marginRestClient.cancelOrder(cancelOrder);
                });
    }

    private String getFirstSymbol(String tradingSymbolPair) {
        return tradingSymbolPair.substring(0, tradingSymbolPair.indexOf("/"));
    }

    private <T extends BinanceBaseOrderRequest> BigDecimal getQuantity(T orderRequest) {
        BigDecimal debt = getDebt(orderRequest);

        if (orderRequest.getQuantityPercentage() != null) {
            return debt.multiply(BigDecimal.valueOf(orderRequest.getQuantityPercentage())).divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP).setScale(5, RoundingMode.HALF_UP);
        }

        return debt;
    }

    private BigDecimal getDebt(BinanceBaseOrderRequest orderRequest) {
        return new BigDecimal(marginRestClient.getAccount().getAssetBalance(getFirstSymbol(orderRequest.getSymbol())).getBorrowed()).setScale(5, RoundingMode.HALF_UP);
    }

}
