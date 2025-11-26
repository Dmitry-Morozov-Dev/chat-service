package com.messenger.chat_service_new.service.message;

import com.messenger.chat_service_new.model.message.Message;
import com.messenger.chat_service_new.modelHelper.events.MessageEnvelope;
import com.messenger.chat_service_new.modelElasticsearch.MessageSearchDocument;
import com.messenger.chat_service_new.repository.MessageRepository;
import com.messenger.chat_service_new.elasticRepository.MessageSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageEditService {

    private final MessageRepository messageRepository;
    private final MessageSearchRepository messageSearchRepository;

    public Mono<Void> updateMessage(MessageEnvelope env,
                                    UUID chatId,
                                    UUID senderId,
                                    UUID messageId,
                                    String bucket) {

        return messageRepository
                .findByChatIdAndBucketMonthAndMessageId(chatId, bucket, messageId)
                .switchIfEmpty(Mono.error(new IllegalStateException("Message not found")))
                .flatMap(msg -> verifyAndApplyEdit(msg, senderId, env.getEdit().newContent()))
                .flatMap(messageRepository::save)
                .then();
    }

    private Mono<Message> verifyAndApplyEdit(Message msg, UUID senderId, String newContent) {
        if (!msg.getSenderId().equals(senderId)) {
            return Mono.error(new IllegalStateException("Cannot edit foreign message"));
        }
        msg.setContent(newContent);
        msg.setEdited(true);
        return Mono.just(msg);
    }

    public Mono<Void> updateSearch(MessageEnvelope env,
                                   UUID chatId,
                                   UUID senderId,
                                   UUID messageId) {
        return messageSearchRepository
                .findById(messageId)
                .switchIfEmpty(Mono.fromCallable(() ->
                        MessageSearchDocument.builder()
                                .messageId(messageId)
                                .chatId(chatId)
                                .senderId(senderId)
                                .build()
                ))
                .map(doc -> {
                    doc.setContent(env.getEdit().newContent() != null
                            ? env.getEdit().newContent()
                            : "");
                    return doc;
                })
                .flatMap(messageSearchRepository::save)
                .then();
    }
}
