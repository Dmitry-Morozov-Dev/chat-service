package com.messenger.chat_service_new.utils;

import com.messenger.chat_service_new.grpc.GetUserChatsResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserChatsGrpcMapper {

    public GetUserChatsResponse toGrpcResponse(List<String> chatIds) {
        return GetUserChatsResponse.newBuilder()
                .addAllChatIds(chatIds)
                .build();
    }
}
