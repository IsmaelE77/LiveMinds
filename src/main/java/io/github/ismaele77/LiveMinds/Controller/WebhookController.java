package io.github.ismaele77.LiveMinds.Controller;

import io.github.ismaele77.LiveMinds.Enum.RoomStatus;
import io.github.ismaele77.LiveMinds.Exception.RoomNotFoundException;
import io.github.ismaele77.LiveMinds.Repository.RoomRepository;
import io.livekit.server.WebhookReceiver;
import livekit.LivekitModels;
import livekit.LivekitWebhook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook-endpoint")
public class WebhookController {

    private final WebhookReceiver webhookReceiver;
    private final RoomRepository roomRepository;

    private static Logger logger = LoggerFactory.getLogger(WebhookController.class);

    public WebhookController(WebhookReceiver webhookReceiver, RoomRepository roomRepository) {
        this.webhookReceiver = webhookReceiver;
        this.roomRepository = roomRepository;
    }

    @PostMapping
    public void handleWebhook(@RequestBody String postBody , @RequestHeader("Authorization") String authorizationHeader) {
        LivekitWebhook.WebhookEvent event = webhookReceiver.receive(postBody, authorizationHeader);

        if(event.getEvent().equals("room_started")){
            LivekitModels.Room room = event.getRoom();
            logger.info("Received webhook event: {}", event.getEvent());
            logger.info("the room : "+room.getName()+" started");
        } else if(event.getEvent().equals("room_finished")){
            LivekitModels.Room liveKitRoom = event.getRoom();
            logger.info("Received webhook event: {}", event.getEvent());
            var room = roomRepository.findByName(liveKitRoom.getName())
                    .orElseThrow(() -> new RoomNotFoundException(liveKitRoom.getName()));
            if(room.getStatus().equals(RoomStatus.FINISHED.getValue())
            && liveKitRoom.getNumParticipants() == 0){
                roomRepository.deleteByName(room.getName());
                logger.info("the room : "+room.getName()+" deleted");
            }
        }else if(event.getEvent().equals("participant_joined")){
            logger.info("Received webhook event: {}", event.getEvent());
            var user = event.getParticipant();
            var room = roomRepository.findByName(event.getRoom().getName())
                    .orElseThrow(() -> new RoomNotFoundException(event.getRoom().getName()));
            if(room.getBroadcaster().getUsername().equals(user.getIdentity())){
                room.setStatus(RoomStatus.STREAMING.getValue());
                roomRepository.save(room);
            }
        }else if(event.getEvent().equals("participant_left")){
            logger.info("Received webhook event: {}", event.getEvent());
            var user = event.getParticipant();
            var room = roomRepository.findByName(event.getRoom().getName())
                    .orElseThrow(() -> new RoomNotFoundException(event.getRoom().getName()));
            if(room.getBroadcaster().getUsername().equals(user.getIdentity())){
                room.setStatus(RoomStatus.FINISHED.getValue());
                roomRepository.save(room);
            }
        }
    }

    private void changeRoomStatus(RoomStatus roomStatus){

    }
}
