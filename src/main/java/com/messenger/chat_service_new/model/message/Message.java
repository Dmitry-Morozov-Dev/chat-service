package com.messenger.chat_service_new.model.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.messenger.chat_service_new.modelHelper.enums.MessageStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table("messages")
public class Message {

    @PrimaryKeyColumn(name = "chat_id", type = PrimaryKeyType.PARTITIONED)
    @CassandraType(type = CassandraType.Name.UUID)
    @JsonProperty("chat_id")
    private UUID chatId;

    @PrimaryKeyColumn(name = "bucket_month", type = PrimaryKeyType.PARTITIONED)
    @CassandraType(type = CassandraType.Name.TEXT)
    @JsonProperty("bucket_month")
    private String bucketMonth;

    @PrimaryKeyColumn(name = "message_id", ordinal = 0, ordering = Ordering.DESCENDING)
    @CassandraType(type = CassandraType.Name.TIMEUUID)
    @JsonProperty("message_id")
    private UUID messageId;

    @Column("sender_id")
    @CassandraType(type = CassandraType.Name.UUID)
    @JsonProperty("sender_id")
    private UUID senderId;

    @Column("content")
    @CassandraType(type = CassandraType.Name.TEXT)
    @JsonProperty("content")
    private String content;

    @Column("created_at")
    @CassandraType(type = CassandraType.Name.TIMESTAMP)
    @JsonProperty("created_at")
    private Instant createdAt;

    @Column("status")
    @CassandraType(type = CassandraType.Name.TEXT)
    @JsonProperty("status")
    private MessageStatus status;

    @Column("metadata")
    @CassandraType(type = CassandraType.Name.MAP, typeArguments = {CassandraType.Name.TEXT, CassandraType.Name.TEXT})
    @JsonProperty("metadata")
    private Map<String, String> metadata;

    @Column("replyToMessageId")
    @CassandraType(type = CassandraType.Name.TIMEUUID)
    @JsonProperty("replyToMessageId")
    private UUID replyToMessageId;

    @Column("isEdited")
    @CassandraType(type = CassandraType.Name.BOOLEAN)
    @JsonProperty("isEdited")
    private boolean isEdited;
}