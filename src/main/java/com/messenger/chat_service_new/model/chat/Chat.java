package com.messenger.chat_service_new.model.chat;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.messenger.chat_service_new.modelHelper.enums.ChatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.*;

import java.time.Instant;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table("chats")
public class Chat {

    @PrimaryKeyColumn(name = "chat_id", type = PrimaryKeyType.PARTITIONED)
    @CassandraType(type = CassandraType.Name.UUID)
    @JsonProperty("chat_id")
    private UUID chatId;

    @CassandraType(type = CassandraType.Name.TEXT)
    @JsonProperty("chat_type")
    private ChatType chatType;

    @CassandraType(type = CassandraType.Name.TEXT)
    @JsonProperty("name")
    private String name;

    @Column("avatar")
    @CassandraType(type = CassandraType.Name.TEXT)
    @JsonProperty("avatar")
    private String avatar;

    @Column("created_at")
    @CassandraType(type = CassandraType.Name.TIMESTAMP)
    @JsonProperty("created_at")
    private Instant createdAt;

    @Column("creator_id")
    @CassandraType(type = CassandraType.Name.UUID)
    @JsonProperty("creator_id")
    private UUID creatorId;

    @Column("metadata")
    @CassandraType(type = CassandraType.Name.MAP, typeArguments = {CassandraType.Name.TEXT, CassandraType.Name.TEXT})
    @JsonProperty("metadata")
    private Map<String, String> metadata;
}