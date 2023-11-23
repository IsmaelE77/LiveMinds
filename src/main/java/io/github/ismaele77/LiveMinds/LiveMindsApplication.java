package io.github.ismaele77.LiveMinds;

import io.github.ismaele77.LiveMinds.Enum.UserRole;
import io.github.ismaele77.LiveMinds.Model.AppUser;
import io.github.ismaele77.LiveMinds.Model.Role;
import io.github.ismaele77.LiveMinds.Repository.AppUserRepository;
import io.livekit.server.*;
import livekit.LivekitModels;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.security.crypto.password.PasswordEncoder;
import retrofit2.Call;
import retrofit2.Response;

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
//		RoomServiceClient roomServiceClient = RoomServiceClient.create(
//				"https://isteqra-lectures-1ntumk04.livekit.cloud",
//				"APIeWv9jLhoJCmt",
//				"ASG94HIPg3KAJ7D3AapWbqKuSLIgBdgvH1QhEa5hB9O");

//		List<LivekitModels.ParticipantInfo> users = roomServiceClient.listParticipants("ITE_BAS401_C1").execute().body();
//		var participant = roomServiceClient.getParticipant("ITE_BAS401_C1","ismail_190735").execute().body();
//		var per = LivekitModels.ParticipantPermission.newBuilder()
//						.setCanPublish(false).build();
//		var response = roomServiceClient.updateParticipant("ITE_BAS401_C1",participant.getIdentity() , "ahmed" , "meta" , LivekitModels.ParticipantPermission.newBuilder()
//				.setCanPublish(false).build()).execute().body();
//		System.out.println(response.isInitialized());
		//roomServiceClient.mutePublishedTrack("ITE_BNA_C2",r.getIdentity() , "PA_ZPwmZ9Me9Egv",false).execute();
	}

	@Bean
	public CommandLineRunner demo(AppUserRepository userRepository , PasswordEncoder encoder) {
		return (args) -> {
			AppUser user = userRepository.findById(2L).get();
			user.setPassword(encoder.encode("admin"));
			user.setRole(new Role(3L,"Professor"));
			userRepository.save(user);
			AppUser user2 = userRepository.findById(3L).get();
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


