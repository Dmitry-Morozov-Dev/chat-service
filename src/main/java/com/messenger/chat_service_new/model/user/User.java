package com.messenger.chat_service_new.model.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table("users")
public class User {

    @PrimaryKeyColumn(name = "user_id", type = PrimaryKeyType.PARTITIONED)
    @CassandraType(type = CassandraType.Name.UUID)
    @JsonProperty("user_id")
    private UUID userId;

    @Column("name")
    @CassandraType(type = CassandraType.Name.TEXT)
    @JsonProperty("name")
    private String name;

    @Column("avatar")
    @CassandraType(type = CassandraType.Name.TEXT)
    @JsonProperty("avatar")
    private String avatar;
}