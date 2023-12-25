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
        Specification<Room> specification = Specification.where(null);
        for (var criteria : request.getFilters()) {
            specification = RoomSpecifications.addSpecificationByKey(criteria.getKey(), criteria.getValue(), specification);
        }
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        return roomRepository.findAll(specification, pageable);
    }

    public Page<Room> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return roomRepository.findAll(pageable);
    }

    public Room findByNameOrThrow(String name) {
        return roomRepository.findByName(name)
                .orElseThrow(() -> new RoomNotFoundException(name));
    }

    public Optional<Room> findByName(String name) {
        return roomRepository.findByName(name);
    }

    public boolean existsByName(String name) {
        return roomRepository.existsByName(name);
    }


    public void create(CreateRoomRequest createRoomRequest, AppUser userDetails) {
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
    }

    public void update(CreateRoomRequest roomRequest, Room room) {
        room.setName(roomRequest.getName());
        room.setProgram(roomRequest.getProgram());
        room.setCourse(roomRequest.getCourse());
        room.setProfessorClass(roomRequest.getProfessorClass());
        room.setTime(roomRequest.getTime());

        liveKitService.updateRoom(room);
        roomRepository.save(room);
    }

    public void delete(Room room) {
        roomRepository.delete(room);
    }

    public void deleteByName(String name) {
        roomRepository.deleteByName(name);
    }


    public void banUser(String roomName, String userName) {
        Room room = roomRepository.findByName(roomName).orElseThrow(() -> new RoomNotFoundException(roomName));
        AppUser user = appUserRepository.findByUserName(userName).orElseThrow(() -> new UserNotFoundException(userName));

        if (!room.getBannedUsers().contains(user)) {
            room.getBannedUsers().add(user);
            roomRepository.save(room);
        }
    }

    public void save(Room room) {
        roomRepository.save(room);
    }
}
