package com.messenger.chat_service_new.model.chat;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table("user_chats")
public class UserChatsInfo {

    @PrimaryKeyColumn(name = "user_id", type = PrimaryKeyType.PARTITIONED)
    @CassandraType(type = CassandraType.Name.UUID)
    @JsonProperty("user_id")
    private UUID userId;

    @PrimaryKeyColumn(name = "chat_id", type = PrimaryKeyType.CLUSTERED)
    @CassandraType(type = CassandraType.Name.UUID)
    @JsonProperty("chat_id")
    private UUID chatId;

    @Column("chat_type")
    @CassandraType(type = CassandraType.Name.TEXT)
    @JsonProperty("chat_type")
    private String chatType;

    @Column("name")
    @CassandraType(type = CassandraType.Name.TEXT)
    @JsonProperty("name")
    private String name;

    @Column("avatar")
    @CassandraType(type = CassandraType.Name.TEXT)
    @JsonProperty("avatar")
    private String avatar;

    @Column("lastReadMessage")
    @CassandraType(type = CassandraType.Name.UUID)
    @JsonProperty("lastReadMessage")
    private UUID lastReadMessage;
}