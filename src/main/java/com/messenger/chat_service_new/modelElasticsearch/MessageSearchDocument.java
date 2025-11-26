package com.messenger.chat_service_new.modelElasticsearch;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import java.time.Instant;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "messages")
public class MessageSearchDocument {

    @Id
    @Field(type = FieldType.Keyword)
    private UUID messageId;

    @Field(type = FieldType.Keyword)
    private UUID chatId;

    @Field(type = FieldType.Keyword)
    private UUID senderId;

    //TODO: Добавить анализаторы
    @Field(type = FieldType.Text, analyzer = "russian")
    private String content;

    @Field(type = FieldType.Date)
    private Instant createdAt;
}
