package com.messenger.chat_service_new.service.chat;

import com.messenger.chat_service_new.model.chat.Chat;
import com.messenger.chat_service_new.modelHelper.DTO.ChatDTO;
import com.messenger.chat_service_new.modelHelper.DTO.UserChatDTO;
import com.messenger.chat_service_new.modelHelper.DTO.ChatCreateRequest;
import com.messenger.chat_service_new.modelHelper.enums.Role;
import com.messenger.chat_service_new.repository.*;
import com.messenger.chat_service_new.utils.ChatMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    @Value("${chat.unread-limit:50}")
    private int unreadLimit;

    @Value("${chat.max-limit:100}")
    private int maxLimit;

    private final ChatRepository chatRepository;
    private final ChatMapper chatMapper;

    private final ChatCreationService chatCreationService;
    private final ParticipantService participantService;
    private final UserChatsBuilder userChatsBuilder;
    private final SecurityService securityService;

    public Mono<Chat> createChat(ChatCreateRequest request) {
        return securityService.getCurrentUserId()
                .flatMap(creatorId -> chatCreationService.createChat(creatorId, request));
    }

    public Mono<Void> addParticipant(UUID chatId, UUID userId) {
        return securityService.getCurrentUserId()
                .flatMap(currentUserId -> participantService.checkAdminOrModerator(chatId, currentUserId))
                .flatMap(isAllowed -> {
                    if (!Boolean.TRUE.equals(isAllowed)) {
                        return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN,
                                "Only admin or moderator can add participants"));
                    }
                    return participantService.addParticipantInternal(chatId, userId);
                });
    }

    public Mono<Void> removeParticipant(UUID chatId, UUID userId) {
        return securityService.getCurrentUserId()
                .flatMap(currentUserId -> participantService.checkAdminOrModerator(chatId, currentUserId))
                .flatMap(isAllowed -> {
                    if (!Boolean.TRUE.equals(isAllowed)) {
                        return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN,
                                "Only admin or moderator can remove participants"));
                    }
                    return participantService.removeParticipantInternal(chatId, userId);
                });
    }

    public Mono<Void> updateRole(UUID chatId, UUID userId, Role newRole) {
        return securityService.getCurrentUserId()
                .flatMap(currentUserId -> participantService.checkAdmin(chatId, currentUserId))
                .flatMap(isAllowed -> {
                    if (!Boolean.TRUE.equals(isAllowed)) {
                        return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN,
                                "Only admin can change roles"));
                    }
                    return participantService.updateRoleInternal(chatId, userId, newRole);
                });
    }

    public Mono<List<UserChatDTO>> getUserChats(long offset, long limit) {
        if (limit > maxLimit) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Limit max " + maxLimit));
        }
        return securityService.getCurrentUserId()
                .flatMap(userId -> userChatsBuilder.buildUserChatsList(userId, offset, limit, unreadLimit));
    }

    public Mono<ChatDTO> getChat(UUID chatId) {
        return securityService.getCurrentUserId()
                .flatMap(userId -> participantService.checkUserInChat(userId, chatId))
                .flatMap(ok -> chatRepository.findByChatId(chatId)
                        .map(chatMapper::mapToDTO)
                        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat not found"))));
    }
}
