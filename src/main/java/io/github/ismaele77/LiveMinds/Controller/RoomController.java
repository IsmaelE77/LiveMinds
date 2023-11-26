package io.github.ismaele77.LiveMinds.Controller;

import io.github.ismaele77.LiveMinds.DTO.CreateRoomRequest;
import io.github.ismaele77.LiveMinds.DTO.ParticipantDto;
import io.github.ismaele77.LiveMinds.DTO.RoomDto;
import io.github.ismaele77.LiveMinds.Exception.AccessDeniedException;
import io.github.ismaele77.LiveMinds.Exception.RoomNotFoundException;
import io.github.ismaele77.LiveMinds.Model.AppUser;
import io.github.ismaele77.LiveMinds.Repository.AppUserRepository;
import io.github.ismaele77.LiveMinds.Repository.RoomRepository;
import io.github.ismaele77.LiveMinds.Model.Room;
import io.github.ismaele77.LiveMinds.Service.RoomLiveKitService;
import io.livekit.server.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping("/api/v1/rooms")
public class
RoomController {

    private final RoomRepository roomRepository;
    private final AppUserRepository appUserRepository;
    private final RoomLiveKitService roomLiveKit;
    private final AccessToken accessToken;

    public RoomController(RoomRepository roomRepository, AppUserRepository appUserRepository, RoomLiveKitService roomLiveKit, AccessToken accessToken) {
        this.roomRepository = roomRepository;
        this.appUserRepository = appUserRepository;
        this.roomLiveKit = roomLiveKit;
        this.accessToken = accessToken;
    }

    @GetMapping
    @PreAuthorize("hasRole('Professor') || hasRole('Student')")
     public ResponseEntity<CollectionModel<RoomDto>> findAll(HttpServletRequest request){
        List<RoomDto> allRooms = new ArrayList<RoomDto>();
        for (Room room : roomRepository.findAll()) {
            String roomName = room.getName();
            Link selfLink = linkTo(RoomController.class).slash(roomName).withSelfRel();
            RoomDto roomDto = new RoomDto();
            roomDto.CreateNewUserDto(room);
            roomDto.add(selfLink);
            allRooms.add(roomDto);
        }

        Link link = linkTo(RoomController.class).withSelfRel();
        CollectionModel<RoomDto> result = CollectionModel.of(allRooms, link);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{roomName}")
    @PreAuthorize("hasRole('Professor') || hasRole('Student')")
    ResponseEntity<EntityModel<RoomDto>> findByName(@PathVariable String roomName) {
        Room room = roomRepository.findByName(roomName)
                .orElseThrow(() -> new RoomNotFoundException(roomName));

        RoomDto roomDto = new RoomDto(room);
        return ResponseEntity.ok(EntityModel.of(roomDto));
    }


    @PostMapping
    @PreAuthorize("hasRole('Professor')")
    public ResponseEntity<?> createRoom(
            @RequestBody @Valid CreateRoomRequest createRoomRequest ,
            Errors errors,  @AuthenticationPrincipal AppUser userDetails) {
        if(errors.hasErrors()){
            return ResponseEntity.badRequest().build();
        }
        if(roomRepository.existsByName(createRoomRequest.getName())){
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Room with name " + createRoomRequest.getName() + " already exists.");
        }


        Room room = new Room(
                null,
                createRoomRequest.getName(),
                createRoomRequest.getProgram(),
                createRoomRequest.getCourse(),
                createRoomRequest.getProfessorClass(),
                createRoomRequest.getTime(),
                userDetails
        );

        roomLiveKit.CreateRoom(room);

        roomRepository.save(room);

        URI location = linkTo(RoomController.class).slash(room.getName()).toUri();

        return ResponseEntity.created(location).body("Room created successfully");
    }

    @PatchMapping("/{roomName}")
    @PreAuthorize("hasRole('Professor')")
    public ResponseEntity<?> updateRoom(@RequestBody @Valid CreateRoomRequest createRoomRequest ,
                                        Errors errors , @PathVariable String roomName,
                                        @AuthenticationPrincipal AppUser userDetails) {
        Room room = roomRepository.findByName(roomName)
                .orElseThrow(() -> new RoomNotFoundException(roomName));
        if(room.getBroadcaster().getId() != userDetails.getId()){
            throw new AccessDeniedException("update" + roomName);
        }
        if(errors.hasErrors()){
            return ResponseEntity.badRequest().build();
        }
        var bool = createRoomRequest.getTime().equals(room.getTime());
        if(roomRepository.existsByName(createRoomRequest.getName())
            && createRoomRequest.getTime().isEqual(room.getTime())){
            return ResponseEntity.status(HttpStatus.OK)
                    .body("Nothing change");
        }

        room.setName(createRoomRequest.getName());
        room.setProgram(createRoomRequest.getProgram());
        room.setCourse(createRoomRequest.getCourse());
        room.setProfessorClass(createRoomRequest.getProfessorClass());
        room.setTime(createRoomRequest.getTime());

        roomLiveKit.UpdateRoom(room);
        roomRepository.save(room);

        //URI location = linkTo(RoomController.class).slash(room.getName()).toUri();

        return ResponseEntity.ok().body("Room updated successfully");
    }

    @DeleteMapping("/{roomName}")
    @PreAuthorize("hasRole('Professor')")
    ResponseEntity<?> deleteByName(@PathVariable String roomName, @AuthenticationPrincipal AppUser userDetails) {
        Room room = roomRepository.findByName(roomName)
                .orElseThrow(() -> new RoomNotFoundException(roomName));
        if(room.getBroadcaster().getId() != userDetails.getId()){
            throw new AccessDeniedException("delete" + roomName);
        }

        roomRepository.deleteById(room.getId());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{roomName}/token")
    @PreAuthorize("hasRole('Professor') || hasRole('Student')")
    public ResponseEntity<?> getRoomToken(@PathVariable String roomName,
        @AuthenticationPrincipal AppUser userDetails) {
        Room room = roomRepository.findByName(roomName)
                .orElseThrow(() -> new RoomNotFoundException(roomName));

        accessToken.setName(userDetails.getName());
        accessToken.setIdentity(userDetails.getUsername());
        accessToken.setTtl(2*60);
        if (userDetails.getRole().getName().equals("Professor")){
            accessToken.addGrants(new RoomJoin(true), new RoomAdmin(true),  new RoomName(roomName) , new CanPublish(true) ,new CanPublishData(true));
        }
        else{
            accessToken.addGrants(new RoomJoin(true), new RoomName(roomName) , new CanPublish(false) , new CanPublishData(true));
        }

        return ResponseEntity.ok(accessToken.toJwt());
    }

    @GetMapping("/{roomName}/participants")
    @PreAuthorize("hasRole('Professor') || hasRole('Student')")
    public ResponseEntity<?> getParticipants(@PathVariable String roomName){
        if (!roomRepository.existsByName(roomName)) {
            throw new RoomNotFoundException(roomName);
        }
        var roomParticipantsList = roomLiveKit.getParticipantList(roomName);
        List<ParticipantDto> participantsInfo = new ArrayList<ParticipantDto>();
        for (var participant : roomParticipantsList) {
            ParticipantDto user = new ParticipantDto();
            user.setName(participant.getName());
            user.setIdentity(participant.getIdentity());
            participantsInfo.add(user);
        }
        return ResponseEntity.ok(participantsInfo);
    }

    @PostMapping("/{roomName}/participants/{participantIdentity}/canPublish")
    @PreAuthorize("hasRole('Professor')")
    public ResponseEntity<?> changePublishPermission(@PathVariable String roomName ,
                                                   @PathVariable String participantIdentity , @RequestBody boolean canPublish){
        if (!roomRepository.existsByName(roomName)) {
            throw new RoomNotFoundException(roomName);
        }
        boolean result = roomLiveKit.changePublishPermission(roomName,participantIdentity,canPublish);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{roomName}/participants/{participantIdentity}/mute")
    @PreAuthorize("hasRole('Professor')")
    public ResponseEntity<?> muteParticipant(@PathVariable String roomName ,
                                                   @PathVariable String participantIdentity , @RequestBody boolean mute){
        if (!roomRepository.existsByName(roomName)) {
            throw new RoomNotFoundException(roomName);
        }
        boolean result = roomLiveKit.muteParticipant(roomName,participantIdentity,mute);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{roomName}/participants/{participantIdentity}/expel")
    @PreAuthorize("hasRole('Professor')")
    public ResponseEntity<?> expelParticipant(@PathVariable String roomName ,
                                             @PathVariable String participantIdentity){
        if (!roomRepository.existsByName(roomName)) {
            throw new RoomNotFoundException(roomName);
        }
        boolean result = roomLiveKit.expelParticipant(roomName,participantIdentity);
        return ResponseEntity.ok(result);
    }

}