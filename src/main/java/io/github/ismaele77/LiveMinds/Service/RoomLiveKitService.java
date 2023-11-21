package io.github.ismaele77.LiveMinds.Service;

import io.github.ismaele77.LiveMinds.Model.Room;
import io.livekit.server.RoomServiceClient;
import livekit.LivekitModels;
import org.springframework.stereotype.Component;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

@Component
public class RoomLiveKitService {

    private final RoomServiceClient roomServiceClient;
    private final int emptyTimeout = 3 * 60 * 60;
    public RoomLiveKitService(RoomServiceClient roomServiceClient){
        this.roomServiceClient = roomServiceClient;
    }

    public boolean CreateRoom(Room room){
        // get the current time
        Date currentTime = new Date();

        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(currentTime);

        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(room.getTime());

        int roomTime = calculateSecondsDifference(calendar1, calendar2) + emptyTimeout ;
        System.out.println(roomTime);
        Call<LivekitModels.Room> call = roomServiceClient.createRoom(room.getName(), roomTime);
        try {
            call.execute();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private static int calculateSecondsDifference(Calendar calendar1, Calendar calendar2) {
        long milliseconds1 = calendar1.getTimeInMillis();
        long milliseconds2 = calendar2.getTimeInMillis();
        long diff = milliseconds2 - milliseconds1;
        return (int) (diff / 1000);
    }
}
