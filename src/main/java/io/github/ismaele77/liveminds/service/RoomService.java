package io.github.ismaele77.liveminds.service;

import io.github.ismaele77.liveminds.dto.request.CreateRoomRequest;
import io.github.ismaele77.liveminds.dto.request.SearchRequest;
import io.github.ismaele77.liveminds.enums.RoomStatus;
import io.github.ismaele77.liveminds.exception.RoomNotFoundException;
import io.github.ismaele77.liveminds.exception.UserNotFoundException;
import io.github.ismaele77.liveminds.model.AppUser;
import io.github.ismaele77.liveminds.model.Room;
import io.github.ismaele77.liveminds.repository.AppUserRepository;
import io.github.ismaele77.liveminds.repository.RoomRepository;
import io.github.ismaele77.liveminds.specifications.RoomSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final AppUserRepository appUserRepository;
    private final LiveKitService liveKitService;

    public Page<Room> search(SearchRequest request) {
        log.info("Searching for rooms with request: {}", request);
        Specification<Room> specification = Specification.where(null);
        for (var criteria : request.getFilters()) {
            specification = RoomSpecifications.addSpecificationByKey(criteria.getKey(), criteria.getValue(), specification);
        }
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        return roomRepository.findAll(specification, pageable);
    }

    public Page<Room> findAll(int page, int size) {
        log.info("Fetching all rooms with page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        return roomRepository.findAll(pageable);
    }

    public Room findByNameOrThrow(String name) {
        log.info("Fetching room by name: {}", name);
        return roomRepository.findByName(name)
                .orElseThrow(() -> new RoomNotFoundException(name));
    }

    public Optional<Room> findByName(String name) {
        log.info("Fetching room by name (optional): {}", name);
        return roomRepository.findByName(name);
    }

    public boolean existsByName(String name) {
        log.info("Checking if room exists by name: {}", name);
        return roomRepository.existsByName(name);
    }

    public void create(CreateRoomRequest createRoomRequest, AppUser userDetails) {
        log.info("Creating room with request: {} by user: {}", createRoomRequest, userDetails.getUsername());
        Room room = new Room(
                null,
                createRoomRequest.getName(),
                createRoomRequest.getProgram(),
                createRoomRequest.getCourse(),
                createRoomRequest.getProfessorClass(),
                createRoomRequest.getTime(),
                RoomStatus.NOT_STARTED.getValue(),
                userDetails,
                Collections.emptyList()
        );

        liveKitService.createRoom(room);

        roomRepository.save(room);
        log.info("Room created successfully. Name: {}", createRoomRequest.getName());
    }

    public void update(CreateRoomRequest roomRequest, Room room) {
        log.info("Updating room with name: {}", room.getName());
        room.setName(roomRequest.getName());
        room.setProgram(roomRequest.getProgram());
        room.setCourse(roomRequest.getCourse());
        room.setProfessorClass(roomRequest.getProfessorClass());
        room.setTime(roomRequest.getTime());

        liveKitService.updateRoom(room);
        roomRepository.save(room);
        log.info("Room updated successfully. Name: {}", room.getName());
    }

    public void delete(Room room) {
        log.info("Deleting room with name: {}", room.getName());
        roomRepository.delete(room);
    }

    public void deleteByName(String name) {
        log.info("Deleting room by name: {}", name);
        roomRepository.deleteByName(name);
    }

    public void banUser(String roomName, String userName) {
        log.info("Banning user {} from room {}", userName, roomName);
        Room room = roomRepository.findByName(roomName).orElseThrow(() -> new RoomNotFoundException(roomName));
        AppUser user = appUserRepository.findByUserName(userName).orElseThrow(() -> new UserNotFoundException(userName));

        if (!room.getBannedUsers().contains(user)) {
            room.getBannedUsers().add(user);
            roomRepository.save(room);
            log.info("User {} banned successfully from room {}", userName, roomName);
        }
    }

    public void save(Room room) {
        log.info("Saving room with name: {}", room.getName());
        roomRepository.save(room);
    }
}
