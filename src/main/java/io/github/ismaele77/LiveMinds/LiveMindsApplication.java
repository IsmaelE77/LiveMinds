package io.github.ismaele77.LiveMinds;

import io.livekit.server.*;
import livekit.LivekitModels;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;

@ComponentScan
@SpringBootApplication
public class LiveMindsApplication {
	public static void main(String[] args) throws IOException {
		SpringApplication.run(LiveMindsApplication.class, args);
	}

	@Bean
	public RoomServiceClient roomServiceClient() {
		return RoomServiceClient.create(
				"http://localhost:7880/",
				"devkey",
				"secret");
	}

	@Bean
	public AccessToken token() {
		return new AccessToken("devkey", "secret");
	}

	@Bean
	public WebhookReceiver webhookReceiver(){
		return new WebhookReceiver("devkey","secret");
	}
}


