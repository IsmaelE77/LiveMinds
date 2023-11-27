package io.github.ismaele77.LiveMinds;

import io.github.ismaele77.LiveMinds.Model.AppUser;
import io.github.ismaele77.LiveMinds.Model.Role;
import io.github.ismaele77.LiveMinds.Repository.AppUserRepository;
import io.livekit.server.*;
import livekit.LivekitModels;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.crypto.password.PasswordEncoder;
import retrofit2.Call;

import java.io.IOException;
import java.util.List;


@ComponentScan
@SpringBootApplication
public class LiveMindsApplication {

	private final String HOST = "http://localhost:7880";
	private final String APIKEY = "devkey";
	private final String SECRET = "secret";

	public static void main(String[] args) throws IOException {
		SpringApplication.run(LiveMindsApplication.class, args);
	}

	@Bean
	public CommandLineRunner demo(AppUserRepository userRepository , PasswordEncoder encoder) {
		return (args) -> {
			AppUser user = userRepository.findByUserName("admin").get();
			user.setPassword(encoder.encode("admin"));
			userRepository.save(user);
			AppUser user2 = userRepository.findByUserName("ismail_190735").get();
			user2.setPassword(encoder.encode("2134990"));
			userRepository.save(user2);
		};
	}

	@Bean
	public RoomServiceClient roomServiceClient() {
		return RoomServiceClient.create(
				HOST,
				APIKEY,
				SECRET);
	}

	@Bean
	public AccessToken token() {
		return new AccessToken(APIKEY, SECRET);
	}

	@Bean
	public WebhookReceiver webhookReceiver(){
		return new WebhookReceiver(APIKEY,SECRET);
	}
}


