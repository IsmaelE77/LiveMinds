package io.github.ismaele77.LiveMinds.Service;

import io.github.ismaele77.LiveMinds.Exception.RoomNotFoundException;
import io.github.ismaele77.LiveMinds.Exception.UserNotFoundException;
import io.github.ismaele77.LiveMinds.Model.AppUser;
import io.github.ismaele77.LiveMinds.Model.Room;
import io.github.ismaele77.LiveMinds.Repository.AppUserRepository;
import io.github.ismaele77.LiveMinds.Repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomService {


    private final RoomRepository roomRepository;
    private final AppUserRepository appUserRepository;

    public void banUser(String roomName, String userName) {
        Room room = roomRepository.findByName(roomName).orElseThrow(() -> new RoomNotFoundException(roomName));
        AppUser user = appUserRepository.findByUserName(userName).orElseThrow(() -> new UserNotFoundException(userName));

        if (!room.getBannedUsers().contains(user)) {
            room.getBannedUsers().add(user);
            roomRepository.save(room);
        }
    }
}
