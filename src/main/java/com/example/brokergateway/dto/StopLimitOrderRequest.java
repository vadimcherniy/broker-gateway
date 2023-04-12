package com.example.brokergateway.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class StopLimitOrderRequest extends BaseOrderRequest {
    @NotBlank
    private String triggeredPrice;
}
