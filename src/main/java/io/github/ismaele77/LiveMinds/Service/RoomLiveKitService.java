package io.github.ismaele77.LiveMinds.Service;

import io.github.ismaele77.LiveMinds.DTO.TokenResponse;
import io.github.ismaele77.LiveMinds.Exception.LiveKitException;
import io.github.ismaele77.LiveMinds.Exception.RoomCreationException;
import io.github.ismaele77.LiveMinds.Model.AppUser;
import io.github.ismaele77.LiveMinds.Model.Room;
import io.livekit.server.*;
import livekit.LivekitModels;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import retrofit2.Call;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RoomLiveKitService {


    private final RoomServiceClient roomServiceClient;
    private final AccessToken accessToken;
    private static final Logger logger = LoggerFactory.getLogger(RoomLiveKitService.class);
    private final static int EMPTY_TIMEOUT = 3 * 60 * 60;
    private static final int TOKEN_TIME_OUT = 2*60;

    //private final EgressServiceClient egressServiceClient;



    public void CreateRoom(Room room){
        int roomTime = getRoomTime(room);
        Call<LivekitModels.Room> call = roomServiceClient.createRoom(room.getName(), roomTime);
        try {
            call.execute();
        } catch (IOException e) {
            logger.error("Error creating room", e);
            throw new RoomCreationException();
        }
    }

    @Transactional
    public void UpdateRoom(Room room){
        roomServiceClient.deleteRoom(room.getName());
        CreateRoom(room);
    }

    public List<LivekitModels.ParticipantInfo> getParticipantList(String roomName){
        try {
            return roomServiceClient.listParticipants(roomName).execute().body();
        } catch (IOException e) {
            logger.error("Error get participant list", e);
            throw new LiveKitException("Get participant list");
        }
    }

    public void changePublishPermission(String roomName , String participantIdentity ,boolean canPublish){
        try {
            LivekitModels.ParticipantInfo participant = roomServiceClient.getParticipant(roomName,participantIdentity).execute().body();
            LivekitModels.ParticipantPermission.newBuilder()
                    .setCanPublish(false).build();
            var response = roomServiceClient.updateParticipant(roomName,participant.getIdentity() , participant.getName(),
                    participant.getMetadata() , LivekitModels.ParticipantPermission.newBuilder()
                            .setCanPublish(canPublish).setCanPublishData(true).setCanSubscribe(true).build()).execute().body();
        } catch (IOException | NullPointerException e) {
            logger.error("Error change publish permission", e);
            throw new LiveKitException("Change publish permission");
        }
    }

    public void muteParticipant(String roomName , String participantIdentity , boolean mute){
        try {
            LivekitModels.ParticipantInfo participant = roomServiceClient.getParticipant(roomName,participantIdentity).execute().body();
            for (var trackInfo : participant.getTracksList())
                    if(trackInfo.getType() == LivekitModels.TrackType.AUDIO)
                        roomServiceClient.mutePublishedTrack(roomName,participantIdentity ,trackInfo.getSid(),mute).execute().body();
        } catch (IOException | NullPointerException e) {
            logger.error("Error mute participant", e);
            throw new LiveKitException("Mute participant");
        }
    }

    public void expelParticipant(String roomName , String participantIdentity){
        try {
            roomServiceClient.removeParticipant(roomName,participantIdentity).execute().body();
        } catch (IOException e) {
            logger.error("Error expel participant", e);
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

    private int getRoomTime(Room room){
        // Get the current LocalDateTime
        LocalDateTime now = LocalDateTime.now();

        // Calculate the duration between the two LocalDateTime instances
        Duration duration = Duration.between(room.getTime(), now);

        // Get the total number of seconds
        long seconds = duration.getSeconds();

        return  (int)(seconds + EMPTY_TIMEOUT);
    }
    private int calculateSecondsDifference(Calendar calendar1, Calendar calendar2) {
        long milliseconds1 = calendar1.getTimeInMillis();
        long milliseconds2 = calendar2.getTimeInMillis();
        long diff = milliseconds2 - milliseconds1;
        return (int) (diff / 1000);
    }
}
