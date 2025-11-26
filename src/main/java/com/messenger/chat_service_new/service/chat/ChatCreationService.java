package com.messenger.chat_service_new.service.chat;

import com.messenger.chat_service_new.modelHelper.DTO.ChatCreateRequest;
import com.messenger.chat_service_new.model.chat.Chat;
import com.messenger.chat_service_new.modelHelper.enums.ChatType;
import com.messenger.chat_service_new.modelHelper.enums.Role;
import com.messenger.chat_service_new.repository.ChatRepository;
import com.messenger.chat_service_new.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatCreationService {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final ParticipantService participantService;

    public Mono<Chat> createChat(UUID creatorId, ChatCreateRequest request) {
        UUID chatId = UUID.randomUUID();
        Instant now = Instant.now();

        Chat chat = Chat.builder()
                .chatId(chatId)
                .chatType(request.getType())
                .name(request.getName())
                .avatar(request.getAvatar())
                .createdAt(now)
                .creatorId(creatorId)
                .build();

        return chatRepository.save(chat)
                .flatMap(savedChat -> handleChatCreation(savedChat, request, creatorId, now));
    }

    private Mono<Chat> handleChatCreation(Chat savedChat, ChatCreateRequest request, UUID creatorId, Instant now) {
        Mono<Void> creatorParticipant = participantService.saveParticipant(savedChat.getChatId(), creatorId, Role.ADMIN, now).then();

        if (request.getType() == ChatType.PERSONAL) {
            return createPersonalChat(savedChat, request, creatorId, now, creatorParticipant);
        } else {
            return createGroupChat(savedChat, request, creatorId, now, creatorParticipant);
        }
    }

    private Mono<Chat> createPersonalChat(Chat savedChat, ChatCreateRequest request, UUID creatorId, Instant now, Mono<Void> creatorParticipant) {
        if (request.getUserIds().size() != 1) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Personal chat must have exactly one user"));
        }

        UUID otherUserId = request.getUserIds().getFirst();

        return userRepository.findByUserId(otherUserId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Other user not found")))
                .flatMap(otherUser -> {
                    Mono<Void> otherParticipant = participantService.saveParticipant(savedChat.getChatId(), otherUserId, Role.MEMBER, now).then();

                    Mono<Void> creatorUserChat = Mono.when(
                            participantService.saveUserChat(creatorId, savedChat.getChatId(), savedChat.getChatType().name(), otherUser.getName(), otherUser.getAvatar()),
                            participantService.saveUserChatsList(creatorId, savedChat.getChatId())
                    ).then();

                    Mono<Void> otherUserChat = userRepository.findByUserId(creatorId)
                            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Creator user not found")))
                            .flatMap(creatorUser -> Mono.when(
                                    participantService.saveUserChat(otherUserId, savedChat.getChatId(), savedChat.getChatType().name(), creatorUser.getName(), creatorUser.getAvatar()),
                                    participantService.saveUserChatsList(otherUserId, savedChat.getChatId())
                            )).then();

                    return Mono.when(creatorParticipant, otherParticipant, creatorUserChat, otherUserChat)
                            .thenReturn(savedChat);
                });
    }

    private Mono<Chat> createGroupChat(Chat savedChat, ChatCreateRequest request, UUID creatorId, Instant now, Mono<Void> creatorParticipant) {
        Mono<Void> members = Flux.fromIterable(request.getUserIds())
                .flatMap(userId -> Mono.when(
                        participantService.saveParticipant(savedChat.getChatId(), userId, Role.MEMBER, now),
                        participantService.saveUserChat(userId, savedChat.getChatId(), savedChat.getChatType().name(), savedChat.getName(), savedChat.getAvatar()),
                        participantService.saveUserChatsList(userId, savedChat.getChatId())
                ))
                .then();

        Mono<Void> creatorUserChat = Mono.when(
                participantService.saveUserChat(creatorId, savedChat.getChatId(), savedChat.getChatType().name(), savedChat.getName(), savedChat.getAvatar()),
                participantService.saveUserChatsList(creatorId, savedChat.getChatId())
        ).then();

        return Mono.when(creatorParticipant, members, creatorUserChat)
                .thenReturn(savedChat);
    }
}
