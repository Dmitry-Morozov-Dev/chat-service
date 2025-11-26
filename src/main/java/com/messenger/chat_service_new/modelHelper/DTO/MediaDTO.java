package com.messenger.chat_service_new.modelHelper.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.messenger.chat_service_new.modelHelper.enums.MediaType;
import lombok.Builder;

import java.util.UUID;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MediaDTO(
        @JsonProperty("media_id") UUID mediaId,
        @JsonProperty("type") MediaType type,
        @JsonProperty("url") String url,
        @JsonProperty("thumbnail_url") String thumbnailUrl
) {}
