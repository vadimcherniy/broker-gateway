package com.brokergateway.ib.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class IBIdGenerator {
    private final AtomicInteger atomicInteger = new AtomicInteger(0);

    public void setValidId(int id) {
        atomicInteger.set(id);
    }

    public int incrementAndGet() {
        return atomicInteger.incrementAndGet();
    }
}
