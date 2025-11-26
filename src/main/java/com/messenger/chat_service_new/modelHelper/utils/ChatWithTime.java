package com.messenger.chat_service_new.modelHelper.utils;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ChatWithTime {
    private UUID chatId;
    private Instant lastMessageTime;
}