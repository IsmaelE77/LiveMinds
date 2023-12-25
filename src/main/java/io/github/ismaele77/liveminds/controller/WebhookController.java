package io.github.ismaele77.liveminds.controller;

import io.github.ismaele77.liveminds.enums.RoomStatus;
import io.github.ismaele77.liveminds.exception.RoomNotFoundException;
import io.github.ismaele77.liveminds.service.LiveKitService;
import io.github.ismaele77.liveminds.service.RoomService;
import io.livekit.server.WebhookReceiver;
import livekit.LivekitWebhook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook-endpoint")
public class WebhookController {

    private final WebhookReceiver webhookReceiver;
    private final RoomService roomService;
    private final LiveKitService liveKitService;
    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);

    public WebhookController(WebhookReceiver webhookReceiver, RoomService roomService, LiveKitService liveKitService) {
        this.webhookReceiver = webhookReceiver;
        this.roomService = roomService;
        this.liveKitService = liveKitService;
    }

    @PostMapping
    public void handleWebhook(@RequestBody String postBody, @RequestHeader("Authorization") String authorizationHeader) {
        LivekitWebhook.WebhookEvent event = webhookReceiver.receive(postBody, authorizationHeader);
        String roomName = event.getRoom().getName();

        logger.info("Received webhook event: {}", event.getEvent());

        switch (event.getEvent()) {
            case "room_started":
                handleRoomStarted(roomName);
                break;
            case "room_finished":
                handleRoomFinished(event);
                break;
            case "participant_joined":
                handleParticipantJoined(event);
                break;
            case "participant_left":
                handleParticipantLeft(event);
                break;
            default:
                logger.warn("Unhandled webhook event: {}", event.getEvent());
        }
    }

    private void handleRoomStarted(String roomName) {
        logger.info("The room {} started", roomName);
    }

    private void handleRoomFinished(LivekitWebhook.WebhookEvent event) {
        String roomName = event.getRoom().getName();
        var room = roomService.findByNameOrThrow(roomName);

        if (room.getStatus().equals(RoomStatus.FINISHED.getValue())
                && event.getRoom().getNumParticipants() == 0) {
            liveKitService.deleteRoom(room.getName());
            roomService.deleteByName(room.getName());
            logger.info("The room {} deleted", roomName);
        }
    }

    private void handleParticipantJoined(LivekitWebhook.WebhookEvent event) {
        var user = event.getParticipant();
        String roomName = event.getRoom().getName();

        var room = roomService.findByNameOrThrow(roomName);

        if (room.getBroadcaster().getUsername().equals(user.getIdentity())) {
            room.setStatus(RoomStatus.STREAMING.getValue());
            roomService.save(room);
        }
    }

    private void handleParticipantLeft(LivekitWebhook.WebhookEvent event) {
        var user = event.getParticipant();
        String roomName = event.getRoom().getName();

        var room = roomService.findByName(roomName)
                .orElseThrow(() -> new RoomNotFoundException(roomName));

        if (room.getBroadcaster().getUsername().equals(user.getIdentity())) {
            room.setStatus(RoomStatus.FINISHED.getValue());
            roomService.save(room);
        }
    }
}
