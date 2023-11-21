package io.github.ismaele77.LiveMinds.Controller;

import io.github.ismaele77.LiveMinds.DTO.CreateRoomRequest;
import io.github.ismaele77.LiveMinds.DTO.RoomDto;
import io.github.ismaele77.LiveMinds.Model.AppUser;
import io.github.ismaele77.LiveMinds.Repository.AppUserRepository;
import io.github.ismaele77.LiveMinds.Repository.RoomRepository;
import io.github.ismaele77.LiveMinds.Model.Room;
import io.github.ismaele77.LiveMinds.Service.RoomLiveKitService;
import io.livekit.server.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    ResponseEntity<CollectionModel<RoomDto>> findAll(){
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
    ResponseEntity<EntityModel<RoomDto>> findByName(@PathVariable String roomName) {
        Room room = roomRepository.findById(roomName)
                .orElse(null);
        if(room == null)
            return ResponseEntity.notFound().build();

        RoomDto roomDto = new RoomDto(room);
        return ResponseEntity.ok(EntityModel.of(roomDto));
    }

    @DeleteMapping("/{roomName}")
    ResponseEntity<?> deleteByName(@PathVariable String roomName) {
        Room room = roomRepository.findById(roomName)
                .orElse(null);
        if(room == null)
            return ResponseEntity.notFound().build();

        roomRepository.deleteById(roomName);

        return ResponseEntity.accepted().build();
    }

    @PostMapping
    public ResponseEntity<?> createRoom(@RequestBody @Valid CreateRoomRequest createRoomRequest , Errors errors) {
        if(errors.hasErrors()){
            return ResponseEntity.badRequest().build();
        }
        if(roomRepository.existsByName(createRoomRequest.getName())){
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Room with name " + createRoomRequest.getName() + " already exists.");
        }

        AppUser broadcaster = appUserRepository.findById(createRoomRequest.getBroadcasterId())
                .orElseThrow(() -> new EntityNotFoundException());

        Room room = new Room(
                null,
                createRoomRequest.getName(),
                createRoomRequest.getProgram(),
                createRoomRequest.getCourse(),
                createRoomRequest.getProfessorClass(),
                createRoomRequest.getTime(),
                broadcaster
        );

        if (!roomLiveKit.CreateRoom(room)) {
            return ResponseEntity.internalServerError().build();
        }

        roomRepository.save(room);

        URI location = linkTo(RoomController.class).slash(room.getName()).toUri();

        return ResponseEntity.created(location).body("Room created successfully");
    }

    @PatchMapping("/{roomName}")
    public ResponseEntity<?> updateRoom(@RequestBody @Valid CreateRoomRequest createRoomRequest , Errors errors , @PathVariable String roomName) {
        Room room = roomRepository.findByName(roomName)
                .orElse(null);
        if(room == null) {
            return ResponseEntity.notFound().build();
        }
        if(errors.hasErrors()){
            return ResponseEntity.badRequest().build();
        }
        if(roomRepository.existsByName(createRoomRequest.getName())){
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Room with name " + createRoomRequest.getName() + " already exists.");
        }

        room.setName(createRoomRequest.getName());
        room.setProgram(createRoomRequest.getProgram());
        room.setCourse(createRoomRequest.getCourse());
        room.setProfessorClass(createRoomRequest.getProfessorClass());
        room.setTime(createRoomRequest.getTime());

        if (!roomLiveKit.UpdateRoom(room)) {
            return ResponseEntity.internalServerError().build();
        }

        roomRepository.save(room);

        URI location = linkTo(RoomController.class).slash(room.getName()).toUri();

        return ResponseEntity.created(location).body("Room updated successfully");
    }

    @GetMapping("/{roomName}/token")
    public ResponseEntity<?> getRoomToken(@PathVariable String roomName ,@RequestParam Long userId) {
        if (!roomRepository.existsById(roomName)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Room with name " + roomName + " not exists.");
        }
        AppUser user = appUserRepository.findById(userId).get();
        accessToken.setName(user.getName());
        accessToken.setIdentity(user.getUserName());

        if (user.getRole().getName().equals("Professor")){
            accessToken.addGrants(new RoomJoin(true), new RoomAdmin(true),  new RoomName(roomName) , new CanPublish(true) ,new CanPublishData(true));
        }
        else{
            accessToken.addGrants(new RoomJoin(true), new RoomName(roomName) , new CanPublish(false) , new CanPublishData(true));
        }

        return ResponseEntity.ok(accessToken.toJwt());
    }

}
