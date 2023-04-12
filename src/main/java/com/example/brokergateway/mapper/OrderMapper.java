package com.example.brokergateway.mapper;

import com.binance.api.client.domain.OrderType;
import com.binance.api.client.domain.TimeInForce;
import com.binance.api.client.domain.account.NewOCO;
import com.binance.api.client.domain.account.NewOrder;
import com.binance.api.client.domain.account.Order;
import com.binance.api.client.domain.account.request.CancelOrderRequest;
import com.example.brokergateway.dto.LimitOrderRequest;
import com.example.brokergateway.dto.OcoOrderRequest;
import com.example.brokergateway.dto.StopLimitOrderRequest;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public abstract class OrderMapper {

    public NewOrder toNewOrder(LimitOrderRequest request) {
        return new NewOrder(getSymbol(request.getSymbol()), request.getSide(), OrderType.LIMIT, TimeInForce.GTC, request.getQuantity(), request.getPrice());
    }

    public NewOrder toStopLimitOrder(StopLimitOrderRequest request) {
        NewOrder order = new NewOrder(getSymbol(request.getSymbol()), request.getSide(), OrderType.STOP_LOSS_LIMIT, TimeInForce.GTC, request.getQuantity(), request.getPrice());
        order.stopPrice(request.getTriggeredPrice());
        return order;
    }

    public NewOCO toOcoOrder(OcoOrderRequest request) {
        NewOCO ocoOrder = new NewOCO(getSymbol(request.getSymbol()), request.getSide(), request.getQuantity(), request.getTakeProfitPrice(), request.getStopLossPrice());
        ocoOrder.setStopLimitPrice(request.getPrice());
        ocoOrder.setStopLimitTimeInForce(TimeInForce.FOK);
        return ocoOrder;
    }

    public CancelOrderRequest toCancelOrder(Order order) {
        return new CancelOrderRequest(order.getSymbol(), order.getOrderId());
    }

    private String getSymbol(String tradingPair) {
        return tradingPair.replace("/", "");
    }
}
