package com.example.brokergateway.client.domain;

import com.binance.api.client.constant.BinanceApiConstants;
import com.binance.api.client.domain.OrderSide;
import com.binance.api.client.domain.TimeInForce;
import com.binance.api.client.domain.account.NewOrderResponseType;
import com.binance.api.client.domain.account.SideEffectType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class NewMarginOCO {
    private String symbol;
    private String isIsolated;
    private String listClientOrderId;
    private OrderSide side;
    private String quantity;
    private String limitClientOrderId;
    private String price;
    private String limitIcebergQty;
    private String stopClientOrderId;
    private String stopPrice;
    private String stopLimitPrice;
    private String stopIcebergQty;
    private TimeInForce stopLimitTimeInForce;
    private NewOrderResponseType newOrderRespType;
    private SideEffectType sideEffectType;
    private Long recvWindow;
    private long timestamp;

    public NewMarginOCO(String symbol, OrderSide side, String quantity, String price, String stopPrice) {
        this.symbol = symbol;
        this.side = side;
        this.quantity = quantity;
        this.price = price;
        this.stopPrice = stopPrice;
        this.timestamp = System.currentTimeMillis();
        this.recvWindow = BinanceApiConstants.DEFAULT_RECEIVING_WINDOW;
    }
}
