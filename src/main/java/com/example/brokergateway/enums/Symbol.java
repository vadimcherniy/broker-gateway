package com.example.brokergateway.enums;

public enum Symbol {
    BTC,
    ETH,
    MATIC,
    ARB;

    public static Symbol getBySymbol(String pair) {
        return switch (pair.substring(0, pair.indexOf("/"))) {
            case "BTC" -> BTC;
            case "ETH" -> ETH;
            case "MATIC" -> MATIC;
            case "ARB" -> ARB;
            default -> null;
        };
    }
}
