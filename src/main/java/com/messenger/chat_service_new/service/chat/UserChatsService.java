package com.messenger.chat_service_new.service.chat;

import com.messenger.chat_service_new.repository.UserChatsListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserChatsService {

    private final UserChatsListRepository repository;

    public Mono<List<String>> getUserChatIds(UUID userId) {
        return repository.findByUserId(userId)
                .map(entity -> entity.getChatId().toString())
                .collectList();
    }
}
