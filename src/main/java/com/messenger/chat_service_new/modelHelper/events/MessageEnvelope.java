package com.messenger.chat_service_new.modelHelper.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.messenger.chat_service_new.modelHelper.DTO.MediaDTO;
import com.messenger.chat_service_new.modelHelper.enums.EventType;
import com.messenger.chat_service_new.modelHelper.enums.MessageStatus;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Value
@Builder
public class MessageEnvelope {
    EventType type;
    String tempId;
    String realId;
    String chatId;
    String senderId;
    String senderName;
    String senderAvatar;
    String receiverId;
    String content;
    List<MediaDTO> mediaDTOS;
    String replyTo;
    Boolean isEdited;
    MessageStatus status;
    Long timestamp;
    Boolean typing;
    OnlineEvent online;
    EditEvent edit;
    DeleteEvent delete;
    String readUpTo;
}