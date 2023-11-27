package io.github.ismaele77.LiveMinds;

import io.github.ismaele77.LiveMinds.Configuration.LiveKitConfigProperties;
import io.github.ismaele77.LiveMinds.Model.AppUser;
import io.github.ismaele77.LiveMinds.Repository.AppUserRepository;
import io.livekit.server.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;


@ComponentScan
@SpringBootApplication
@EnableConfigurationProperties(LiveKitConfigProperties.class)
public class LiveMindsApplication {

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
	public RoomServiceClient roomServiceClient(LiveKitConfigProperties liveKit) {
		return RoomServiceClient.create(
				liveKit.host(),
				liveKit.apiKey(),
				liveKit.secret());
	}

	@Bean
	public AccessToken token(LiveKitConfigProperties liveKit) {
		return new AccessToken(liveKit.apiKey(),liveKit.secret());
	}

	@Bean
	public WebhookReceiver webhookReceiver(LiveKitConfigProperties liveKit){
		return new WebhookReceiver(liveKit.apiKey(),liveKit.secret());
	}
}


