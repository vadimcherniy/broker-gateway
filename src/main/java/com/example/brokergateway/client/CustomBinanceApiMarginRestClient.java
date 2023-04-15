package com.example.brokergateway.client;

import com.binance.api.client.domain.account.MarginNewOrderResponse;
import com.binance.api.client.impl.BinanceApiMarginRestClientImpl;
import com.example.brokergateway.client.domain.NewMarginOCO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static com.binance.api.client.impl.BinanceApiServiceGenerator.createService;
import static com.binance.api.client.impl.BinanceApiServiceGenerator.executeSync;

@Service
public class CustomBinanceApiMarginRestClient extends BinanceApiMarginRestClientImpl {
    private final CustomBinanceApiService customBinanceApiService;

    public CustomBinanceApiMarginRestClient(@Value(value = "${binance.api.key}") String apiKey,
                                            @Value(value = "${binance.secret.key}") String secretKey) {
        super(apiKey, secretKey);
        customBinanceApiService = createService(CustomBinanceApiService.class, apiKey, secretKey);
    }

    public MarginNewOrderResponse newMarginOcoOrder(NewMarginOCO order) {
        return executeSync(customBinanceApiService.newMarginOcoOrder(
                order.getSymbol(), order.getSide(), order.getQuantity(), order.getPrice(), order.getStopPrice(), order.getStopLimitPrice(),
                order.getNewOrderRespType(), order.getSideEffectType(), order.getStopLimitTimeInForce(), order.getRecvWindow(), order.getTimestamp())
        );
    }
}
