package com.messenger.chat_service_new.controller;

import com.messenger.chat_service_new.model.chat.Chat;
import com.messenger.chat_service_new.modelHelper.DTO.ChatCreateRequest;
import com.messenger.chat_service_new.modelHelper.DTO.ChatDTO;
import com.messenger.chat_service_new.modelHelper.DTO.UserChatDTO;
import com.messenger.chat_service_new.modelHelper.enums.Role;
import com.messenger.chat_service_new.service.chat.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/chats")
@Slf4j
public class ChatController {

    private final ChatService chatService;

    @Value("${app.pagination.default-limit}")
    private long defaultLimit;

    @Value("${app.pagination.default-offset}")
    private long defaultOffset;

    @Value("${app.pagination.max-limit}")
    private long maxLimit;

    @PostMapping
    public Mono<ResponseEntity<Chat>> createChat(@RequestBody ChatCreateRequest request) {
        log.debug("Controller Test");
        return chatService.createChat(request)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chat creation failed")))
                .doOnError(e -> log.error("Исключение: ", e));
    }

    @PostMapping("/{chat_id}/participants/{user_id}")
    public Mono<ResponseEntity<Void>> addParticipant(@PathVariable("chat_id") UUID chatId,
                                                     @PathVariable("user_id") UUID userId) {
        return chatService.addParticipant(chatId, userId)
                .map(v -> ResponseEntity.noContent().build());
    }

    @DeleteMapping("/{chat_id}/participants/{user_id}")
    public Mono<ResponseEntity<Void>> removeParticipant(@PathVariable("chat_id") UUID chatId,
                                                        @PathVariable("user_id") UUID userId) {
        return chatService.removeParticipant(chatId, userId)
                .map(v -> ResponseEntity.noContent().build());
    }

    @PutMapping("/{chat_id}/participants/{user_id}/role")
    public Mono<ResponseEntity<Void>> updateRole(@PathVariable("chat_id") UUID chatId,
                                                 @PathVariable("user_id") UUID userId,
                                                 @RequestBody Role newRole) {
        return chatService.updateRole(chatId, userId, newRole)
                .map(v -> ResponseEntity.noContent().build());
    }

    @GetMapping("/{chat_id}")
    public Mono<ResponseEntity<ChatDTO>> getChat(@PathVariable("chat_id") UUID chatId) {
        return chatService.getChat(chatId)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @GetMapping
    public Mono<ResponseEntity<PagedModel<EntityModel<UserChatDTO>>>> getChats(
            @RequestParam(required = false) Long offset,
            @RequestParam(required = false) Long limit) {

        long finalOffset = offset != null ? offset : defaultOffset;
        long finalLimit = limit != null ? limit : defaultLimit;

        return chatService.getUserChats(finalOffset, finalLimit)
                .map(chats -> {
                    PagedModel<EntityModel<UserChatDTO>> paged = PagedModel.of(
                            chats.stream().map(EntityModel::of).toList(),
                            new PagedModel.PageMetadata(finalLimit, finalOffset, chats.size())
                    );

                    paged.add(Link.of("/chats?limit=" + finalLimit + "&offset=" + finalOffset, "self"));
                    paged.add(Link.of("/chats?limit=" + finalLimit + "&offset=" + (finalOffset + finalLimit), "next"));

                    return ResponseEntity.ok(paged);
                })
                .doOnError(e -> log.error("Ошибка при получении чатов: ", e))
                .doOnSuccess(e -> log.debug("Чаты получены"));
    }
}
