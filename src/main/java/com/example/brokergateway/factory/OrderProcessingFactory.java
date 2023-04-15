package com.example.brokergateway.factory;

import com.example.brokergateway.dto.PositionType;
import com.example.brokergateway.service.LongPositionOrderProcessingService;
import com.example.brokergateway.service.OrderProcessingService;
import com.example.brokergateway.service.ShortPositionOrderProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderProcessingFactory {
    private final LongPositionOrderProcessingService longPositionOrderProcessingService;
    private final ShortPositionOrderProcessingService shortPositionOrderProcessingService;

    public OrderProcessingService getOrderProcessingService(PositionType positionType) {
        return positionType == PositionType.SHORT ? shortPositionOrderProcessingService : longPositionOrderProcessingService;
    }
}
