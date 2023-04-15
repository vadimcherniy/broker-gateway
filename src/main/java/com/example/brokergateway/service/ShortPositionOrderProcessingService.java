package com.example.brokergateway.service;

import com.binance.api.client.domain.account.MarginNewOrder;
import com.binance.api.client.domain.account.request.OrderRequest;
import com.example.brokergateway.client.CustomBinanceApiMarginRestClient;
import com.example.brokergateway.client.domain.NewMarginOCO;
import com.example.brokergateway.dto.*;
import com.example.brokergateway.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.function.Consumer;

import static com.example.brokergateway.dto.RequestOrderType.*;

@Service
@Slf4j
public class ShortPositionOrderProcessingService implements OrderProcessingService {
    private final CustomBinanceApiMarginRestClient marginRestClient;
    private final OrderMapper mapper;
    private final Map<RequestOrderType, Consumer<BaseOrderRequest>> handlers = Map.of(
            STOP_LIMIT, request -> processStopLimitOrderRequest((StopLimitOrderRequest) request),
            OCO, request -> processOcoOrderRequest((OcoOrderRequest) request),
            LIMIT, request -> processLimitOrderRequest((LimitOrderRequest) request),
            CANCEL, request -> processCancelOrderRequest((CancelOrderRequest) request)
    );

    public ShortPositionOrderProcessingService(@Value(value = "${binance.api.key}") String apiKey,
                                               @Value(value = "${binance.secret.key}") String secretKey,
                                               OrderMapper mapper) {
        this.marginRestClient = new CustomBinanceApiMarginRestClient(apiKey, secretKey);
        this.mapper = mapper;
    }

    @Override
    public void processOrderRequest(BaseOrderRequest orderRequest) {
        wait(orderRequest.getDelay());
        handlers.get(orderRequest.getType()).accept(orderRequest);
    }

    private void processStopLimitOrderRequest(StopLimitOrderRequest orderRequest) {
        MarginNewOrder marginNewOrder = mapper.toMarginStopLimitOrder(orderRequest);
        log.info("Short: calling Binance margin api client 'newOrder', processing MarginStopLimitOrderRequest, payload: " + marginNewOrder.toString());
        marginRestClient.newOrder(marginNewOrder);
    }

    private void processOcoOrderRequest(OcoOrderRequest orderRequest) {
        String borrowed = getDebt(orderRequest).toPlainString();
        NewMarginOCO marginOCO = mapper.toNewMarginOCO(orderRequest);
        marginOCO.setQuantity(borrowed);
        log.info("Short: calling Binance margin api client 'newMarginOcoOrder', processing MarginOcoOrderRequest, payload: " + marginOCO);
        marginRestClient.newMarginOcoOrder(marginOCO);
    }

    private void processLimitOrderRequest(LimitOrderRequest orderRequest) {
        if (orderRequest.getQuantity() == null) {
            orderRequest.setQuantity(getQuantity(orderRequest));
        }
        MarginNewOrder marginNewOrder = mapper.toMarginLimitOrder(orderRequest);
        log.info("Short: calling Binance margin api client 'newOrder', processing LimitOrderRequest, payload: " + marginNewOrder.toString());
        marginRestClient.newOrder(marginNewOrder);
    }

    private void processCancelOrderRequest(CancelOrderRequest orderRequest) {
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

    private <T extends BaseOrderRequest> BigDecimal getQuantity(T orderRequest) {
        BigDecimal debt = getDebt(orderRequest);

        if (orderRequest.getQuantityPercentage() != null) {
            return debt.multiply(BigDecimal.valueOf(orderRequest.getQuantityPercentage())).divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP).setScale(5, RoundingMode.HALF_UP);
        }

        return debt;
    }

    private BigDecimal getDebt(BaseOrderRequest orderRequest) {
        return new BigDecimal(marginRestClient.getAccount().getAssetBalance(getFirstSymbol(orderRequest.getSymbol())).getBorrowed()).setScale(5, RoundingMode.HALF_UP);
    }

}
