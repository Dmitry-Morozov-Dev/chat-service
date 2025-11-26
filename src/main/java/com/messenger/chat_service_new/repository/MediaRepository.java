package com.messenger.chat_service_new.repository;

import com.messenger.chat_service_new.model.media.Media;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.UUID;

public interface MediaRepository extends ReactiveCassandraRepository<Media, UUID> {
    Flux<Media> findByChatIdAndBucketMonthAndMessageId(UUID chatId, String bucketMonth, UUID messageId);
    Mono<Void> deleteByChatIdAndBucketMonthAndMessageIdAndMediaId(UUID chatId, String bucketMonth, UUID messageId, UUID mediaId);
    Flux<Media> findAllByChatIdAndBucketMonthAndMessageIdIn(UUID chatId, String bucketMonth, Collection<UUID> messageIds);

}
