package com.messenger.chat_service_new.repository;

import com.messenger.chat_service_new.model.chat.UserChatsInfo;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface UserChatsInfoRepository extends ReactiveCassandraRepository<UserChatsInfo, UUID> {
    @Query("SELECT * FROM user_chats WHERE user_id = ?0 AND chat_id = ?1")
    Mono<UserChatsInfo> findByUserIdAndChatId(UUID userId, UUID chatId);

    Mono<Void> deleteByUserIdAndChatId(UUID userId, UUID chatId);
}