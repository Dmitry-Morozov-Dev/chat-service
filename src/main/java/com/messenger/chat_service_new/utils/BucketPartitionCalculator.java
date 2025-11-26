package com.messenger.chat_service_new.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class BucketPartitionCalculator {

    @Value("${app.chat_participant_buckets_amount:1}")
    private Integer chatParticipantBucketAmount;

    public Integer getChatParticipantBucketPartition(UUID senderId){
        int hash = senderId.hashCode();
        return Math.floorMod(hash, chatParticipantBucketAmount);
    }

    public static String getMessageBucketPartition(Instant dateTime){
        return YearMonth.from(dateTime.atZone(ZoneOffset.UTC)).format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }
}
