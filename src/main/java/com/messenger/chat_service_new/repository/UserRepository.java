package com.messenger.chat_service_new.repository;

import com.messenger.chat_service_new.model.user.User;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface UserRepository extends ReactiveCassandraRepository<User, UUID> {
    Mono<User> findByUserId(UUID userId);
}
