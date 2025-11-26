package com.messenger.chat_service_new.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.messenger.chat_service_new.model.chat.Chat;
import com.messenger.chat_service_new.modelHelper.DTO.ChatDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatMapper {

    private final ObjectMapper objectMapper;

    public ChatDTO mapToDTO(Chat chat) {
        return ChatDTO.builder()
                .chatId(chat.getChatId())
                .chatType(chat.getChatType())
                .name(chat.getName())
                .avatar(chat.getAvatar())
                .createdAt(chat.getCreatedAt())
                .creatorId(chat.getCreatorId())
                .build();
    }
}
