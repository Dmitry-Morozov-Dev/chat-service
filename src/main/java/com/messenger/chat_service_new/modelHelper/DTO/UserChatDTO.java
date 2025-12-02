package com.messenger.chat_service_new.modelHelper.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserChatDTO {

    @JsonProperty("chat_id")
    private UUID chatId;

    @JsonProperty("chat_type")
    private String chatType;

    @JsonProperty("name")
    private String name;

    @JsonProperty("avatar")
    private String avatar;

    @JsonProperty("unread_messages_count")
    private String unreadMessagesCount;

    @JsonProperty("last_message_time")
    private String lastMessageTime;

    @JsonProperty("last_message_content")
    private String lastMessageContent;
}