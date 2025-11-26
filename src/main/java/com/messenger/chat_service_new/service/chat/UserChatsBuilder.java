package com.messenger.chat_service_new.service.chat;

import com.messenger.chat_service_new.model.chat.UserChatsInfo;
import com.messenger.chat_service_new.model.chat.UserChatsList;
import com.messenger.chat_service_new.modelHelper.DTO.UserChatDTO;
import com.messenger.chat_service_new.modelHelper.utils.ChatWithTime;
import com.messenger.chat_service_new.repository.MessageRepository;
import com.messenger.chat_service_new.repository.UserChatsInfoRepository;
import com.messenger.chat_service_new.repository.UserChatsListRepository;
import com.messenger.chat_service_new.utils.BucketPartitionCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserChatsBuilder {

    private final MessageRepository messageRepository;
    private final UserChatsInfoRepository userChatsInfoRepository;
    private final UserChatsListRepository userChatsListRepository;

    public Mono<List<UserChatDTO>> buildUserChatsList(UUID userId, long offset, long limit, int unreadLimit) {
        return userChatsListRepository.findByUserId(userId)
                .collectList()
                .flatMap(chatIdsList -> {
                    if (chatIdsList.isEmpty()) {
                        return Mono.just(List.<UserChatDTO>of());
                    }

                    return Flux.fromIterable(chatIdsList)
                            .flatMap(chatList -> messageRepository.findLastMessageTime(chatList.getChatId())
                                    .map(time -> ChatWithTime.builder()
                                            .chatId(chatList.getChatId())
                                            .lastMessageTime(time)
                                            .build()), 10)
                            .collectSortedList(Comparator.comparing(ChatWithTime::getLastMessageTime).reversed())
                            .map(sorted -> {
                                int start = (int) Math.min(offset, sorted.size());
                                int end = (int) Math.min(start + limit, sorted.size());
                                return sorted.subList(start, end);
                            })
                            .flatMapMany(Flux::fromIterable)
                            .concatMap(chatWithTime -> buildUserChatDTO(userId, chatWithTime, unreadLimit))
                            .collectList();
                });
    }

    private Mono<UserChatDTO> buildUserChatDTO(UUID userId, ChatWithTime chatWithTime, int unreadLimit) {
        return Mono.zip(
                userChatsInfoRepository.findByUserIdAndChatId(userId, chatWithTime.getChatId()),
                userChatsListRepository.findByUserIdAndChatId(userId, chatWithTime.getChatId())
        ).flatMap(tuple -> {
            UserChatsInfo chatInfo = tuple.getT1();
            UserChatsList chatList = tuple.getT2();
            UUID lastRead = chatInfo.getLastReadMessage();

            String bucketMonth = BucketPartitionCalculator.getMessageBucketPartition(
                    chatWithTime.getLastMessageTime() != null ? chatWithTime.getLastMessageTime() : Instant.now());

            return messageRepository.findMessagesIdsAfterWithLimit(chatWithTime.getChatId(), bucketMonth, lastRead, unreadLimit)
                    .collectList()
                    .map(messages -> {
                        String unread = messages.size() >= unreadLimit ? unreadLimit + "+" : String.valueOf(messages.size());
                        return UserChatDTO.builder()
                                .chatId(chatInfo.getChatId())
                                .chatType(chatInfo.getChatType())
                                .name(chatInfo.getName())
                                .avatar(chatInfo.getAvatar())
                                .unreadMessagesCount(unread)
                                .lastMessageTime(
                                        chatWithTime.getLastMessageTime() != null
                                                ? chatWithTime.getLastMessageTime().toString()
                                                : null
                                )
                                .build();
                    });
        });
    }
}
