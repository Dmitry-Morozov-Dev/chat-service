package com.messenger.chat_service_new.model.chat;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.*;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table("user_chats_list")
public class UserChatsList {

    @PrimaryKeyColumn(name = "user_id", type = PrimaryKeyType.PARTITIONED)
    @CassandraType(type = CassandraType.Name.UUID)
    @JsonProperty("user_id")
    private UUID userId;

    @PrimaryKeyColumn(name = "chat_id", type = PrimaryKeyType.CLUSTERED)
    @CassandraType(type = CassandraType.Name.UUID)
    @JsonProperty("chat_id")
    private UUID chatId;

    @Column("created_at")
    @CassandraType(type = CassandraType.Name.TIMESTAMP)
    @JsonProperty("created_at")
    private Instant createdAt;
}