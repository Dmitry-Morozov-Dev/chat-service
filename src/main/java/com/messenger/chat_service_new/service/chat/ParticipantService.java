package com.messenger.chat_service_new.service.chat;

import com.messenger.chat_service_new.model.chat.Chat;
import com.messenger.chat_service_new.model.participant.ChatParticipant;
import com.messenger.chat_service_new.model.chat.UserChatsInfo;
import com.messenger.chat_service_new.model.chat.UserChatsList;
import com.messenger.chat_service_new.modelHelper.enums.EventType;
import com.messenger.chat_service_new.modelHelper.enums.Role;
import com.messenger.chat_service_new.modelHelper.events.MessageEnvelope;
import com.messenger.chat_service_new.repository.ChatParticipantRepository;
import com.messenger.chat_service_new.repository.ChatRepository;
import com.messenger.chat_service_new.repository.UserChatsInfoRepository;
import com.messenger.chat_service_new.repository.UserChatsListRepository;
import com.messenger.chat_service_new.service.kafka.KafkaProducerService;
import com.messenger.chat_service_new.utils.BucketPartitionCalculator;
import com.messenger.chat_service_new.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ParticipantService {

    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatRepository chatRepository;
    private final KafkaProducerService kafkaProducerService;
    private final JsonUtils jsonUtils;
    private final UserChatsInfoRepository userChatsInfoRepository;
    private final UserChatsListRepository userChatsListRepository;
    private final BucketPartitionCalculator bucketPartitionCalculator;

    @Value("${chat.default-notifications:default}")
    private String defaultNotifications;

    @Value("${spring.kafka.topic.message.out}")
    private String messageOutTopic;

    public Mono<ChatParticipant> saveParticipant(UUID chatId, UUID userId, Role role, Instant joinedAt) {
        Integer bucketPartition = bucketPartitionCalculator.getChatParticipantBucketPartition(userId);
        ChatParticipant participant = ChatParticipant.builder()
                .chatId(chatId)
                .bucketPartition(bucketPartition)
                .userId(userId)
                .role(role)
                .notifications(defaultNotifications)
                .joinedAt(joinedAt)
                .build();
        return chatParticipantRepository.save(participant)
                .flatMap(saved -> sendParticipateEvent(chatId, userId)
                        .thenReturn(saved));
    }

    private Mono<Void> sendParticipateEvent(UUID chatId, UUID userId) {
        MessageEnvelope envelope = MessageEnvelope.builder()
                .type(EventType.PARTICIPATE)
                .chatId(chatId.toString())
                .receiverId(userId.toString())
                .timestamp(Instant.now().toEpochMilli())
                .build();

        return jsonUtils.toJson(envelope)
                .flatMap(envelopeJson -> kafkaProducerService.send(messageOutTopic,null, envelopeJson, userId.toString()));
    }


    public Mono<UserChatsInfo> saveUserChat(UUID userId, UUID chatId, String chatType, String name, String avatar) {
        UserChatsInfo userChatsInfo = UserChatsInfo.builder()
                .userId(userId)
                .chatId(chatId)
                .chatType(chatType)
                .name(name)
                .avatar(avatar)
                .build();
        return userChatsInfoRepository.save(userChatsInfo);
    }

    public Mono<UserChatsList> saveUserChatsList(UUID userId, Chat chat) {
        UserChatsList userChatsList = UserChatsList.builder()
                .userId(userId)
                .chatId(chat.getChatId())
                .createdAt(chat.getCreatedAt())
                .build();
        return userChatsListRepository.save(userChatsList);
    }

    public Mono<Boolean> checkUserInChat(UUID userId, UUID chatId) {
        Integer bucket = bucketPartitionCalculator.getChatParticipantBucketPartition(userId);
        return chatParticipantRepository.existsByChatIdAndBucketPartitionAndUserId(chatId, bucket, userId)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN,
                                "User is not a participant in this chat"));
                    }
                    return Mono.just(true);
                });
    }

    public Mono<Boolean> checkAdminOrModerator(UUID chatId, UUID userId) {
        Integer bucketPartition = bucketPartitionCalculator.getChatParticipantBucketPartition(userId);
        return chatParticipantRepository.findByChatIdAndBucketPartitionAndUserId(chatId, bucketPartition, userId)
                .map(p -> p.getRole() == Role.ADMIN || p.getRole() == Role.MODERATOR)
                .defaultIfEmpty(false);
    }

    public Mono<Boolean> checkAdmin(UUID chatId, UUID userId) {
        Integer bucketPartition = bucketPartitionCalculator.getChatParticipantBucketPartition(userId);
        return chatParticipantRepository.findByChatIdAndBucketPartitionAndUserId(chatId, bucketPartition, userId)
                .map(p -> p.getRole() == Role.ADMIN)
                .defaultIfEmpty(false);
    }

    public Mono<Void> addParticipantInternal(UUID chatId, UUID userId) {
        Instant now = Instant.now();
        return chatRepository.findByChatId(chatId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat not found")))
                .flatMap(chat -> chatParticipantRepository.existsByChatIdAndBucketPartitionAndUserId(chatId, bucketPartitionCalculator.getChatParticipantBucketPartition(userId), userId)
                        .flatMap(exists -> {
                            if (exists) {
                                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is already a participant"));
                            }

                            Mono<Void> participant = saveParticipant(chatId, userId, Role.MEMBER, now).then();
                            Mono<Void> userChat = Mono.when(
                                    saveUserChat(userId, chatId, chat.getChatType().name(), chat.getName(), chat.getAvatar()),
                                    saveUserChatsList(userId, chat)
                            ).then();

                            return Mono.when(participant, userChat);
                        })
                ).then();
    }

    public Mono<Void> removeParticipantInternal(UUID chatId, UUID userId) {
        Integer bucketPartition = bucketPartitionCalculator.getChatParticipantBucketPartition(userId);
        return chatParticipantRepository.existsByChatIdAndBucketPartitionAndUserId(chatId, bucketPartition, userId)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not a participant"));
                    }
                    Mono<Void> deleteParticipant = chatParticipantRepository.deleteByChatIdAndBucketPartitionAndUserId(chatId, bucketPartition, userId);
                    Mono<Void> deleteUserChat = Mono.when(
                            userChatsInfoRepository.deleteByUserIdAndChatId(userId, chatId),
                            userChatsListRepository.deleteByUserIdAndChatId(userId, chatId)
                    );
                    return Mono.when(deleteParticipant, deleteUserChat);
                });
    }

    public Mono<Void> updateRoleInternal(UUID chatId, UUID userId, Role newRole) {
        Integer bucketPartition = bucketPartitionCalculator.getChatParticipantBucketPartition(userId);
        return chatParticipantRepository.findByChatIdAndBucketPartitionAndUserId(chatId, bucketPartition, userId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Participant not found")))
                .flatMap(participant -> {
                    participant.setRole(newRole);
                    return chatParticipantRepository.save(participant).then();
                });
    }
}
