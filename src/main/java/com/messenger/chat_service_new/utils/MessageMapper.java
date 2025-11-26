package com.messenger.chat_service_new.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.messenger.chat_service_new.modelHelper.DTO.MessageDTO;
import com.messenger.chat_service_new.model.message.Message;
import com.messenger.chat_service_new.modelHelper.enums.MessageStatus;
import java.util.List;

import com.messenger.chat_service_new.modelElasticsearch.MessageSearchDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageMapper {

    private final ObjectMapper objectMapper;

    public List<MessageDTO> entitiesToDTOs(List<Message> messages) {
        return messages.stream()
                .map(message -> MessageDTO.builder()
                        .chatId(message.getChatId())
                        .messageId(message.getMessageId())
                        .senderId(message.getSenderId())
                        .content(message.getContent())
                        .createdAt(message.getCreatedAt())
                        .status(message.getStatus())
                        .replyToMessageId(message.getReplyToMessageId())
                        .isEdited(message.isEdited())
                        .media(null)
                        .senderName(null)
                        .senderAvatar(null)
                        .build()
                )
                .toList();
    }

    public MessageSearchDocument toSearchDocument(MessageDTO dto) {
        return MessageSearchDocument.builder()
                .messageId(dto.getMessageId())
                .chatId(dto.getChatId())
                .senderId(dto.getSenderId())
                .content(dto.getContent())
                .createdAt(dto.getCreatedAt())
                .build();
    }

    public String toString(Message message) throws JsonProcessingException {
        return objectMapper.writeValueAsString(message);
    }

    public MessageDTO toDTO(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, MessageDTO.class);
    }
}
