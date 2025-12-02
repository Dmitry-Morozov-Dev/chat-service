package com.messenger.chat_service_new.grpc;

import com.messenger.chat_service_new.service.chat.UserChatsService;
import com.messenger.chat_service_new.utils.UserChatsGrpcMapper;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class UserChatsServiceImpl extends UserChatsServiceGrpc.UserChatsServiceImplBase {

    private final UserChatsService userChatsDomainService;
    private final UserChatsGrpcMapper mapper;

    @Override
    public void getUserChats(GetUserChatsRequest request,
                             StreamObserver<GetUserChatsResponse> responseObserver) {

        log.info("Received getUserChats request for userId={}", request.getUserId());

        UUID userId;
        try {
            userId = UUID.fromString(request.getUserId());
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format: {}", request.getUserId(), e);
            responseObserver.onError(e);
            return;
        }

        userChatsDomainService.getUserChatIds(userId)
                .doOnNext(list -> log.info("Found {} chats for userId={}", list.size(), userId))
                .doOnError(e -> log.error("Error while fetching chats for userId={}", userId, e))
                .doOnTerminate(() -> log.info("Completed processing getUserChats for userId={}", userId))
                .map(mapper::toGrpcResponse)
                .subscribe(responseObserver::onNext,
                        responseObserver::onError,
                        responseObserver::onCompleted);
    }
}
