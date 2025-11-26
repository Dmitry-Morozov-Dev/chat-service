package com.messenger.chat_service_new.modelHelper.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.messenger.chat_service_new.modelHelper.enums.MessageStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {

    @JsonProperty("chat_id")
    private UUID chatId;

    @JsonProperty("message_id")
    private UUID messageId;

    @JsonProperty("sender_id")
    private UUID senderId;

    @JsonProperty("sender_name")
    private String senderName;

    @JsonProperty("sender_avatar")
    private String senderAvatar;

    @JsonProperty("content")
    private String content;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("status")
    private MessageStatus status;

    @JsonProperty("media")
    private Map<String, MediaDTO> media;

    @JsonProperty("replyToMessageId")
    private UUID replyToMessageId;

    @JsonProperty("isEdited")
    private boolean isEdited;
}