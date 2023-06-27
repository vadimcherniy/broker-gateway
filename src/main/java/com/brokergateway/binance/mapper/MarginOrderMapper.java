package com.brokergateway.binance.mapper;

import com.binance.api.client.domain.account.MarginNewOrder;
import com.binance.api.client.domain.account.Order;
import com.binance.api.client.domain.account.SideEffectType;
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
public class MarginOrderMapper {

    public MarginNewOrder toMarginStopLimitOrder(BinanceStopLimitOrderRequest request) {
        MarginNewOrder marginNewOrder = new MarginNewOrder(getSymbol(request.getSymbol()), BUY, STOP_LOSS_LIMIT, GTC, request.getQuantity(), request.getPrice());
        marginNewOrder.stopPrice(request.getTriggeredPrice());
        marginNewOrder.sideEffectType(SideEffectType.MARGIN_BUY);
        return marginNewOrder;
    }

    public NewMarginOCO toNewMarginOCO(BinanceOcoOrderRequest request) {
        NewMarginOCO marginOCO = new NewMarginOCO(getSymbol(request.getSymbol()), SELL, request.getQuantity(), request.getTakeProfitPrice(), request.getStopLossPrice());
        marginOCO.setStopLimitPrice(request.getPrice());
        marginOCO.setStopLimitTimeInForce(FOK);
        marginOCO.setSideEffectType(AUTO_REPAY);
        return marginOCO;
    }

    public MarginNewOrder toMarginLimitOrder(BinanceLimitOrderRequest request) {
        MarginNewOrder limitOrder = new MarginNewOrder(getSymbol(request.getSymbol()), SELL, LIMIT, GTC, request.getQuantity(), request.getPrice());
        limitOrder.sideEffectType(AUTO_REPAY);
        return limitOrder;
    }

    public CancelOrderRequest toCancelOrder(Order order) {
        return new CancelOrderRequest(order.getSymbol(), order.getOrderId());
    }

    private String getSymbol(String tradingPair) {
        return tradingPair.replace("/", "");
    }
}
