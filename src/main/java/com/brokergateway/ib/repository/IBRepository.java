package com.brokergateway.ib.repository;

import com.ib.client.Decimal;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class IBRepository {
    private static final Map<String, Decimal> repository = new HashMap<>();

    public Decimal getQuantity(String symbol) {
        return repository.getOrDefault(symbol, Decimal.ZERO);
    }

    public void save(String symbol, Decimal quantity) {
        repository.put(symbol, quantity);
    }

    public void delete(String symbol) {
        repository.remove(symbol);
    }
}
