package com.example.brokergateway.dto;

import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Setter
public class StopLimitOrderRequest extends BaseOrderRequest {
    @NotBlank
    private BigDecimal triggeredPrice;

    public String getTriggeredPrice() {
        return triggeredPrice != null ? triggeredPrice.setScale(2, RoundingMode.HALF_UP).toPlainString() : null;
    }
}
