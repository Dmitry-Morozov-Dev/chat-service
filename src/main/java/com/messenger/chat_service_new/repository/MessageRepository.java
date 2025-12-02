package com.messenger.chat_service_new.repository;

import com.messenger.chat_service_new.model.message.Message;
import com.messenger.chat_service_new.modelHelper.projectors.LastMessageTimeProjection;
import com.messenger.chat_service_new.modelHelper.projectors.MessageIdProjection;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends ReactiveCassandraRepository<Message, UUID> {
    Mono<Message> findFirstByChatIdAndBucketMonthOrderByMessageIdDesc(UUID chatId, String bucketMonth);

    Mono<Message> findByChatIdAndBucketMonthAndMessageId(UUID chatId, String bucketMonth, UUID messageId);

    Flux<Message> findByChatIdAndBucketMonthAndMessageIdIn(UUID chatId, String bucketMonth, List<UUID> messageIds);

    @Query("SELECT message_id FROM messages WHERE chat_id = :chatId AND bucket_month = :bucketMonth AND message_id > :lastRead LIMIT :limit")
    Flux<MessageIdProjection> findMessagesIdsAfterWithLimit(UUID chatId, String bucketMonth, UUID lastRead, int limit);

    @Query("SELECT * FROM messages WHERE chat_id = ?0 AND bucket_month = ?1 AND message_id > ?2 LIMIT ?3")
    Flux<Message> findMessagesAfter(UUID chatId, String bucketMonth, UUID afterMessageId, int limit);

    @Query("SELECT * FROM messages WHERE chat_id = ?0 AND bucket_month = ?1 AND message_id < ?2 ORDER BY message_id DESC LIMIT ?3")
    Flux<Message> findMessagesBefore(UUID chatId, String bucketMonth, UUID beforeMessageId, int limit);

    @Query("SELECT * FROM messages WHERE chat_id = ?0 AND bucket_month = ?1 LIMIT ?2")
    Flux<Message> findLatestMessages(UUID chatId, String bucketMonth, int limit);

}