package com.example.brokergateway.dto;

import com.binance.api.client.domain.OrderSide;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, visible = true, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = LimitOrderRequest.class, name = "LIMIT"),
        @JsonSubTypes.Type(value = StopLimitOrderRequest.class, name = "STOP_LIMIT"),
        @JsonSubTypes.Type(value = OcoOrderRequest.class, name = "OCO"),
        @JsonSubTypes.Type(value = CancelOrderRequest.class, name = "CANCEL")
})
public class BaseOrderRequest {
    private OrderSide side;
    @NotNull
    private RequestOrderType type;
    @NotBlank
    private String symbol;
    private String quantity;
    @Min(1)
    @Max(100)
    private Integer quantityPercentage;
    private String price;
    private Integer delay;

    public String getQuantity() {
        return quantity != null ? String.format("%.5f", Double.valueOf(quantity)) : "0.0";
    }
}
