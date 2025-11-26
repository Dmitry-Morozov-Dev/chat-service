package com.messenger.chat_service_new.modelHelper.events;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DeleteEvent(
        @JsonProperty("for_everyone") boolean delete
) {}