package com.brokergateway.binance.dto;

import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Setter
public class BinanceOcoOrderRequest extends BinanceBaseOrderRequest {
    @NotBlank
    private BigDecimal stopLossPrice;
    @NotBlank
    private BigDecimal takeProfitPrice;

    public String getStopLossPrice() {
        return stopLossPrice != null ? stopLossPrice.setScale(2, RoundingMode.HALF_UP).toPlainString() : null;
    }

    public String getTakeProfitPrice() {
        return takeProfitPrice != null ? takeProfitPrice.setScale(2, RoundingMode.HALF_UP).toPlainString() : null;
    }
}
