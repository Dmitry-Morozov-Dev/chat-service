package com.messenger.chat_service_new.service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.messenger.chat_service_new.modelHelper.DTO.MediaDTO;
import com.messenger.chat_service_new.elasticRepository.MessageSearchRepository;
import com.messenger.chat_service_new.modelHelper.enums.MessageStatus;
import com.messenger.chat_service_new.modelHelper.events.MessageEnvelope;
import com.messenger.chat_service_new.repository.*;
import com.messenger.chat_service_new.service.media.MediaEditService;
import com.messenger.chat_service_new.service.message.MessageBuilder;
import com.messenger.chat_service_new.service.message.MessageDeleteService;
import com.messenger.chat_service_new.service.message.MessageEditService;
import com.messenger.chat_service_new.utils.BucketPartitionCalculator;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverRecord;

import java.time.Duration;
import java.time.Instant;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageEventConsumer {

    private final ObjectMapper objectMapper;
    private final KafkaReceiver<String, String> kafkaReceiver;
    private final KafkaProducerService kafkaProducerService;
    private final MeterRegistry meterRegistry;

    private final MessageBuilder messageBuilder;
    private final MediaEditService mediaEditService;
    private final MessageEditService messageEditService;
    private final MessageDeleteService messageDeleteService;

    private final MessageRepository messageRepository;
    private final MediaRepository mediaRepository;
    private final MessageSearchRepository messageSearchRepository;
    private final UserChatsInfoRepository userChatsInfoRepository;

    private Timer processTimer;
    private Counter eventCounter;

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        processTimer = meterRegistry.timer("chat.message.consumer.process.time");
        eventCounter = meterRegistry.counter("chat.message.consumer.events", "type", "unknown");

        kafkaReceiver.receive()
                .publishOn(Schedulers.parallel())
                .flatMap(this::process)
                .subscribe(
                        v -> {},
                        err -> log.error("Fatal consumer error", err)
                );
    }

    private Mono<Void> process(ReceiverRecord<String, String> record) {
        long start = System.nanoTime();

        return Mono.just(record.value())
                .flatMap(this::parseEnvelope)
                .flatMap(env -> {
                    eventCounter.increment();
                    return processEnvelope(env);
                })
                .then(record.receiverOffset().commit())
                .doOnSuccess(v -> processTimer.record(Duration.ofNanos(System.nanoTime() - start)))
                .onErrorResume(t -> {
                    log.error("Failed to process message: {}", record.value(), t);
                    return kafkaProducerService.sendToDlq(record.key(), record.value());
                });
    }

    private Mono<MessageEnvelope> parseEnvelope(String json) {
        return Mono.fromCallable(() -> objectMapper.readValue(json, MessageEnvelope.class))
                .onErrorMap(e -> new IllegalArgumentException("Invalid envelope JSON", e));
    }

    private Mono<Void> processEnvelope(MessageEnvelope env) {
        UUID chatId = uuid(env.getChatId());
        UUID senderId = uuid(env.getSenderId());
        UUID realId = uuid(env.getRealId());

        return switch (env.getType()) {
            case MESSAGE -> handleMessage(env, chatId, senderId, realId);
            case EDIT -> handleEdit(env, chatId, senderId, realId);
            case DELETE -> handleDelete(env, chatId, senderId, realId);
            case READ -> handleRead(env, chatId, senderId, realId);
            case DELIVERED -> handleDelivered(env, chatId, realId);
            default -> Mono.empty();
        };
    }

    private Mono<Void> handleMessage(MessageEnvelope env,
                                     UUID chatId,
                                     UUID senderId,
                                     UUID messageId) {

        String bucket = bucket(env);

        var message = messageBuilder.buildMessage(env, chatId, senderId, messageId, bucket);

        Mono<Void> saveMessage = messageRepository.save(message).then();

        Mono<Void> saveMedia = saveMedia(env.getMediaDTOS(), env, chatId, senderId, messageId, bucket);

        var searchDoc = messageBuilder.buildSearch(env, chatId, senderId, messageId);
        Mono<Void> saveSearch = messageSearchRepository.save(searchDoc).then();

        return Mono.when(saveMessage, saveMedia, saveSearch).then();
    }

    private Mono<Void> saveMedia(List<MediaDTO> dtos,
                                 MessageEnvelope env,
                                 UUID chatId,
                                 UUID senderId,
                                 UUID messageId,
                                 String bucket) {
        if (dtos == null || dtos.isEmpty()) return Mono.empty();

        return Flux.fromIterable(dtos)
                .map(dto -> messageBuilder.buildMedia(dto, chatId, senderId, messageId, bucket, env))
                .flatMap(mediaRepository::save)
                .then();
    }

    private Mono<Void> handleEdit(MessageEnvelope env,
                                  UUID chatId,
                                  UUID senderId,
                                  UUID messageId) {

        String bucket = bucket(env);

        Mono<Void> updateMessage = messageEditService.updateMessage(env, chatId, senderId, messageId, bucket);
        Mono<Void> updateSearch = messageEditService.updateSearch(env, chatId, senderId, messageId);

        if (env.getEdit().newMediaDTO() == null || env.getEdit().newMediaDTO().isEmpty()) {
            return Mono.when(updateMessage, updateSearch);
        }

        Mono<Void> applyMediaChanges =
                mediaEditService.applyEdit(
                        env.getEdit().newMediaDTO(),
                        chatId,
                        messageId,
                        senderId,
                        bucket
                );

        return Mono.when(updateMessage, updateSearch, applyMediaChanges);
    }

    private Mono<Void> handleDelete(MessageEnvelope env,
                                    UUID chatId,
                                    UUID senderId,
                                    UUID messageId) {

        String bucket = bucket(env);
        return messageDeleteService.delete(chatId, senderId, messageId, bucket);
    }

    private Mono<Void> handleRead(MessageEnvelope env,
                                  UUID chatId,
                                  UUID userId,
                                  UUID messageId) {

        UUID readUpTo = uuid(env.getReadUpTo());
        String bucket = bucket(env);

        Mono<Void> updateUserInfo =
                userChatsInfoRepository
                        .findByUserIdAndChatId(userId, chatId)
                        .switchIfEmpty(Mono.error(new IllegalStateException("UserChatsInfo not found")))
                        .flatMap(info -> {
                            info.setLastReadMessage(readUpTo);
                            return userChatsInfoRepository.save(info);
                        })
                        .then();

        Mono<Void> markMessageAsRead =
                messageRepository
                        .findByChatIdAndBucketMonthAndMessageId(chatId, bucket, messageId)
                        .flatMap(msg -> {
                            if (msg.getStatus() == MessageStatus.READ) return Mono.empty();
                            msg.setStatus(MessageStatus.READ);
                            return messageRepository.save(msg);
                        })
                        .then();

        return Mono.when(updateUserInfo, markMessageAsRead);
    }

    private Mono<Void> handleDelivered(MessageEnvelope env,
                                       UUID chatId,
                                       UUID messageId) {

        String bucket = bucket(env);

        return messageRepository
                .findByChatIdAndBucketMonthAndMessageId(chatId, bucket, messageId)
                .flatMap(msg -> {
                    if (msg.getStatus() == MessageStatus.READ) return Mono.empty();
                    msg.setStatus(MessageStatus.DELIVERED);
                    return messageRepository.save(msg);
                })
                .then();
    }

    private UUID uuid(String s) {
        return s == null ? null : UUID.fromString(s);
    }

    private String bucket(MessageEnvelope env) {
        return BucketPartitionCalculator.getMessageBucketPartition(
                Instant.ofEpochMilli(env.getTimestamp())
        );
    }
}
