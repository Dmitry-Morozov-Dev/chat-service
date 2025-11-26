package com.messenger.chat_service_new.config;

import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchClient;
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.springframework.data.elasticsearch.repository.config.EnableReactiveElasticsearchRepositories;

import java.time.Duration;

@Configuration
@EnableReactiveElasticsearchRepositories(basePackages = "com.messenger.chat_service_new.elasticRepository")
public class ElasticsearchConfig {

    @Value("${spring.data.elasticsearch.uris:http://localhost:9200}")
    private String url;

    @Value("${spring.data.elasticsearch.username:}")
    private String username;

    @Value("${spring.data.elasticsearch.password:}")
    private String password;

    @Value("${spring.data.elasticsearch.connection-timeout:5s}")
    private Duration connectTimeout;

    @Value("${spring.data.elasticsearch.socket-timeout:30s}")
    private Duration socketTimeout;

    @Bean
    public RestClient restClient() {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        if (!username.isBlank()) {
            credentialsProvider.setCredentials(
                    AuthScope.ANY,
                    new UsernamePasswordCredentials(username, password)
            );
        }

        return RestClient.builder(HttpHost.create(url))
                .setRequestConfigCallback(rc -> rc
                        .setConnectTimeout((int) connectTimeout.toMillis())
                        .setSocketTimeout((int) socketTimeout.toMillis()))
                .setHttpClientConfigCallback(hc ->
                        hc.setDefaultCredentialsProvider(credentialsProvider))
                .build();
    }

    @Bean
    public ElasticsearchTransport elasticsearchTransport(RestClient restClient) {
        return new RestClientTransport(restClient, new JacksonJsonpMapper());
    }

    @Bean
    public ReactiveElasticsearchClient reactiveElasticsearchClient(
            ElasticsearchTransport transport
    ) {
        return new ReactiveElasticsearchClient(transport);
    }

    @Bean(name = "reactiveElasticsearchTemplate")
    public ReactiveElasticsearchOperations reactiveElasticsearchOperations(
            ReactiveElasticsearchClient client,
            ElasticsearchConverter converter
    ) {
        return new ReactiveElasticsearchTemplate(client, converter);
    }
}