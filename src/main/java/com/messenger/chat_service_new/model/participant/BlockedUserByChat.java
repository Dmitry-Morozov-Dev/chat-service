package com.messenger.chat_service_new.model.participant;

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

import java.time.Instant;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table("blocked_users_by_chat")
public class BlockedUserByChat {

    @PrimaryKeyColumn(name = "chat_id", type = PrimaryKeyType.PARTITIONED)
    @CassandraType(type = CassandraType.Name.UUID)
    @JsonProperty("chat_id")
    private UUID chatId;

    @PrimaryKeyColumn(name = "bucket_month", type = PrimaryKeyType.PARTITIONED)
    @CassandraType(type = CassandraType.Name.TEXT)
    @JsonProperty("bucket_month")
    private String bucketMonth;

    @PrimaryKeyColumn(name = "user_id", ordinal = 0, type = PrimaryKeyType.CLUSTERED)
    @CassandraType(type = CassandraType.Name.UUID)
    @JsonProperty("user_id")
    private UUID userId;

    @Column("blocked_at")
    @CassandraType(type = CassandraType.Name.TIMESTAMP)
    @JsonProperty("blocked_at")
    private Instant blockedAt;

    @Column("blocked_by")
    @CassandraType(type = CassandraType.Name.UUID)
    @JsonProperty("blocked_by")
    private UUID blockedBy;

    @Column("reason")
    @CassandraType(type = CassandraType.Name.TEXT)
    @JsonProperty("reason")
    private String reason;
}