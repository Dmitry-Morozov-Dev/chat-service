package com.messenger.chat_service_new.modelHelper.projectors;

import java.time.Instant;

public interface LastMessageTimeProjection {
    Instant getCreatedAt();
}
