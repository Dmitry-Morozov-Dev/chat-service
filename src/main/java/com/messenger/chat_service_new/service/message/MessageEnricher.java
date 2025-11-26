package com.messenger.chat_service_new.service.message;

import com.messenger.chat_service_new.modelHelper.DTO.MediaDTO;
import com.messenger.chat_service_new.modelHelper.DTO.MessageDTO;
import com.messenger.chat_service_new.model.media.Media;
import com.messenger.chat_service_new.model.user.User;
import com.messenger.chat_service_new.repository.MediaRepository;
import com.messenger.chat_service_new.repository.UserRepository;
import com.messenger.chat_service_new.utils.BucketPartitionCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MessageEnricher {

    private final MediaRepository mediaRepository;
    private final UserRepository userRepository;

    public Mono<List<MessageDTO>> enrichWithMedia(List<MessageDTO> messages) {
        if (messages == null || messages.isEmpty()) {
            return Mono.just(List.of());
        }

        Map<String, List<MessageDTO>> messagesByBucket = messages.stream()
                .collect(Collectors.groupingBy(msg ->
                        BucketPartitionCalculator.getMessageBucketPartition(msg.getCreatedAt())
                ));

        return Flux.fromIterable(messagesByBucket.entrySet())
                .flatMap(entry -> {
                    String bucketMonth = entry.getKey();
                    List<MessageDTO> msgsInBucket = entry.getValue();
                    List<UUID> messageIds = msgsInBucket.stream()
                            .map(MessageDTO::getMessageId)
                            .toList();

                    UUID chatId = msgsInBucket.get(0).getChatId();

                    return mediaRepository.findAllByChatIdAndBucketMonthAndMessageIdIn(chatId, bucketMonth, messageIds)
                            .collectList()
                            .map(mediaList -> {
                                Map<UUID, List<Media>> mediaByMessageId = mediaList.stream()
                                        .collect(Collectors.groupingBy(Media::getMessageId));

                                msgsInBucket.forEach(msg -> {
                                    List<Media> medias = mediaByMessageId.get(msg.getMessageId());
                                    if (medias != null && !medias.isEmpty()) {
                                        Map<String, MediaDTO> mediaMap = medias.stream()
                                                .collect(Collectors.toMap(
                                                        media -> media.getMediaId().toString(),
                                                        media -> MediaDTO.builder()
                                                                .mediaId(media.getMediaId())
                                                                .type(media.getType())
                                                                .url(media.getUrl())
                                                                .thumbnailUrl(media.getThumbnailUrl())
                                                                .build(),
                                                        (a, b) -> a
                                                ));
                                        msg.setMedia(mediaMap);
                                    }
                                });
                                return msgsInBucket;
                            });
                })
                .collectList()
                .map(lists -> lists.stream()
                        .flatMap(List::stream)
                        .toList());
    }

    public Mono<List<MessageDTO>> enrichWithUser(List<MessageDTO> messages) {
        if (messages == null || messages.isEmpty()) {
            return Mono.just(List.of());
        }

        Set<UUID> senderIds = messages.stream()
                .map(MessageDTO::getSenderId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        return Flux.fromIterable(senderIds)
                .flatMap(userRepository::findByUserId)
                .collectMap(User::getUserId, user -> user)
                .map(userById -> messages.stream()
                        .peek(msg -> {
                            User user = userById.get(msg.getSenderId());
                            if (user != null) {
                                msg.setSenderName(user.getName());
                                msg.setSenderAvatar(user.getAvatar());
                            }
                        })
                        .toList()
                );
    }
}
