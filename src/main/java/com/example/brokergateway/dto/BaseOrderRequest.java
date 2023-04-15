package com.example.brokergateway.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@Setter
@ToString
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, visible = true, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = LimitOrderRequest.class, name = "LIMIT"),
        @JsonSubTypes.Type(value = StopLimitOrderRequest.class, name = "STOP_LIMIT"),
        @JsonSubTypes.Type(value = OcoOrderRequest.class, name = "OCO"),
        @JsonSubTypes.Type(value = CancelOrderRequest.class, name = "CANCEL")
})
public class BaseOrderRequest {
    @NotNull
    private PositionType positionType;
    @NotNull
    private RequestOrderType type;
    @NotBlank
    private String symbol;
    private BigDecimal quantity;
    @Min(1)
    @Max(100)
    private Integer quantityPercentage;
    private BigDecimal price;
    @Min(1)
    private Integer delay;

    public String getPrice() {
        return price != null ? price.setScale(2, RoundingMode.HALF_UP).toPlainString() : null;
    }

    public String getQuantity() {
        return quantity != null ? quantity.setScale(5, RoundingMode.FLOOR).toPlainString() : null;
    }
}
