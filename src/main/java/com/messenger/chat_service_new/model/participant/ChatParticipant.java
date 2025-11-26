package com.messenger.chat_service_new.model.participant;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.messenger.chat_service_new.modelHelper.enums.Role;
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
@Table("chat_participants")
public class ChatParticipant {

    @PrimaryKeyColumn(name = "chat_id", type = PrimaryKeyType.PARTITIONED)
    @CassandraType(type = CassandraType.Name.UUID)
    @JsonProperty("chat_id")
    private UUID chatId;

    @PrimaryKeyColumn(name = "bucket_partition", type = PrimaryKeyType.PARTITIONED)
    @CassandraType(type = CassandraType.Name.INT)
    @JsonProperty("bucket_partition")
    private Integer bucketPartition;

    @PrimaryKeyColumn(name = "user_id", type = PrimaryKeyType.CLUSTERED)
    @CassandraType(type = CassandraType.Name.UUID)
    @JsonProperty("user_id")
    private UUID userId;

    @Column("role")
    @CassandraType(type = CassandraType.Name.TEXT)
    @JsonProperty("role")
    private Role role;

    @Column("notifications")
    @CassandraType(type = CassandraType.Name.TEXT)
    @JsonProperty("notifications")
    private String notifications;

    @Column("joined_at")
    @CassandraType(type = CassandraType.Name.TIMESTAMP)
    @JsonProperty("joined_at")
    private Instant joinedAt;
}