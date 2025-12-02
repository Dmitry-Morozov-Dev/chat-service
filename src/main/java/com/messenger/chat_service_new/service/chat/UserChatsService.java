package com.messenger.chat_service_new.service.chat;

import com.messenger.chat_service_new.repository.UserChatsListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserChatsService {

    private final UserChatsListRepository repository;

    public Mono<List<String>> getUserChatIds(UUID userId) {
        log.debug("Fetching chat IDs for userId={}", userId);
        return repository.findByUserId(userId)
                .map(entity -> entity.getChatId().toString())
                .doOnNext(chatId -> log.debug("Found chatId={}", chatId))
                .collectList()
                .doOnSuccess(list -> log.info("Total {} chats found for userId={}", list.size(), userId))
                .doOnError(e -> log.error("Error fetching chat IDs for userId={}", userId, e));
    }

}
