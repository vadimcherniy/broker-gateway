package com.brokergateway.binance.factory;

import com.brokergateway.binance.dto.PositionType;
import com.brokergateway.binance.service.LongOrderProcessingService;
import com.brokergateway.binance.service.OrderProcessingService;
import com.brokergateway.binance.service.ShortOrderProcessingService;
import com.brokergateway.binance.service.margin.MarginLongOrderProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BinanceOrderProcessingFactory {
    private final MarginLongOrderProcessingService marginLongOrderProcessingService;
    private final ShortOrderProcessingService shortOrderProcessingService;

    public OrderProcessingService getOrderProcessingService(PositionType positionType) {
        return positionType == PositionType.SHORT ? shortOrderProcessingService : marginLongOrderProcessingService;
    }
}
