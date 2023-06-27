package com.brokergateway.ib.service;

import com.brokergateway.ib.configuration.IBBeansFactory;
import com.brokergateway.ib.dto.*;
import com.brokergateway.ib.repository.IBRepository;
import com.ib.client.Decimal;
import com.ib.client.Order;
import com.ib.client.Types;
import com.ib.contracts.StkContract;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.brokergateway.ib.dto.RequestOrderType.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class IBOrderProcessingService {
    private final IBIdGenerator idGenerator;
    private final IBBeansFactory ibBeansFactory;
    private final IBRepository repository;

    private final Map<RequestOrderType, Consumer<IBBaseOrderRequest>> handlers = Map.of(
            STOP_LIMIT, request -> processStopLimitOrderRequest((IBStopLimitOrderRequest) request),
            CANCEL, request -> processCancelOrderRequest((IBCancelOrderRequest) request),
            LIMIT, request -> processLimitOrderRequest((IBLimitOrderRequest) request)
    );

    public void processOrderRequest(IBBaseOrderRequest orderRequest) {
        handlers.get(orderRequest.getType()).accept(orderRequest);
    }

    private void processStopLimitOrderRequest(IBStopLimitOrderRequest orderRequest) {
        int parentOrderId = idGenerator.incrementAndGet();
        Decimal quantity = Decimal.get(orderRequest.getQuantity().setScale(0, RoundingMode.DOWN));
        double lmtPrice = orderRequest.getPositionType() == PositionType.LONG
                ? orderRequest.getPrice().setScale(2, RoundingMode.UP).doubleValue()
                : orderRequest.getPrice().setScale(2, RoundingMode.DOWN).doubleValue();
        double auxPrice = orderRequest.getPositionType() == PositionType.LONG
                ? orderRequest.getTriggeredPrice().setScale(2, RoundingMode.UP).doubleValue()
                : orderRequest.getTriggeredPrice().setScale(2, RoundingMode.DOWN).doubleValue();

        Order parent = new Order();
        parent.orderId(parentOrderId);
        parent.action(orderRequest.getPositionType() == PositionType.LONG ? Types.Action.BUY : Types.Action.SELL);
        parent.orderType("STP LMT");
        parent.totalQuantity(quantity);
        parent.lmtPrice(lmtPrice);
        parent.auxPrice(auxPrice); // triggered price
        //The parent and children orders will need this attribute set to false to prevent accidental executions.
        //The LAST CHILD will have it set to true.
        parent.transmit(false);

        Order takeProfit = new Order();
        takeProfit.orderId(idGenerator.incrementAndGet());
        takeProfit.action(orderRequest.getPositionType() == PositionType.LONG ? Types.Action.SELL : Types.Action.BUY);
        takeProfit.orderType("LMT");
        takeProfit.totalQuantity(quantity);
        takeProfit.lmtPrice(orderRequest.getTakeProfitPrice().setScale(2, RoundingMode.HALF_UP).doubleValue());
        takeProfit.parentId(parentOrderId);
        takeProfit.tif(Types.TimeInForce.GTC);
        takeProfit.transmit(false);

        Order stopLoss = new Order();
        stopLoss.orderId(idGenerator.incrementAndGet());
        stopLoss.action(orderRequest.getPositionType() == PositionType.LONG ? Types.Action.SELL : Types.Action.BUY);
        stopLoss.orderType("STP");
        //Stop trigger price
        stopLoss.auxPrice(orderRequest.getStopLossPrice().setScale(2, RoundingMode.HALF_UP).doubleValue());
        stopLoss.totalQuantity(quantity);
        stopLoss.parentId(parentOrderId);
        stopLoss.tif(Types.TimeInForce.GTC);
        //In this case, the low side order will be the last child being sent. Therefore, it needs to set this attribute to true
        //to activate all its predecessors
        stopLoss.transmit(true);

        List<Order> bracketOrder = new ArrayList<>();
        bracketOrder.add(parent);
        bracketOrder.add(takeProfit);
        bracketOrder.add(stopLoss);

        for (Order o : bracketOrder) {
            ibBeansFactory.getIBClient().placeOrder(o.orderId(), new StkContract(orderRequest.getSymbol()), o);
        }
    }

    private void processCancelOrderRequest(IBCancelOrderRequest orderRequest) {
        ibBeansFactory.getIBClient().reqGlobalCancel();
    }

    @SneakyThrows
    private void processLimitOrderRequest(IBLimitOrderRequest orderRequest) {
        Decimal availableQuantity = repository.getQuantity(orderRequest.getSymbol());
        Decimal orderQuantity = Decimal.get(
                availableQuantity.multiply(Decimal.get(orderRequest.getQuantityPercentage())).divide(Decimal.ONE_HUNDRED)
                        .value()
                        .setScale(0, RoundingMode.UP)
        );
        ibBeansFactory.getIBClient().reqGlobalCancel(); // close opened orders: tp, sl orders
        Thread.sleep(1000);

        int orderId = idGenerator.incrementAndGet();
        Order limitOrder = new Order();
        limitOrder.orderId(orderId);
        limitOrder.action(orderRequest.getPositionType() == PositionType.LONG ? Types.Action.SELL : Types.Action.BUY);
        limitOrder.orderType("LMT");
        limitOrder.totalQuantity(orderQuantity);
        limitOrder.lmtPrice(orderRequest.getPrice().setScale(2, RoundingMode.HALF_UP).doubleValue());
        limitOrder.tif(Types.TimeInForce.GTC);
        ibBeansFactory.getIBClient().placeOrder(orderId, new StkContract(orderRequest.getSymbol()), limitOrder);

        availableQuantity = Decimal.get(availableQuantity.value().subtract(orderQuantity.value()));
        repository.save(orderRequest.getSymbol(), availableQuantity);

        if (availableQuantity.compareTo(Decimal.ZERO) != 0) {
            Order takeProfit = new Order();
            takeProfit.ocaGroup("123456789");
            takeProfit.ocaType(1);
            takeProfit.orderId(idGenerator.incrementAndGet());
            takeProfit.action(orderRequest.getPositionType() == PositionType.LONG ? Types.Action.SELL : Types.Action.BUY);
            takeProfit.orderType("LMT");
            takeProfit.totalQuantity(availableQuantity);
            takeProfit.tif(Types.TimeInForce.GTC);
            takeProfit.lmtPrice(orderRequest.getTakeProfitPrice().setScale(2, RoundingMode.HALF_UP).doubleValue());

            Order stopLoss = new Order();
            stopLoss.ocaGroup("123456789");
            stopLoss.ocaType(1);
            stopLoss.orderId(idGenerator.incrementAndGet());
            stopLoss.action(orderRequest.getPositionType() == PositionType.LONG ? Types.Action.SELL : Types.Action.BUY);
            stopLoss.orderType("STP");
            stopLoss.tif(Types.TimeInForce.GTC);
            //Stop trigger price
            stopLoss.auxPrice(orderRequest.getStopLossPrice().setScale(2, RoundingMode.HALF_UP).doubleValue());
            stopLoss.totalQuantity(availableQuantity);

            List<Order> bracketOrder = new ArrayList<>();
            bracketOrder.add(takeProfit);
            bracketOrder.add(stopLoss);

            for (Order o : bracketOrder) {
                ibBeansFactory.getIBClient().placeOrder(o.orderId(), new StkContract(orderRequest.getSymbol()), o);
            }
        }
    }
}
