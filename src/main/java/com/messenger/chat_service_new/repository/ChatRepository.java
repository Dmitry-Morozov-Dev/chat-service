package com.messenger.chat_service_new.repository;

import com.messenger.chat_service_new.model.chat.Chat;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface ChatRepository extends ReactiveCassandraRepository<Chat, String> {
    Mono<Chat> findByChatId(UUID chatId);
}