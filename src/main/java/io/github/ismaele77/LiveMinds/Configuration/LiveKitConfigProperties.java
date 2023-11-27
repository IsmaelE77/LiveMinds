package io.github.ismaele77.LiveMinds.Configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("livekit")
public record LiveKitConfigProperties(String host, String apiKey , String secret) {
}
