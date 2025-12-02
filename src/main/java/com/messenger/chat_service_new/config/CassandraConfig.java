package com.messenger.chat_service_new.config;

import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.cassandra.config.AbstractReactiveCassandraConfiguration;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.core.convert.CassandraCustomConversions;
import org.springframework.data.cassandra.repository.config.EnableReactiveCassandraRepositories;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Configuration
@EnableReactiveCassandraRepositories(basePackages = "com.messenger.chat_service_new.repository")
@EntityScan(basePackages = "com.messenger.chat_service_new.model")
public class CassandraConfig extends AbstractReactiveCassandraConfiguration {

    @Value("${spring.cassandra.keyspace-name:chat_keyspace}")
    private String keyspace;

    @Value("${spring.cassandra.contact-points:localhost:9042}")
    private String contactPoints;

    @Value("${spring.cassandra.local-datacenter:datacenter1}")
    private String localDatacenter;

    @Value("${spring.cassandra.request.timeout:30s}")
    private Duration requestTimeout;

    @Value("${spring.cassandra.connection.connect-timeout:30s}")
    private Duration connectTimeout;

    @Override
    protected String getKeyspaceName() {
        return keyspace;
    }

    @Override
    protected String getContactPoints() {
        return contactPoints;
    }

    @Override
    protected String getLocalDataCenter() {
        return localDatacenter;
    }

    @Override
    public SchemaAction getSchemaAction() {
        return SchemaAction.CREATE_IF_NOT_EXISTS;
    }

    @Bean
    public CqlSessionBuilderCustomizer sessionBuilderCustomizer() {
        return builder -> builder.withConfigLoader(DriverConfigLoader.programmaticBuilder()
                .withDuration(DefaultDriverOption.REQUEST_TIMEOUT, requestTimeout)
                .withDuration(DefaultDriverOption.CONNECTION_CONNECT_TIMEOUT, connectTimeout)
                .withDuration(DefaultDriverOption.METADATA_SCHEMA_REQUEST_TIMEOUT, Duration.ofSeconds(60))
                .build());
    }
}