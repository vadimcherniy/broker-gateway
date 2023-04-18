package com.example.brokergateway.utils;

import com.example.brokergateway.enums.Symbol;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.function.Function;

public class Formatter {
    private static final Map<Symbol, Function<BigDecimal, String>> quantityFormatter = Map.of(
            Symbol.BTC, Formatter::btcQuantity,
            Symbol.ETH, Formatter::ethQuantity,
            Symbol.MATIC, Formatter::maticQuantity,
            Symbol.ARB, Formatter::arbQuantity
    );

    private static final Map<Symbol, Function<BigDecimal, String>> priceFormatter = Map.of(
            Symbol.BTC, Formatter::btcPrice,
            Symbol.ETH, Formatter::ethPrice,
            Symbol.MATIC, Formatter::maticPrice,
            Symbol.ARB, Formatter::arbPrice
    );

    public static String getPrice(Symbol symbol, BigDecimal price) {
        return priceFormatter.get(symbol).apply(price);
    }

    public static String getQuantity(Symbol symbol, BigDecimal quantity) {
        return quantityFormatter.get(symbol).apply(quantity);
    }

    private static String btcPrice(BigDecimal price) {
        return price != null ? price.setScale(2, RoundingMode.FLOOR).toPlainString() : null;
    }

    private static String ethPrice(BigDecimal price) {
        return price != null ? price.setScale(2, RoundingMode.FLOOR).toPlainString() : null;
    }

    private static String maticPrice(BigDecimal price) {
        return price != null ? price.setScale(4, RoundingMode.FLOOR).toPlainString() : null;
    }

    private static String arbPrice(BigDecimal price) {
        return price != null ? price.setScale(4, RoundingMode.FLOOR).toPlainString() : null;
    }

    private static String btcQuantity(BigDecimal quantity) {
        return quantity != null ? quantity.setScale(5, RoundingMode.FLOOR).toPlainString() : null;
    }

    private static String ethQuantity(BigDecimal quantity) {
        return quantity != null ? quantity.setScale(4, RoundingMode.FLOOR).toPlainString() : null;
    }

    private static String maticQuantity(BigDecimal quantity) {
        return quantity != null ? quantity.setScale(1, RoundingMode.FLOOR).toPlainString() : null;
    }

    private static String arbQuantity(BigDecimal quantity) {
        return quantity != null ? quantity.setScale(1, RoundingMode.FLOOR).toPlainString() : null;
    }
}
