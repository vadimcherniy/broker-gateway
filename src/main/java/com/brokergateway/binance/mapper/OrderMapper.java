package com.brokergateway.binance.mapper;

import com.binance.api.client.domain.account.*;
import com.binance.api.client.domain.account.request.CancelOrderRequest;
import com.brokergateway.binance.client.domain.NewMarginOCO;
import com.brokergateway.binance.dto.BinanceLimitOrderRequest;
import com.brokergateway.binance.dto.BinanceOcoOrderRequest;
import com.brokergateway.binance.dto.BinanceStopLimitOrderRequest;
import org.springframework.stereotype.Component;

import static com.binance.api.client.domain.OrderSide.BUY;
import static com.binance.api.client.domain.OrderSide.SELL;
import static com.binance.api.client.domain.OrderType.LIMIT;
import static com.binance.api.client.domain.OrderType.STOP_LOSS_LIMIT;
import static com.binance.api.client.domain.TimeInForce.FOK;
import static com.binance.api.client.domain.TimeInForce.GTC;
import static com.binance.api.client.domain.account.SideEffectType.AUTO_REPAY;

@Component
public class OrderMapper {

    public NewOrder toLimitOrder(BinanceLimitOrderRequest request) {
        return new NewOrder(getSymbol(request.getSymbol()), SELL, LIMIT, GTC, request.getQuantity(), request.getPrice());
    }

    public NewOrder toStopLimitOrder(BinanceStopLimitOrderRequest request) {
        NewOrder order = new NewOrder(getSymbol(request.getSymbol()), BUY, STOP_LOSS_LIMIT, GTC, request.getQuantity(), request.getPrice());
        order.stopPrice(request.getTriggeredPrice());
        return order;
    }

    public NewOCO toOcoOrder(BinanceOcoOrderRequest request) {
        NewOCO ocoOrder = new NewOCO(getSymbol(request.getSymbol()), SELL, request.getQuantity(), request.getTakeProfitPrice(), request.getStopLossPrice());
        ocoOrder.setStopLimitPrice(request.getPrice());
        ocoOrder.setStopLimitTimeInForce(FOK);
        return ocoOrder;
    }

    public MarginNewOrder toMarginStopLimitOrder(BinanceStopLimitOrderRequest request) {
        MarginNewOrder marginNewOrder = new MarginNewOrder(getSymbol(request.getSymbol()), SELL, STOP_LOSS_LIMIT, GTC, request.getQuantity(), request.getPrice());
        marginNewOrder.stopPrice(request.getTriggeredPrice());
        marginNewOrder.sideEffectType(SideEffectType.MARGIN_BUY);
        return marginNewOrder;
    }

    public NewMarginOCO toNewMarginOCO(BinanceOcoOrderRequest request) {
        NewMarginOCO marginOCO = new NewMarginOCO(getSymbol(request.getSymbol()), BUY, request.getQuantity(), request.getTakeProfitPrice(), request.getStopLossPrice());
        marginOCO.setStopLimitPrice(request.getPrice());
        marginOCO.setSideEffectType(AUTO_REPAY);
        marginOCO.setStopLimitTimeInForce(FOK);
        return marginOCO;
    }

    public MarginNewOrder toMarginLimitOrder(BinanceLimitOrderRequest request) {
        MarginNewOrder marginNewOrder = new MarginNewOrder(getSymbol(request.getSymbol()), BUY, LIMIT, GTC, request.getQuantity(), request.getPrice());
        marginNewOrder.sideEffectType(AUTO_REPAY);
        return marginNewOrder;
    }

    public CancelOrderRequest toCancelOrder(Order order) {
        return new CancelOrderRequest(order.getSymbol(), order.getOrderId());
    }

    private String getSymbol(String tradingPair) {
        return tradingPair.replace("/", "");
    }
}
