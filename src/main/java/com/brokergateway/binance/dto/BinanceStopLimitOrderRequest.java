package com.brokergateway.binance.dto;

import com.brokergateway.binance.utils.Formatter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Setter
public class BinanceStopLimitOrderRequest extends BinanceBaseOrderRequest {
    @NotBlank
    private BigDecimal triggeredPrice;

    public String getTriggeredPrice() {
        return Formatter.getPrice(Symbol.getBySymbol(getSymbol()), triggeredPrice);
    }
}
