package com.messenger.chat_service_new.elasticRepository;

import com.messenger.chat_service_new.modelElasticsearch.MessageSearchDocument;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface MessageSearchRepository extends ReactiveElasticsearchRepository<MessageSearchDocument, UUID> {
    Flux<MessageSearchDocument> findByChatIdAndContentContaining(UUID chatId, String query);
}
