package com.example.brokergateway.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class OcoOrderRequest extends StopLimitOrderRequest {
    @NotBlank
    private String stopLossPrice;
    @NotBlank
    private String takeProfitPrice;
}
