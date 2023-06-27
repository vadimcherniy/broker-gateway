package com.brokergateway.ib.configuration;

import com.ib.client.EClientSocket;
import com.ib.client.EJavaSignal;
import com.ib.client.EReader;
import com.ib.client.EWrapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class IBBeansFactory {
    private static final long period = TimeUnit.MINUTES.toMillis(1);
    private static final long delay = TimeUnit.SECONDS.toMillis(1);
    @Value("${tws.host}")
    private String host;
    @Value("${tws.live.port}")
    private Integer twsPort;
    private final EWrapper wrapper;
    private static EJavaSignal javaSignal;
    private static EClientSocket client;

    @SneakyThrows
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        javaSignal = new EJavaSignal();
        client = new EClientSocket(wrapper, javaSignal);
        client.eConnect(host, twsPort, 0);

        TimeUnit.SECONDS.sleep(1);
        initIBMessageHandler();
    }

    public EClientSocket getIBClient() {
        client.reqCurrentTime();

        if (!client.isConnected()) {
            log.info("Not connected to TWS. Trying to reconnect.");
            init();
            if (client.isConnected()) {
                log.info("TWS connection is established");
            }
        }

        return client;
    }

    private void initIBMessageHandler() {
        client.reqAccountUpdates(true, "U4234082");

        final EReader reader = new EReader(client, javaSignal);
        reader.start();
        new Thread(() -> {
            while (client.isConnected()) {
                javaSignal.waitForSignal();
                try {
                    reader.processMsgs();
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }
        }).start();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void checkConnection() {
        TimerTask task = new TimerTask() {
            public void run() {
                getIBClient();
            }
        };
        Timer timer = new Timer();
        timer.schedule(task, delay, period);
    }
}
