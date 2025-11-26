package com.messenger.chat_service_new.model.media;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.messenger.chat_service_new.modelHelper.enums.MediaStatus;
import com.messenger.chat_service_new.modelHelper.enums.MediaType;
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
@Table("media")
public class Media {

    @PrimaryKeyColumn(name = "chat_id", type = PrimaryKeyType.PARTITIONED)
    @CassandraType(type = CassandraType.Name.UUID)
    @JsonProperty("chat_id")
    private UUID chatId;

    @PrimaryKeyColumn(name = "bucket_month", type = PrimaryKeyType.PARTITIONED)
    @CassandraType(type = CassandraType.Name.TEXT)
    @JsonProperty("bucket_month")
    private String bucketMonth;

    @PrimaryKeyColumn(name = "media_id", ordinal = 1, ordering = Ordering.DESCENDING)
    @CassandraType(type = CassandraType.Name.TIMEUUID)
    @JsonProperty("media_id")
    private UUID mediaId;

    @PrimaryKeyColumn(name = "message_id", ordinal = 0, ordering = Ordering.DESCENDING)
    @CassandraType(type = CassandraType.Name.TIMEUUID)
    @JsonProperty("message_id")
    private UUID messageId;

    @Column("sender_id")
    @CassandraType(type = CassandraType.Name.UUID)
    @JsonProperty("sender_id")
    private UUID senderId;

    @Column("type")
    @CassandraType(type = CassandraType.Name.TEXT)
    @JsonProperty("type")
    private MediaType type;

    @Column("size")
    @CassandraType(type = CassandraType.Name.BIGINT)
    @JsonProperty("size")
    private long size;

    @Column("hash")
    @CassandraType(type = CassandraType.Name.TEXT)
    @JsonProperty("hash")
    private String hash;

    @Column("url")
    @CassandraType(type = CassandraType.Name.TEXT)
    @JsonProperty("url")
    private String url;

    @Column("thumbnail_url")
    @CassandraType(type = CassandraType.Name.TEXT)
    @JsonProperty("thumbnail_url")
    private String thumbnailUrl;

    @Column("created_at")
    @CassandraType(type = CassandraType.Name.TIMESTAMP)
    @JsonProperty("created_at")
    private Instant createdAt;

    @Column("status")
    @CassandraType(type = CassandraType.Name.TEXT)
    @JsonProperty("status")
    private MediaStatus status;

    @Column("metadata")
    @CassandraType(type = CassandraType.Name.MAP, typeArguments = {CassandraType.Name.TEXT, CassandraType.Name.TEXT})
    @JsonProperty("metadata")
    private Map<String, String> metadata;
}