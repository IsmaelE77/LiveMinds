package io.github.ismaele77.LiveMinds.Controller;

import io.github.ismaele77.LiveMinds.Repository.RoomRepository;
import io.livekit.server.WebhookReceiver;
import livekit.LivekitModels;
import livekit.LivekitWebhook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook-endpoint")
@CrossOrigin(origins="*")
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
            LivekitModels.Room room = event.getRoom();
            logger.info("Received webhook event: {}", event.getEvent());
            roomRepository.deleteByName(room.getName());
            logger.info("the room : "+room.getName()+" deleted");
        }
    }
}
