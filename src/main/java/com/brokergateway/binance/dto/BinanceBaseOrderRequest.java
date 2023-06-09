package com.brokergateway.binance.dto;

import com.brokergateway.binance.utils.Formatter;
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

@Getter
@Setter
@ToString
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, visible = true, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = BinanceLimitOrderRequest.class, name = "LIMIT"),
        @JsonSubTypes.Type(value = BinanceStopLimitOrderRequest.class, name = "STOP_LIMIT"),
        @JsonSubTypes.Type(value = BinanceOcoOrderRequest.class, name = "OCO"),
        @JsonSubTypes.Type(value = BinanceCancelOrderRequest.class, name = "CANCEL")
})
public class BinanceBaseOrderRequest {
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
        return Formatter.getPrice(Symbol.getBySymbol(symbol), price);
    }

    public String getQuantity() {
        return Formatter.getQuantity(Symbol.getBySymbol(symbol), quantity);
    }
}
