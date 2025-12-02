package com.messenger.chat_service_new.service.kafka;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaSender<String, String> kafkaSender;

    @Value("${kafka.topics.dlq:chat.dlq}")
    private String dlqTopic;

    public Mono<Void> sendToDlq(String key, String value) {
        return kafkaSender.send(
                        Mono.just(SenderRecord.create(new ProducerRecord<>(dlqTopic, key, value), null))
                )
                .then()
                .doOnSuccess(result -> log.error("Sent to DLQ: key={}, value={}", key, value))
                .doOnError(err -> log.error("Failed to send to DLQ!", err))
                .then();
    }

    @CircuitBreaker(name = "kafkaProducer", fallbackMethod = "kafkaFallback")
    public Mono<Void> send(String topic, String key, String value, String userId) {
        return kafkaSender.send(Mono.just(SenderRecord.create(topic, null, null, key, value, null)))
                .next()
                .doOnSuccess(res -> log.atInfo().addKeyValue("userId", userId).addKeyValue("key", key).log("Sent to Kafka"))
                .then();
    }

    public Mono<Void> kafkaFallback(String topic, String key, String value, String userId, Throwable t) {
        log.atError().addKeyValue("userId", userId).setCause(t).log("Kafka fallback triggered");
        return sendToDlq(key, value + " | fallback: " + t.getMessage());
    }
}
