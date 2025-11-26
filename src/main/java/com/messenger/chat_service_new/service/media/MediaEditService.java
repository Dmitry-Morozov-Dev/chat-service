package com.messenger.chat_service_new.service.media;

import com.messenger.chat_service_new.modelHelper.DTO.MediaDTO;
import com.messenger.chat_service_new.model.media.Media;
import com.messenger.chat_service_new.modelHelper.enums.MediaStatus;
import com.messenger.chat_service_new.repository.MediaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MediaEditService {

    private final MediaRepository mediaRepository;

    public Mono<Void> applyEdit(List<MediaDTO> newMediaDtos,
                                UUID chatId,
                                UUID messageId,
                                UUID senderId,
                                String bucket) {

        Set<UUID> newIds = newMediaDtos.stream()
                .map(MediaDTO::mediaId)
                .collect(Collectors.toSet());

        Mono<Set<UUID>> existingIdsMono = mediaRepository
                .findByChatIdAndBucketMonthAndMessageId(chatId, bucket, messageId)
                .map(Media::getMediaId)
                .collect(Collectors.toSet())
                .defaultIfEmpty(Set.of());

        Mono<Void> deleteRemoved =
                existingIdsMono.flatMapMany(existing ->
                                Flux.fromIterable(existing)
                                        .filter(id -> !newIds.contains(id))
                                        .flatMap(id -> mediaRepository
                                                .deleteByChatIdAndBucketMonthAndMessageIdAndMediaId(
                                                        chatId, bucket, messageId, id
                                                )))
                        .then();

        Mono<Void> saveNew =
                existingIdsMono.flatMapMany(existing ->
                        Flux.fromIterable(newMediaDtos)
                                .filter(dto -> !existing.contains(dto.mediaId()))
                                .map(dto ->
                                        Media.builder()
                                                .chatId(chatId)
                                                .bucketMonth(bucket)
                                                .mediaId(dto.mediaId())
                                                .messageId(messageId)
                                                .senderId(senderId)
                                                .type(dto.type())
                                                .url(dto.url())
                                                .thumbnailUrl(dto.thumbnailUrl())
                                                .createdAt(Instant.now())
                                                .status(MediaStatus.UPLOADED)
                                                .build()
                                )
                                .flatMap(mediaRepository::save)
                ).then();

        return Mono.when(deleteRemoved, saveNew);
    }
}
