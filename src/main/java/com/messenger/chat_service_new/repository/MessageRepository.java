package com.messenger.chat_service_new.repository;

import com.messenger.chat_service_new.model.message.Message;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends ReactiveCassandraRepository<Message, UUID> {
    @Query("SELECT created_at FROM messages WHERE chat_id = ?0 ORDER BY message_id DESC LIMIT 1")
    Mono<Instant> findLastMessageTime(UUID chatId);

    Mono<Message> findByChatIdAndBucketMonthAndMessageId(UUID chatId, String bucketMonth, UUID messageId);

    Flux<Message> findByChatIdAndBucketMonthAndMessageIdIn(UUID chatId, String bucketMonth, List<UUID> messageIds);

    @Query("SELECT message_id FROM messages WHERE chat_id = :chatId AND bucket_month = :bucketMonth AND message_id > :lastRead LIMIT :limit")
    Flux<UUID> findMessagesIdsAfterWithLimit(UUID chatId, String bucketMonth, UUID lastRead, int limit);

    @Query("SELECT * FROM messages_by_chat WHERE chat_id = ?0 AND bucket_month = ?1 AND message_id > ?2 LIMIT ?3")
    Flux<Message> findMessagesAfter(UUID chatId, String bucketMonth, UUID afterMessageId, Long limit);

    @Query("SELECT * FROM messages_by_chat WHERE chat_id = ?0 AND bucket_month = ?1 AND message_id < ?2 ORDER BY message_id DESC LIMIT ?3")
    Flux<Message> findMessagesBefore(UUID chatId, String bucketMonth, UUID beforeMessageId, Long limit);

    @Query("SELECT * FROM messages_by_chat WHERE chat_id = ?0 AND bucket_month = ?1 LIMIT ?2")
    Flux<Message> findLatestMessages(UUID chatId, String bucketMonth, Long limit);

}