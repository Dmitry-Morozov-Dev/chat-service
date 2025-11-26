package com.messenger.chat_service_new.service.message;

import com.messenger.chat_service_new.modelHelper.DTO.MediaDTO;
import com.messenger.chat_service_new.model.media.Media;
import com.messenger.chat_service_new.model.message.Message;
import com.messenger.chat_service_new.modelHelper.enums.MediaStatus;
import com.messenger.chat_service_new.modelHelper.enums.MessageStatus;
import com.messenger.chat_service_new.modelHelper.events.MessageEnvelope;
import com.messenger.chat_service_new.modelElasticsearch.MessageSearchDocument;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

@Component
public class MessageBuilder {

    public Message buildMessage(MessageEnvelope env, UUID chatId, UUID senderId,
                                UUID messageId, String bucket) {
        return Message.builder()
                .messageId(messageId)
                .chatId(chatId)
                .bucketMonth(bucket)
                .senderId(senderId)
                .content(env.getContent())
                .replyToMessageId(env.getReplyTo() != null
                        ? UUID.fromString(env.getReplyTo())
                        : null)
                .createdAt(Instant.ofEpochMilli(env.getTimestamp()))
                .status(MessageStatus.SENT)
                .isEdited(false)
                .build();
    }

    public Media buildMedia(MediaDTO dto, UUID chatId, UUID senderId,
                            UUID messageId, String bucket, MessageEnvelope env) {
        return Media.builder()
                .chatId(chatId)
                .bucketMonth(bucket)
                .mediaId(dto.mediaId())
                .messageId(messageId)
                .senderId(senderId)
                .type(dto.type())
                .url(dto.url())
                .thumbnailUrl(dto.thumbnailUrl())
                .createdAt(Instant.ofEpochMilli(env.getTimestamp()))
                .status(MediaStatus.UPLOADED)
                .build();
    }

    public MessageSearchDocument buildSearch(MessageEnvelope env,
                                             UUID chatId,
                                             UUID senderId,
                                             UUID messageId) {
        return MessageSearchDocument.builder()
                .messageId(messageId)
                .chatId(chatId)
                .senderId(senderId)
                .content(env.getContent() != null ? env.getContent() : "")
                .createdAt(Instant.ofEpochMilli(env.getTimestamp()))
                .build();
    }
}
