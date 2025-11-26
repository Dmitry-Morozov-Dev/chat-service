package com.messenger.chat_service_new.service.message;

import com.messenger.chat_service_new.model.message.Message;
import com.messenger.chat_service_new.repository.MediaRepository;
import com.messenger.chat_service_new.repository.MessageRepository;
import com.messenger.chat_service_new.elasticRepository.MessageSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageDeleteService {

    private final MessageRepository messageRepository;
    private final MediaRepository mediaRepository;
    private final MessageSearchRepository messageSearchRepository;

    public Mono<Void> delete(UUID chatId,
                             UUID senderId,
                             UUID messageId,
                             String bucket) {

        Mono<Void> deleteMessage =
                messageRepository
                        .findByChatIdAndBucketMonthAndMessageId(chatId, bucket, messageId)
                        .switchIfEmpty(Mono.error(new IllegalStateException("Message not found")))
                        .flatMap(msg -> verifyOwnership(msg, senderId))
                        .flatMap(messageRepository::delete)
                        .then();

        Mono<Void> deleteMedia =
                mediaRepository
                        .findByChatIdAndBucketMonthAndMessageId(chatId, bucket, messageId)
                        .flatMap(mediaRepository::delete)
                        .then();

        Mono<Void> deleteSearch =
                messageSearchRepository
                        .deleteById(messageId)
                        .onErrorResume(e -> Mono.empty());

        return Mono.when(deleteMessage, deleteMedia, deleteSearch);
    }

    private Mono<Message> verifyOwnership(Message msg, UUID senderId) {
        if (!msg.getSenderId().equals(senderId)) {
            return Mono.error(new IllegalStateException("Cannot delete foreign message"));
        }
        return Mono.just(msg);
    }
}
