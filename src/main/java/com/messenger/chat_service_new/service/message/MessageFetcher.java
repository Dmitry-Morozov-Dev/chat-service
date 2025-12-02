package com.messenger.chat_service_new.service.message;

import com.messenger.chat_service_new.model.chat.UserChatsList;
import com.messenger.chat_service_new.model.message.Message;
import com.messenger.chat_service_new.repository.MessageRepository;
import com.messenger.chat_service_new.modelHelper.projectors.LastMessageTimeProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageFetcher {

    private final MessageRepository messageRepository;

    public Mono<List<Message>> fetchMessagesAcrossBuckets(UUID chatId,
                                                          String currentBucket,
                                                          int remaining,
                                                          UUID after,
                                                          UUID before,
                                                          Instant chatCreatedAt) {
        YearMonth bucket = YearMonth.parse(currentBucket);
        YearMonth chatMonth = YearMonth.from(chatCreatedAt.atZone(ZoneOffset.UTC));

        return fetchFromCassandra(chatId, currentBucket, remaining, after, before)
                .flatMap(messages -> {
                    if (messages.size() >= remaining) {
                        return Mono.just(messages);
                    }

                    if (bucket.isBefore(chatMonth)) {
                        return Mono.just(messages);
                    }

                    int newRemaining = remaining - messages.size();
                    YearMonth nextBucket = (after != null) ? bucket.plusMonths(1) : bucket.minusMonths(1);
                    String nextBucketStr = nextBucket.format(DateTimeFormatter.ofPattern("yyyy-MM"));

                    return fetchMessagesAcrossBuckets(chatId, nextBucketStr, newRemaining, after, before, chatCreatedAt)
                            .map(nextMessages -> {
                                List<Message> all = new ArrayList<>(messages);
                                all.addAll(nextMessages);
                                return all;
                            });
                });
    }

    public Mono<Instant> findLastMessageTimeInChat(UserChatsList chat) {

        YearMonth start = YearMonth.from(chat.getCreatedAt().atZone(ZoneOffset.UTC));
        YearMonth current = YearMonth.now(ZoneOffset.UTC);

        return findRecursive(chat.getChatId(), current, start)
                .map(Message::getCreatedAt)
                .defaultIfEmpty(chat.getCreatedAt());
    }

    public Mono<Message> findLastMessageInChat(UserChatsList chat) {

        YearMonth start = YearMonth.from(chat.getCreatedAt().atZone(ZoneOffset.UTC));
        YearMonth current = YearMonth.now(ZoneOffset.UTC);

        return findRecursive(chat.getChatId(), current, start);
    }

    private Mono<Message> findRecursive(UUID chatId, YearMonth current, YearMonth lowerBound) {

        if (current.isBefore(lowerBound)) {
            return Mono.empty();
        }

        String bucket = current.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        return messageRepository
                .findFirstByChatIdAndBucketMonthOrderByMessageIdDesc(chatId, bucket)
                .switchIfEmpty(
                        findRecursive(chatId, current.minusMonths(1), lowerBound)
                );
    }

    public Mono<List<Message>> fetchFromCassandra(UUID chatId, String bucketMonth, int limit, UUID after, UUID before) {
        if (after != null) {
            return messageRepository.findMessagesAfter(chatId, bucketMonth, after, limit)
                    .collectList()
                    .doOnNext(messages -> log.info("Fetched {} messages (after) from Cassandra for chatId: {}", messages.size(), chatId));
        } else if (before != null) {
            return messageRepository.findMessagesBefore(chatId, bucketMonth, before, limit)
                    .collectList()
                    .doOnNext(messages -> log.info("Fetched {} messages (before) from Cassandra for chatId: {}", messages.size(), chatId));
        } else {
            return messageRepository.findLatestMessages(chatId, bucketMonth, limit)
                    .collectList()
                    .doOnNext(messages -> log.info("Fetched {} messages (latest) from Cassandra for chatId: {}", messages.size(), chatId));
        }
    }

    public Mono<List<Message>> fetchAroundMessage(UUID chatId, UUID aroundId, int halfLimit, String bucketMonth) {
        return messageRepository.findByChatIdAndBucketMonthAndMessageId(chatId, bucketMonth, aroundId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found")))
                .flatMap(centerMessage -> {
                    Mono<List<Message>> older = messageRepository.findMessagesBefore(chatId, bucketMonth, aroundId, halfLimit)
                            .collectList();

                    Mono<List<Message>> newer = messageRepository.findMessagesAfter(chatId, bucketMonth, aroundId, halfLimit)
                            .collectList();

                    return Mono.zip(older, newer)
                            .map(tuple -> {
                                List<Message> result = new ArrayList<>(tuple.getT1());
                                Collections.reverse(result); // от старого к новому
                                result.add(centerMessage);
                                result.addAll(tuple.getT2());
                                return result;
                            });
                })
                .flatMap(list -> {
                    if (list.size() < 2 * halfLimit + 1) {
                        int missing = 2 * halfLimit + 1 - list.size();
                        YearMonth prevMonth = YearMonth.parse(bucketMonth).minusMonths(1);
                        String prevBucket = prevMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));

                        return messageRepository.findLatestMessages(chatId, prevBucket, missing)
                                .collectList()
                                .map(extraOld -> {
                                    List<Message> full = new ArrayList<>(extraOld);
                                    full.addAll(list);
                                    return full;
                                });
                    }
                    return Mono.just(list);
                });
    }
}
