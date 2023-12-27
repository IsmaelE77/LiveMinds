package io.github.ismaele77.liveminds.service;

import io.github.ismaele77.liveminds.dto.response.TokenResponse;
import io.github.ismaele77.liveminds.exception.LiveKitException;
import io.github.ismaele77.liveminds.exception.RoomCreationException;
import io.github.ismaele77.liveminds.model.AppUser;
import io.github.ismaele77.liveminds.model.Room;
import io.livekit.server.*;
import livekit.LivekitModels;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import retrofit2.Call;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class LiveKitService {


    private final RoomServiceClient roomServiceClient;
    private final AccessToken accessToken;
    private final static int EMPTY_TIMEOUT = 3 * 60 * 60;
    private static final int TOKEN_TIME_OUT = 2 * 60;

    //private final EgressServiceClient egressServiceClient;


    public void createRoom(Room room) {
        int roomTime = getRoomTime(room);
        Call<LivekitModels.Room> call = roomServiceClient.createRoom(room.getName(), roomTime);
        try {
            call.execute();
            log.info("Room created successfully. Name: {}", room.getName());
        } catch (IOException e) {
            log.error("Error creating room", e);
            throw new RoomCreationException();
        }
    }

    public void deleteRoom(String name) {
        roomServiceClient.deleteRoom(name);
        log.info("Room deleted successfully. Name: {}", name);
    }

    @Transactional
    public void updateRoom(Room room) {
        deleteRoom(room.getName());
        createRoom(room);
        log.info("Room updated successfully. Name: {}", room.getName());
    }

    public List<LivekitModels.ParticipantInfo> getParticipantList(String roomName) {
        try {
            List<LivekitModels.ParticipantInfo> participantList = roomServiceClient.listParticipants(roomName).execute().body();
            log.info("Retrieved participant list successfully for room: {}", roomName);
            return participantList;
        } catch (IOException e) {
            log.error("Error getting participant list for room: {}", roomName, e);
            throw new LiveKitException("Get participant list");
        }
    }

    public void changePublishPermission(String roomName, String participantIdentity, boolean canPublish) {
        try {
            LivekitModels.ParticipantInfo participant = roomServiceClient.getParticipant(roomName, participantIdentity).execute().body();
            LivekitModels.ParticipantPermission permission = LivekitModels.ParticipantPermission.newBuilder()
                    .setCanPublish(false).build();
            var response = roomServiceClient.updateParticipant(roomName, participant.getIdentity(), participant.getName(),
                    participant.getMetadata(), LivekitModels.ParticipantPermission.newBuilder()
                            .setCanPublish(canPublish).setCanPublishData(true).setCanSubscribe(true).build()).execute().body();
            log.info("Changed publish permission successfully for participant {} in room {}", participantIdentity, roomName);
        } catch (IOException | NullPointerException e) {
            log.error("Error changing publish permission for participant {} in room {}", participantIdentity, roomName, e);
            throw new LiveKitException("Change publish permission");
        }
    }

    public void muteParticipant(String roomName, String participantIdentity, boolean mute) {
        try {
            LivekitModels.ParticipantInfo participant = roomServiceClient.getParticipant(roomName, participantIdentity).execute().body();
            for (var trackInfo : participant.getTracksList())
                if (trackInfo.getType() == LivekitModels.TrackType.AUDIO)
                    roomServiceClient.mutePublishedTrack(roomName, participantIdentity, trackInfo.getSid(), mute).execute().body();
            log.info("Muted participant {} in room {}", participantIdentity, roomName);
        } catch (IOException | NullPointerException e) {
            log.error("Error muting participant {} in room {}", participantIdentity, roomName, e);
            throw new LiveKitException("Mute participant");
        }
    }

    public void expelParticipant(String roomName, String participantIdentity) {
        try {
            roomServiceClient.removeParticipant(roomName, participantIdentity).execute().body();
            log.info("Expelled participant {} from room {}", participantIdentity, roomName);
        } catch (IOException e) {
            log.error("Error expelling participant {} from room {}", participantIdentity, roomName, e);
            throw new LiveKitException("Expel participant");
        }
    }

    public TokenResponse createAccessToken(Room room, AppUser userDetails) {
        accessToken.setName(userDetails.getName());
        accessToken.setIdentity(userDetails.getUsername());
        accessToken.setTtl(TOKEN_TIME_OUT);

        if (room.getBroadcaster().getId() == userDetails.getId()) {
            accessToken.addGrants(new RoomJoin(true), new RoomAdmin(true),
                    new RoomName(room.getName()), new CanPublish(true), new CanPublishData(true));
        } else {
            accessToken.addGrants(new RoomJoin(true), new RoomName(room.getName()),
                    new CanPublish(false), new CanPublishData(true));
        }
        TokenResponse tokenResponse = new TokenResponse(accessToken.toJwt());

        log.info("Created access token for user {} in room {}", userDetails.getUsername(), room.getName());
        return tokenResponse;
    }

//    private void startRecode(String roomName){
//        LivekitEgress.EncodedFileOutput output = LivekitEgress.EncodedFileOutput.newBuilder()
//                .setFileType(LivekitEgress.EncodedFileType.MP4)
//                .setFilepath("test-recording.mp4")
//                .build();
//
//        Call<LivekitEgress.EgressInfo> call = egressServiceClient.startRoomCompositeEgress(
//                roomName,
//                output
//        );
//        try {
//            LivekitEgress.EgressInfo egressInfo = call.execute().body();
//            System.out.println(egressInfo.getEgressId());
//            // handle engress info
//        } catch (IOException e) {
//            // handle error
//            System.out.println(e.getMessage());
//        }
//    }

    private int getRoomTime(Room room) {
        // Get the current LocalDateTime
        LocalDateTime now = LocalDateTime.now();

        // Calculate the duration between the two LocalDateTime instances
        Duration duration = Duration.between(room.getTime(), now);

        // Get the total number of seconds
        long seconds = duration.getSeconds();

        return (int) (seconds + EMPTY_TIMEOUT);
    }

}
