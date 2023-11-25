package io.github.ismaele77.LiveMinds.Service;

import io.github.ismaele77.LiveMinds.Model.Room;
import io.livekit.server.EgressServiceClient;
import io.livekit.server.RoomServiceClient;
import livekit.LivekitEgress;
import livekit.LivekitModels;
import org.springframework.stereotype.Component;
import retrofit2.Call;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component
public class RoomLiveKitService {


    private final RoomServiceClient roomServiceClient;
    private final int emptyTimeout = 3 * 60 * 60;

//    private final EgressServiceClient egressServiceClient;
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
        try {
            return roomServiceClient.listParticipants(roomName).execute().body();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
