package com.brokergateway.ib.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Setter
@Getter
public class IBStopLimitOrderRequest extends IBBaseOrderRequest {
    @NotBlank
    private BigDecimal triggeredPrice;
    @NotBlank
    private BigDecimal stopLossPrice;
    @NotBlank
    private BigDecimal takeProfitPrice;
}
