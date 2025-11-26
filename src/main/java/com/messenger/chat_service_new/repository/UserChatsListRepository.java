package com.messenger.chat_service_new.repository;

import com.messenger.chat_service_new.model.chat.UserChatsList;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserChatsListRepository extends ReactiveCassandraRepository<UserChatsList, UUID> {
    Flux<UserChatsList> findByUserId(UUID userId);
    Mono<UserChatsList> findByUserIdAndChatId(UUID userId, UUID chatId);
    Mono<Void> deleteByUserIdAndChatId(UUID userId, UUID chatId);
}