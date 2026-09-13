package com.banking.transaction.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.EmbeddedKafkaZKBroker;

//@Configuration
public class EmbeddedKafkaConfig {

    private EmbeddedKafkaBroker broker;

    @PostConstruct
    public void startBroker() {
        try {
            broker = new EmbeddedKafkaZKBroker(1, true, 1, "transfer-initiated", "transfer-success", "transfer-failed");
            broker.kafkaPorts(9092);
            broker.afterPropertiesSet();
            System.out.println("=== In-Process Kafka Broker listening on port 9092 ===");
        } catch (Exception e) {
            System.out.println("Kafka port 9092 active or external broker detected.");
        }
    }

    @PreDestroy
    public void stopBroker() {
        if (broker != null) {
            try {
                broker.destroy();
            } catch (Exception ignored) {}
        }
    }
}