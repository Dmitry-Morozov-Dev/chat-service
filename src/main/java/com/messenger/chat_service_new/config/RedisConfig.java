package com.messenger.chat_service_new.config;

import com.messenger.chat_service_new.modelHelper.utils.RedisClusterSettings;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisConfig {

    @Bean
    @ConfigurationProperties(prefix = "redis.cache.config")
    public RedisClusterSettings cacheSettings() {
        return new RedisClusterSettings();
    }

    @Primary
    @Bean("cacheRedisConnectionFactory")
    public ReactiveRedisConnectionFactory cacheRedisConnectionFactory(
            @Value("${redis.cache.host:redis-cache}") String host,
            @Value("${redis.cache.port:6379}") int port,
            RedisClusterSettings settings) {

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);

        SocketOptions socketOptions = SocketOptions.builder()
                .connectTimeout(Duration.ofMillis(settings.getConnectTimeoutMs()))
                .keepAlive(true)
                .tcpNoDelay(true)
                .build();

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofMillis(settings.getCommandTimeoutMs()))
                .shutdownTimeout(Duration.ofMillis(settings.getShutdownTimeoutMs()))
                .clientOptions(ClientOptions.builder()
                        .socketOptions(socketOptions)
                        .build())
                .build();

        return new LettuceConnectionFactory(config, clientConfig);
    }

    @Bean("cacheReactiveRedisTemplate")
    public ReactiveRedisTemplate<String, String> cacheRedisTemplate(
            @Qualifier("cacheRedisConnectionFactory") ReactiveRedisConnectionFactory factory) {

        var serializationContext = RedisSerializationContext
                .<String, String>newSerializationContext(new StringRedisSerializer())
                .build();

        return new ReactiveRedisTemplate<>(factory, serializationContext);
    }
}