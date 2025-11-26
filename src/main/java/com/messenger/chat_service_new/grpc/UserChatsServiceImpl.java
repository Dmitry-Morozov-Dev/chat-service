package com.messenger.chat_service_new.grpc;

import com.messenger.chat_service_new.service.chat.UserChatsService;
import com.messenger.chat_service_new.utils.UserChatsGrpcMapper;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Service;
import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
public class UserChatsServiceImpl extends UserChatsServiceGrpc.UserChatsServiceImplBase {

    private final UserChatsService userChatsDomainService;
    private final UserChatsGrpcMapper mapper;

    @Override
    public void getUserChats(GetUserChatsRequest request,
                             StreamObserver<GetUserChatsResponse> responseObserver) {

        UUID userId = UUID.fromString(request.getUserId());

        userChatsDomainService.getUserChatIds(userId)
                .map(mapper::toGrpcResponse)
                .subscribe(responseObserver::onNext,
                        responseObserver::onError,
                        responseObserver::onCompleted);
    }
}
