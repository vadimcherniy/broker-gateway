//package com.brokergateway.ib.handler;
//
//import com.ib.client.EClientSocket;
//import com.ib.client.EJavaSignal;
//import com.ib.client.EReader;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.context.event.ApplicationReadyEvent;
//import org.springframework.context.event.EventListener;
//import org.springframework.stereotype.Component;
//
//@Component
//@Slf4j
//@RequiredArgsConstructor
//public class IBMessageHandler {
//    private final EClientSocket client;
//    private final EJavaSignal signal;
//
//    @EventListener(ApplicationReadyEvent.class)
//    public void init() {
//        client.reqAccountUpdates(true, "DU2436393");
////        client.reqAccountUpdates(true, "U4234082");
//
//        final EReader reader = new EReader(client, signal);
//        reader.start();
//        new Thread(() -> {
//            while (client.isConnected()) {
//                signal.waitForSignal();
//                try {
//                    reader.processMsgs();
//                } catch (Exception e) {
//                    log.error(e.getMessage());
//                }
//            }
//        }).start();
//    }
//}
