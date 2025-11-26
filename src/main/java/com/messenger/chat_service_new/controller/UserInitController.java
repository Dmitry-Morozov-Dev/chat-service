package com.messenger.chat_service_new.controller;

import com.messenger.chat_service_new.model.user.User;
import com.messenger.chat_service_new.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

//TODO: КЛАСС ДЛЯ ПРОВЕРКИ ФУНКЦИОНАЛЬНОСТИ
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserInitController {

    private final UserRepository userRepository;

    /**
     * КЛАСС ДЛЯ ПРОВЕРКИ ФУНКЦИОНАЛЬНОСТИ
     */
    @PostMapping("/init")
    public Mono<ResponseEntity<User>> initUser(@RequestHeader("x-user-id") String userIdHeader) {
        log.debug("Получил запрос на инициализацию пользователя");
        UUID userId;
        try {
            userId = UUID.fromString(userIdHeader);
        } catch (IllegalArgumentException e) {
            return Mono.just(ResponseEntity.badRequest().build());
        }

        User user = User.builder()
                .userId(userId)
                .name(userId.toString())
                .avatar(null)
                .build();

        return userRepository.findByUserId(userId)
                .switchIfEmpty(userRepository.save(user))
                .map(ResponseEntity::ok);
    }
}