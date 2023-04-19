package com.example.brokergateway.dto;

import com.example.brokergateway.enums.Symbol;
import com.example.brokergateway.utils.Formatter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Setter
public class StopLimitOrderRequest extends BaseOrderRequest {
    @NotBlank
    private BigDecimal triggeredPrice;

    public String getTriggeredPrice() {
        return Formatter.getPrice(Symbol.getBySymbol(getSymbol()), triggeredPrice);
    }
}
