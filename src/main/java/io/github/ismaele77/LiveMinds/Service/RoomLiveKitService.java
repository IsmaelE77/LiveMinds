package io.github.ismaele77.LiveMinds.Service;

import io.github.ismaele77.LiveMinds.DTO.ParticipantDto;
import io.github.ismaele77.LiveMinds.Model.Room;
import io.livekit.server.RoomServiceClient;
import livekit.LivekitModels;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component
public class RoomLiveKitService {

    private final RoomServiceClient roomServiceClient;
    private final int emptyTimeout = 3 * 60 * 60;
    public RoomLiveKitService(RoomServiceClient roomServiceClient){
        this.roomServiceClient = roomServiceClient;
    }


    public boolean CreateRoom(Room room){
        int roomTime = getRoomTime(room);
        Call<LivekitModels.Room> call = roomServiceClient.createRoom(room.getName(), roomTime);
        try {
            call.execute();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public boolean UpdateRoom(Room room){
        roomServiceClient.deleteRoom(room.getName());
        return CreateRoom(room);
    }

    public List<LivekitModels.ParticipantInfo> getParticipantList(String roomName){
        List<LivekitModels.ParticipantInfo> participants = null;
        try {
            participants = roomServiceClient.listParticipants(roomName).execute().body();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return participants;
    }

    public boolean givePublishPermission(String roomName , String participantIdentity ,boolean canPublish){
        try {
            LivekitModels.ParticipantInfo participant = roomServiceClient.getParticipant(roomName,participantIdentity).execute().body();
            var per = LivekitModels.ParticipantPermission.newBuilder()
                    .setCanPublish(false).build();
            var response = roomServiceClient.updateParticipant(roomName,participant.getIdentity() , participant.getName(),
                    participant.getMetadata() , LivekitModels.ParticipantPermission.newBuilder()
                            .setCanPublish(canPublish).build()).execute().body();
            return response.isInitialized();
        } catch (IOException e) {
            return false;
        } catch (NullPointerException e){
            return false;
        }
    }

    public boolean muteParticipant(String roomName , String participantIdentity , boolean mute){
        try {
            LivekitModels.ParticipantInfo participant = roomServiceClient.getParticipant(roomName,participantIdentity).execute().body();
            for (var trackInfo : participant.getTracksList())
                    if(trackInfo.getType() == LivekitModels.TrackType.AUDIO)
                        roomServiceClient.mutePublishedTrack(roomName,participantIdentity ,trackInfo.getSid(),mute).execute().body();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean expelParticipant(String roomName , String participantIdentity){
        try {
            roomServiceClient.removeParticipant(roomName,participantIdentity).execute().body();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private int getRoomTime(Room room){
        // get the current time
        Date currentTime = new Date();

        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(currentTime);

        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(room.getTime());

        return calculateSecondsDifference(calendar1, calendar2) + emptyTimeout ;
    }
    private int calculateSecondsDifference(Calendar calendar1, Calendar calendar2) {
        long milliseconds1 = calendar1.getTimeInMillis();
        long milliseconds2 = calendar2.getTimeInMillis();
        long diff = milliseconds2 - milliseconds1;
        return (int) (diff / 1000);
    }
}
