package com.brokergateway.ib.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Getter
@Setter
public class IBLimitOrderRequest extends IBBaseOrderRequest {
    @Min(1)
    @Max(100)
    private Integer quantityPercentage;
    @NotBlank
    private BigDecimal stopLossPrice;
    @NotBlank
    private BigDecimal takeProfitPrice;
}
