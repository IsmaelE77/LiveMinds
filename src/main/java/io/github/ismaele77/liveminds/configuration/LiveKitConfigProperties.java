package io.github.ismaele77.liveminds.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("livekit")
public record LiveKitConfigProperties(String host, String apiKey, String secret) {
}
