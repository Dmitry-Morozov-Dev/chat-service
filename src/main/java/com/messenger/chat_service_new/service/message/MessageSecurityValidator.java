package com.messenger.chat_service_new.service.message;

import com.messenger.chat_service_new.repository.ChatParticipantRepository;
import com.messenger.chat_service_new.utils.BucketPartitionCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MessageSecurityValidator {

    private final ChatParticipantRepository chatParticipantRepository;
    private final BucketPartitionCalculator bucketPartitionCalculator;

    public Mono<UUID> ensureParticipant(UUID chatId) {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication().getName())
                .map(UUID::fromString)
                .flatMap(userId -> {
                    Integer bucket = bucketPartitionCalculator.getChatParticipantBucketPartition(userId);
                    return chatParticipantRepository.existsByChatIdAndBucketPartitionAndUserId(chatId, bucket, userId)
                            .flatMap(exists -> {
                                if (!exists) {
                                    return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not a participant in this chat"));
                                }
                                return Mono.just(userId);
                            });
                });
    }
}
