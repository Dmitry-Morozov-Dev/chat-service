package com.messenger.chat_service_new.repository;

import com.messenger.chat_service_new.model.participant.BlockedUserByChat;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlockedUserRepository extends ReactiveCassandraRepository<BlockedUserByChat, String> {
}