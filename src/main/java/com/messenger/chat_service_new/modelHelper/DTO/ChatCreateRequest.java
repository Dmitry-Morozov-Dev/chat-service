package com.messenger.chat_service_new.modelHelper.DTO;

import com.messenger.chat_service_new.modelHelper.enums.ChatType;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ChatCreateRequest {
    private ChatType type;
    private String name;
    private String avatar;
    private List<UUID> userIds;
}