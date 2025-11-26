package com.messenger.chat_service_new.repository;

import com.messenger.chat_service_new.model.participant.ChatParticipant;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface ChatParticipantRepository extends ReactiveCassandraRepository<ChatParticipant, UUID> {
    Mono<ChatParticipant> findByChatIdAndBucketPartitionAndUserId(UUID chatId, Integer bucketPartition, UUID userId);
    Mono<Boolean> existsByChatIdAndBucketPartitionAndUserId(UUID chatId, Integer bucketPartition, UUID userId);
    Mono<Void> deleteByChatIdAndBucketPartitionAndUserId(UUID chatId, Integer bucketPartition, UUID userId);
}