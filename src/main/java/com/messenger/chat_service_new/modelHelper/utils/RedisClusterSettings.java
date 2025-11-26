package com.messenger.chat_service_new.modelHelper.utils;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties
public class RedisClusterSettings {
    private int connectTimeoutMs = 500;
    private int commandTimeoutMs = 1000;
    private int shutdownTimeoutMs = 100;
    private int maxRedirects = 5;
    private int topologyRefreshPeriodicSec = 10;
    private boolean validateClusterNodeMembership = false;
}