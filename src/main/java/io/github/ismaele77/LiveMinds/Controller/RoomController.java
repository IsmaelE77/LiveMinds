package io.github.ismaele77.LiveMinds.Controller;

import io.github.ismaele77.LiveMinds.DTO.CreateRoomRequest;
import io.github.ismaele77.LiveMinds.Enum.UserRole;
import io.github.ismaele77.LiveMinds.Model.AppUser;
import io.github.ismaele77.LiveMinds.Repository.AppUserRepository;
import io.github.ismaele77.LiveMinds.Repository.RoomRepository;
import io.github.ismaele77.LiveMinds.Model.Room;
import io.github.ismaele77.LiveMinds.Service.RoomLiveKitService;
import io.livekit.server.*;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/rooms")
public class RoomController {

    private final RoomRepository roomRepository;
    private final AppUserRepository appUserRepository;
    private final RoomLiveKitService roomLiveKit;
    private final RoomServiceClient roomServiceClient;
    private final AccessToken accessToken;

    public RoomController(RoomRepository roomRepository, AppUserRepository appUserRepository, RoomLiveKitService roomLiveKit, RoomServiceClient roomServiceClient, AccessToken accessToken) {
        this.roomRepository = roomRepository;
        this.appUserRepository = appUserRepository;
        this.roomLiveKit = roomLiveKit;
        this.roomServiceClient = roomServiceClient;
        this.accessToken = accessToken;
    }

    @GetMapping
    ResponseEntity<CollectionModel<EntityModel<Room>>> findAll(){
        List<EntityModel<Room>> rooms = StreamSupport.stream(roomRepository.findAll().spliterator(), false)
                .map(room -> EntityModel.of(room, //
                        linkTo(methodOn(RoomController.class).roomRepository.findById(room.getName())).withSelfRel(), //
                        linkTo(methodOn(RoomController.class).findAll()).withRel("employees"))) //
                .collect(Collectors.toList());

        return ResponseEntity.ok( //
                CollectionModel.of(rooms, //
                        linkTo(methodOn(RoomController.class).findAll()).withSelfRel()));
    }

    @PostMapping
    public ResponseEntity<?> createRoom(@RequestBody @Valid CreateRoomRequest createRoomRequest , Errors errors) {
        if(errors.hasErrors()){
            return ResponseEntity.badRequest().build();
        }
        if(roomRepository.existsById(createRoomRequest.getName())){
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Room with name " + createRoomRequest.getName() + " already exists.");
        }

        AppUser broadcaster = appUserRepository.findById(createRoomRequest.getBroadcasterId()).get();
        Room room = new Room(
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

    @GetMapping("/{roomName}/token")
    public ResponseEntity<Object> getRoomToken(@PathVariable String roomName ,@RequestParam Long userId) {
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
