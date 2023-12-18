package io.github.ismaele77.LiveMinds.Controller;

import io.github.ismaele77.LiveMinds.DTO.*;
import io.github.ismaele77.LiveMinds.Enum.RoomStatus;
import io.github.ismaele77.LiveMinds.Exception.AccessDeniedException;
import io.github.ismaele77.LiveMinds.Exception.RoomNotFoundException;
import io.github.ismaele77.LiveMinds.Model.AppUser;
import io.github.ismaele77.LiveMinds.Repository.RoomRepository;
import io.github.ismaele77.LiveMinds.Model.Room;
import io.github.ismaele77.LiveMinds.Service.RoomLiveKitService;
import io.github.ismaele77.LiveMinds.Service.RoomService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
public class
RoomController {

    private final RoomRepository roomRepository;
    private final RoomLiveKitService roomLiveKit;
    private final RoomService roomService;

    @GetMapping
    @PreAuthorize("hasRole('Professor') || hasRole('Student')")
    public ResponseEntity<CollectionModel<RoomDto>> findAll(HttpServletRequest request) {
        List<RoomDto> allRooms = new ArrayList<>();
        for (Room room : roomRepository.findAll()) {
            String roomName = room.getName();
            Link selfLink = linkTo(RoomController.class).slash(roomName).withSelfRel();
            RoomDto roomDto = new RoomDto();
            roomDto.createNewRoomDto(room);
            roomDto.add(selfLink);
            allRooms.add(roomDto);
        }

        Link link = linkTo(RoomController.class).withSelfRel();
        CollectionModel<RoomDto> result = CollectionModel.of(allRooms, link);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{roomName}")
    @PreAuthorize("hasRole('Professor') || hasRole('Student')")
    ResponseEntity<RoomDto> findByName(@PathVariable String roomName) {
        Room room = retrieveRoomByName(roomName);
        RoomDto roomDto = new RoomDto(room);
        Link selfLink = linkTo(RoomController.class).slash(roomName).withSelfRel();
        Link allRooms = linkTo(RoomController.class).withRel("rooms");
        roomDto.add(selfLink);
        roomDto.add(allRooms);

        return ResponseEntity.ok(roomDto);
    }


    @PostMapping
    @PreAuthorize("hasRole('Professor')")
    public ResponseEntity<?> createRoom(
            @RequestBody @Valid CreateRoomRequest createRoomRequest,
            Errors errors, @AuthenticationPrincipal AppUser userDetails) {
        if (errors.hasErrors()) {
            return handleValidationErrors();
        }
        if (roomRepository.existsByName(createRoomRequest.getName())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error",
                    "Room with name " + createRoomRequest.getName() + " already exists."));
        }

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

        roomLiveKit.CreateRoom(room);

        roomRepository.save(room);

        URI location = linkTo(RoomController.class).slash(room.getName()).toUri();

        return ResponseEntity.created(location).body(Map.of("message", "Room created successfully"));
    }

    @PatchMapping("/{roomName}")
    @PreAuthorize("hasRole('Professor')")
    public ResponseEntity<?> updateRoom(@RequestBody @Valid CreateRoomRequest createRoomRequest,
                                        Errors errors, @PathVariable String roomName,
                                        @AuthenticationPrincipal AppUser userDetails) {
        if (errors.hasErrors()) {
            return handleValidationErrors();
        }

        Room room = retrieveRoomByName(roomName);
        checkUserCreatedRoom(room, userDetails, "update" + roomName);

        if (roomRepository.existsByName(createRoomRequest.getName())
                && createRoomRequest.getTime().isEqual(room.getTime())) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(Map.of("message", "Nothing change"));
        }

        room.setName(createRoomRequest.getName());
        room.setProgram(createRoomRequest.getProgram());
        room.setCourse(createRoomRequest.getCourse());
        room.setProfessorClass(createRoomRequest.getProfessorClass());
        room.setTime(createRoomRequest.getTime());

        roomLiveKit.UpdateRoom(room);
        roomRepository.save(room);

        //URI location = linkTo(RoomController.class).slash(room.getName()).toUri();

        return ResponseEntity.ok().body(Map.of("message", "Room updated successfully"));
    }

    @DeleteMapping("/{roomName}")
    @PreAuthorize("hasRole('Professor')")
    ResponseEntity<?> deleteByName(@PathVariable String roomName, @AuthenticationPrincipal AppUser userDetails) {
        Room room = retrieveRoomByName(roomName);
        checkUserCreatedRoom(room, userDetails, "delete" + roomName);
        roomRepository.deleteById(room.getId());
        return ResponseEntity.ok().body(Map.of("message", "Room deleted successfully"));
    }

    @GetMapping("/{roomName}/token")
    @PreAuthorize("hasRole('Professor') || hasRole('Student')")
    public ResponseEntity<?> getRoomToken(@PathVariable String roomName,
                                          @AuthenticationPrincipal AppUser userDetails) {
        Room room = retrieveRoomByName(roomName);
        if (room.getBannedUsers().contains(userDetails)) {
            throw new AccessDeniedException("get token");
        }
        TokenResponse tokenResponse = roomLiveKit.createAccessToken(room, userDetails);

        return ResponseEntity.ok(tokenResponse);
    }


    @GetMapping("/{roomName}/participants")
    @PreAuthorize("hasRole('Professor') || hasRole('Student')")
    public ResponseEntity<CollectionModel<ParticipantDto>> getParticipants(@PathVariable String roomName) {
        if (!roomRepository.existsByName(roomName)) {
            throw new RoomNotFoundException(roomName);
        }
        var participantsInfo = roomLiveKit.getParticipantList(roomName).stream()
                .map(participant -> new ParticipantDto(
                        participant.getName(),
                        participant.getIdentity()
                ))
                .toList();
        Link link = linkTo(RoomController.class).withSelfRel();
        CollectionModel<ParticipantDto> result = CollectionModel.of(participantsInfo, link);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{roomName}/participants/{participantIdentity}/canPublish")
    @PreAuthorize("hasRole('Professor')")
    public ResponseEntity<?> changePublishPermission
            (@PathVariable String roomName,
             @PathVariable String participantIdentity,
             @RequestBody @Valid canPublishRequest req,
             Errors errors,
             @AuthenticationPrincipal AppUser userDetails) {
        if (errors.hasErrors()) {
            return handleValidationErrors();
        }
        Room room = retrieveRoomByName(roomName);
        checkUserCreatedRoom(room, userDetails, "change publish permission for participant");
        roomLiveKit.changePublishPermission(roomName, participantIdentity, req.isCanPublish());
        return ResponseEntity.ok(Map.of("message", "Participant permission changed"));
    }

    @PostMapping("/{roomName}/participants/{participantIdentity}/mute")
    @PreAuthorize("hasRole('Professor')")
    public ResponseEntity<?> muteParticipant
            (@PathVariable String roomName,
             @PathVariable String participantIdentity,
             @RequestBody @Valid muteRequest req,
             Errors errors,
             @AuthenticationPrincipal AppUser userDetails) {
        if (errors.hasErrors()) {
            return handleValidationErrors();
        }
        Room room = retrieveRoomByName(roomName);
        checkUserCreatedRoom(room, userDetails, "mute participant");
        roomLiveKit.muteParticipant(roomName, participantIdentity, req.isMute());
        return ResponseEntity.ok(Map.of("message", "Participant was muted"));
    }

    @PostMapping("/{roomName}/participants/{participantIdentity}/expel")
    @PreAuthorize("hasRole('Professor')")
    public ResponseEntity<?> expelParticipant
            (@PathVariable String roomName,
             @PathVariable String participantIdentity,
             @AuthenticationPrincipal AppUser userDetails) {
        Room room = retrieveRoomByName(roomName);
        checkUserCreatedRoom(room, userDetails, "expel participant");
        roomLiveKit.expelParticipant(roomName, participantIdentity);
        roomService.banUser(roomName, participantIdentity);
        return ResponseEntity.ok(Map.of("message", "Participant was expelled"));
    }

    private void checkUserCreatedRoom(@NotNull Room room, @NotNull AppUser user, String command) {
        if (!room.getBroadcaster().getId().equals(user.getId())) {
            throw new AccessDeniedException(command);
        }
    }

    private Room retrieveRoomByName(String roomName) {
        return roomRepository.findByName(roomName)
                .orElseThrow(() -> new RoomNotFoundException(roomName));
    }

    private ResponseEntity<?> handleValidationErrors() {
        return ResponseEntity.badRequest()
                .body(Map.of("error", "Invalid request data"));
    }

}