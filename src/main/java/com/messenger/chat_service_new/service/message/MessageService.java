package com.messenger.chat_service_new.service.message;

import com.messenger.chat_service_new.modelHelper.DTO.MessageDTO;
import com.messenger.chat_service_new.elasticRepository.MessageSearchRepository;
import com.messenger.chat_service_new.modelElasticsearch.MessageSearchDocument;
import com.messenger.chat_service_new.repository.*;
import com.messenger.chat_service_new.utils.BucketPartitionCalculator;
import com.messenger.chat_service_new.utils.MessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    @Value("${message.max-limit:100}")
    private int maxLimit;

    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final MessageSearchRepository messageSearchRepository;
    private final MessageMapper messageMapper;

    private final MessageFetcher messageFetcher;
    private final MessageEnricher messageEnricher;
    private final MessageSecurityValidator securityValidator;

    public Mono<List<MessageDTO>> getMessages(UUID chatId, long limit, String bucketMonth, UUID after, UUID before, UUID around) {
        if (limit > maxLimit) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Limit exceeds maximum of 100"));
        }

        return securityValidator.ensureParticipant(chatId)
                .flatMap(userId -> chatRepository.findByChatId(chatId)
                        .flatMap(chat -> {
                            Instant chatCreatedAt = chat.getCreatedAt();
                            String initialBucket = (bucketMonth == null || bucketMonth.isBlank())
                                    ? BucketPartitionCalculator.getMessageBucketPartition(Instant.now())
                                    : bucketMonth;

                            if (around != null) {
                                if (bucketMonth == null || bucketMonth.isBlank()) {
                                    return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                            "bucketMonth is required when using 'around'"));
                                }
                                long half = limit / 2;
                                return messageFetcher.fetchAroundMessage(chatId, around, half, initialBucket)
                                        .map(messageMapper::entitiesToDTOs)
                                        .flatMap(messageEnricher::enrichWithUser)
                                        .flatMap(messageEnricher::enrichWithMedia);
                            } else {
                                return messageFetcher.fetchMessagesAcrossBuckets(chatId, initialBucket, limit, after, before, chatCreatedAt)
                                        .map(messageMapper::entitiesToDTOs)
                                        .flatMap(messageEnricher::enrichWithUser)
                                        .flatMap(messageEnricher::enrichWithMedia);
                            }
                        })
                );
    }

    public Mono<List<MessageDTO>> searchMessages(UUID chatId, String query, long offset, long limit) {
        if (limit > maxLimit) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Limit exceeds maximum of 100"));
        }

        if (query == null || query.isEmpty()) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Query cannot be empty"));
        }

        return securityValidator.ensureParticipant(chatId)
                .flatMap(userId -> messageSearchRepository.findByChatIdAndContentContaining(chatId, query)
                        .skip(offset)
                        .take(limit)
                        .collectList()
                        .flatMap(searchDocs -> {
                            if (searchDocs.isEmpty()) {
                                return Mono.just(List.of());
                            }

                            Map<String, List<UUID>> idsByBucket = searchDocs.stream()
                                    .collect(Collectors.groupingBy(
                                            doc -> BucketPartitionCalculator.getMessageBucketPartition(doc.getCreatedAt()),
                                            Collectors.mapping(MessageSearchDocument::getMessageId, Collectors.toList())
                                    ));

                            return Flux.fromIterable(idsByBucket.entrySet())
                                    .flatMap(entry -> {
                                        String bucket = entry.getKey();
                                        List<UUID> messageIds = entry.getValue();
                                        return messageRepository.findByChatIdAndBucketMonthAndMessageIdIn(chatId, bucket, messageIds)
                                                .collectList();
                                    })
                                    .collectList()
                                    .map(lists -> lists.stream().flatMap(List::stream).toList())
                                    .map(messageMapper::entitiesToDTOs)
                                    .flatMap(messageEnricher::enrichWithMedia)
                                    .flatMap(messageEnricher::enrichWithUser);
                        })
                );
    }
}
