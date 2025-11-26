package com.messenger.chat_service_new.modelHelper.events;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.messenger.chat_service_new.modelHelper.DTO.MediaDTO;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EditEvent(
        @JsonProperty("new_content") String newContent,
        @JsonProperty("new_media") List<MediaDTO> newMediaDTO
) {}