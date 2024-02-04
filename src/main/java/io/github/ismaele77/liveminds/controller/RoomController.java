package io.github.ismaele77.liveminds.controller;

import io.github.ismaele77.liveminds.dto.*;
import io.github.ismaele77.liveminds.dto.request.CanPublishRequest;
import io.github.ismaele77.liveminds.dto.request.CreateRoomRequest;
import io.github.ismaele77.liveminds.dto.request.SearchRequest;
import io.github.ismaele77.liveminds.dto.request.muteRequest;
import io.github.ismaele77.liveminds.dto.response.TokenResponse;
import io.github.ismaele77.liveminds.exception.AccessDeniedException;
import io.github.ismaele77.liveminds.model.AppUser;
import io.github.ismaele77.liveminds.model.Room;
import io.github.ismaele77.liveminds.service.LiveKitService;
import io.github.ismaele77.liveminds.service.RoomService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
@Slf4j
public class RoomController {

    private final LiveKitService liveKitService;
    private final RoomService roomService;
    private final RoomDtoModelAssembler assembler;
    private final PagedResourcesAssembler<Room> pagedResourcesAssembler;
    private final String ROOM_SIZE = "15";

    @PostMapping(value = "/search")
    public ResponseEntity<Page<RoomDto>> search(@RequestBody @Valid SearchRequest request) {
        log.info("Searching for rooms with request: {}", request);
        Page<Room> roomPage = roomService.search(request);
        var pagedModel = roomPage.map(assembler::toModel);
        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping
    @PreAuthorize("hasRole('Professor') || hasRole('Student')")
    public ResponseEntity<PagedModel<RoomDto>> findAll(@RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = ROOM_SIZE) int size) {
        log.info("Fetching all rooms with page: {}, size: {}", page, size);
        if (page < 0 || size < 1) {
            log.warn("Invalid page or size parameters: page={}, size={}.", page, size);
            throw new IllegalArgumentException("Page and size must be non-negative and size must be at least 1.");
        }
        Page<Room> roomPage = roomService.findAll(page, size);
        var pagedModel = pagedResourcesAssembler.toModel(roomPage, assembler);
        log.info("Found {} rooms in total.", roomPage.getTotalElements());
        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping("/{roomName}")
    @PreAuthorize("hasRole('Professor') || hasRole('Student')")
    ResponseEntity<RoomDto> findByName(@PathVariable String roomName) {
        log.info("Fetching room by name: {}", roomName);
        var room = assembler.toModel(roomService.findByNameOrThrow(roomName));
        return ResponseEntity.ok(room);
    }

    @PostMapping
    @PreAuthorize("hasRole('Professor')")
    public ResponseEntity<?> createRoom(
            @RequestBody @Valid CreateRoomRequest roomRequest,
            @AuthenticationPrincipal AppUser userDetails) {
        log.info("Creating room with request: {} by user: {}", roomRequest, userDetails.getUsername());
        if (roomService.existsByName(roomRequest.getName())) {
            log.warn("Attempted to create a room with duplicate name: {}", roomRequest.getName());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error",
                    "Room with name " + roomRequest.getName() + " already exists."));
        }
        log.info("Creating room with name: {} by user: {}.", roomRequest.getName(), userDetails.getUsername());
        roomService.create(roomRequest, userDetails);
        URI location = linkTo(RoomController.class).slash(roomRequest.getName()).toUri();
        log.info("Room created successfully. Location: {}", location);
        return ResponseEntity.created(location).body(Map.of("message", "Room created successfully"));
    }

    @PatchMapping("/{roomName}")
    @PreAuthorize("hasRole('Professor')")
    public ResponseEntity<?> updateRoom(@RequestBody @Valid CreateRoomRequest roomRequest,
                                        @PathVariable String roomName,
                                        @AuthenticationPrincipal AppUser userDetails) {
        log.info("Updating room with name: {} by user: {}", roomName, userDetails.getUsername());
        Room room = roomService.findByNameOrThrow(roomName);
        checkUserCreatedRoom(room, userDetails, "update" + roomName);

        if (roomRequest.getTime().isEqual(room.getTime()) && roomRequest.getName().equals(roomName)) {
            log.info("Nothing changed. Room time remains the same.");
            return ResponseEntity.status(HttpStatus.OK)
                    .body(Map.of("message", "Nothing changed"));
        }
        roomService.update(roomRequest, room);
        log.info("Room updated successfully. Name: {}", roomName);
        return ResponseEntity.ok().body(Map.of("message", "Room updated successfully"));
    }

    @DeleteMapping("/{roomName}")
    @PreAuthorize("hasRole('Professor')")
    ResponseEntity<?> deleteByName(@PathVariable String roomName, @AuthenticationPrincipal AppUser userDetails) {
        log.info("Deleting room with name: {} by user: {}", roomName, userDetails.getUsername());
        Room room = roomService.findByNameOrThrow(roomName);
        checkUserCreatedRoom(room, userDetails, "delete" + roomName);
        roomService.delete(room);
        log.info("Room deleted successfully. Name: {}", roomName);
        return ResponseEntity.ok().body(Map.of("message", "Room deleted successfully"));
    }

    @GetMapping("/{roomName}/token")
    @PreAuthorize("hasRole('Professor') || hasRole('Student')")
    public ResponseEntity<TokenResponse> getRoomToken(@PathVariable String roomName,
                                                      @AuthenticationPrincipal AppUser userDetails) {
        log.info("Fetching token for room: {} by user: {}", roomName, userDetails.getUsername());
        Room room = roomService.findByNameOrThrow(roomName);
        if (room.getBannedUsers().contains(userDetails)) {
            log.warn("Access denied. User {} is banned from accessing room {}.", userDetails.getUsername(), roomName);
            throw new AccessDeniedException("get token");
        }
        TokenResponse tokenResponse = liveKitService.createAccessToken(room, userDetails);

        log.info("Token fetched successfully for room: {}. User: {}", roomName, userDetails.getUsername());
        return ResponseEntity.ok(tokenResponse);
    }

    @GetMapping("/{roomName}/participants")
    @PreAuthorize("hasRole('Professor') || hasRole('Student')")
    public ResponseEntity<CollectionModel<ParticipantDto>> getParticipants(@PathVariable String roomName) {
        log.info("Fetching participants for room: {}", roomName);
        roomService.findByNameOrThrow(roomName);
        var participantsInfo = liveKitService.getParticipantList(roomName).stream()
                .map(participant -> new ParticipantDto(
                        participant.getName(),
                        participant.getIdentity()
                ))
                .toList();
        Link link = linkTo(RoomController.class).withSelfRel();
        CollectionModel<ParticipantDto> result = CollectionModel.of(participantsInfo, link);
        log.info("Participants fetched successfully for room: {}", roomName);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{roomName}/participants/{participantIdentity}/canPublish")
    @PreAuthorize("hasRole('Professor')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    content = {@Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"string\"}"))})})
    public ResponseEntity<?> changePublishPermission
            (@PathVariable String roomName,
             @PathVariable String participantIdentity,
             @RequestBody @Valid CanPublishRequest req,
             @AuthenticationPrincipal AppUser userDetails) {
        log.info("Changing publish permission for participant in room: {} by user: {}", roomName, userDetails.getUsername());
        Room room = roomService.findByNameOrThrow(roomName);
        checkUserCreatedRoom(room, userDetails, "change publish permission for participant");
        liveKitService.changePublishPermission(roomName, participantIdentity, req.isCanPublish());
        log.info("Publish permission changed successfully for participant in room: {}. User: {}", roomName, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("message", "Participant permission changed"));
    }

    @PostMapping("/{roomName}/participants/{participantIdentity}/mute")
    @PreAuthorize("hasRole('Professor')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    content = {@Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"string\"}"))})})
    public ResponseEntity<?> muteParticipant
            (@PathVariable String roomName,
             @PathVariable String participantIdentity,
             @RequestBody @Valid muteRequest req,
             @AuthenticationPrincipal AppUser userDetails) {
        log.info("Muting participant in room: {} by user: {}", roomName, userDetails.getUsername());
        Room room = roomService.findByNameOrThrow(roomName);
        checkUserCreatedRoom(room, userDetails, "mute participant");
        liveKitService.muteParticipant(roomName,
                participantIdentity,
                req.isMute());
        log.info("Participant muted successfully in room: {}. User: {}", roomName, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("message", "Participant was muted"));
    }

    @PostMapping("/{roomName}/participants/{participantIdentity}/expel")
    @PreAuthorize("hasRole('Professor')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    content = {@Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"string\"}"))})})
    public ResponseEntity<?> expelParticipant
            (@PathVariable String roomName,
             @PathVariable String participantIdentity,
             @AuthenticationPrincipal AppUser userDetails) {
        log.info("Expelling participant in room: {} by user: {}", roomName, userDetails.getUsername());
        Room room = roomService.findByNameOrThrow(roomName);
        checkUserCreatedRoom(room, userDetails, "expel participant");
        liveKitService.expelParticipant(roomName, participantIdentity);
        roomService.banUser(roomName, participantIdentity);
        log.info("Participant expelled successfully in room: {}. User: {}", roomName, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("message", "Participant was expelled"));
    }

    private void checkUserCreatedRoom(@NotNull Room room, @NotNull AppUser user, String command) {
        if (!room.getBroadcaster().getId().equals(user.getId())) {
            log.warn("Access denied for user {} to {} room {}.", user.getUsername(), command, room.getName());
            throw new AccessDeniedException(command);
        }
    }
}