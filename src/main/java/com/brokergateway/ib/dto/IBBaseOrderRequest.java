package com.brokergateway.ib.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Getter
@Setter
@ToString
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, visible = true, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = IBLimitOrderRequest.class, name = "LIMIT"),
        @JsonSubTypes.Type(value = IBStopLimitOrderRequest.class, name = "STOP_LIMIT"),
        @JsonSubTypes.Type(value = IBCancelOrderRequest.class, name = "CANCEL")
})
public class IBBaseOrderRequest {
    @NotNull
    private PositionType positionType;
    @NotNull
    private RequestOrderType type;
    @NotBlank
    private String symbol;
    private BigDecimal quantity;
    private BigDecimal price;
    @Min(1)
    private Integer delay;
}
