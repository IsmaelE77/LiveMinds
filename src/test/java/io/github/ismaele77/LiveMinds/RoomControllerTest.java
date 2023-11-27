package io.github.ismaele77.LiveMinds;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ismaele77.LiveMinds.Controller.RoomController;
import io.github.ismaele77.LiveMinds.DTO.CreateRoomRequest;
import io.github.ismaele77.LiveMinds.Enum.RoomStatus;
import io.github.ismaele77.LiveMinds.Model.AppUser;
import io.github.ismaele77.LiveMinds.Model.Role;
import io.github.ismaele77.LiveMinds.Model.Room;
import io.github.ismaele77.LiveMinds.Repository.AppUserRepository;
import io.github.ismaele77.LiveMinds.Repository.RoomRepository;
import io.github.ismaele77.LiveMinds.Service.RoomLiveKitService;
import io.github.ismaele77.LiveMinds.Service.RoomService;
import io.livekit.server.AccessToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.hateoas.MediaTypes.HAL_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(RoomController.class)
public class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RoomRepository roomRepository;

    @MockBean
    private AppUserRepository appUserRepository;

    @MockBean
    RoomLiveKitService roomLiveKit;

    @MockBean
    AccessToken accessToken;

    @MockBean
    RoomService roomService;

    private String roomName;
    private Role adminRole;
    private AppUser admin1;
    private AppUser admin2;
    private Room room1;
    private Room room2;
    private  CreateRoomRequest createRoomRequest;

    @BeforeEach
    public void init(){
        roomName = "TestRoom";
        adminRole = new Role(1L,"Professor");
        admin1 = new AppUser(1L,"Ahmed_190735","Ahmed","ahmed@gmail.com","password", adminRole,null);
        admin2 = new AppUser(2L,"Samer_190734","Samer","samer@gmail.com","password", adminRole,null);
        room1 = new Room(1,"BNA401_ITE_c1","ITE","BNA401","c1", LocalDateTime.of(2023, 11, 26, 12, 30),RoomStatus.NOT_STARTED.getValue(), admin1 ,Collections.emptyList());
        room2 = new Room(2,"BNA401_ITE_c1","ITE","BNA401","c1",LocalDateTime.of(2023, 11, 26, 12, 30),RoomStatus.NOT_STARTED.getValue(), admin2,Collections.emptyList());
        createRoomRequest = new CreateRoomRequest("ITE","BNA","C2", LocalDateTime.of(2023, 11, 12, 12, 30));
    }
    @Test
    @WithMockUser(roles = "Student")
    public void testFindAllRooms() throws Exception {
        Mockito.when(roomRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/rooms"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "Student")
    public void testFindRoomByName() throws Exception {
        Mockito.when(roomRepository.findByName(roomName)).thenReturn(Optional.of(room1));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/rooms/{roomName}", roomName))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content().contentType(HAL_JSON))
                .andExpect(jsonPath("$.name").value(room1.getName()));
    }

    @Test
    @WithMockUser(roles = "Student")
    public void testFindRoomByNameNotFound() throws Exception {
        String roomName = "NonExistentRoom";

        Mockito.when(roomRepository.findByName(roomName)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/rooms/{roomName}", roomName))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "Professor")
    public void testCreateRoom() throws Exception {
        Mockito.when(roomRepository.existsByName(roomName)).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/rooms")
                        .with(user(admin1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRoomRequest)))
                .andExpect(content().string("Room created successfully"))
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    @WithMockUser(roles = "Professor")
    public void testCreateRoomConflict() throws Exception {
        Mockito.when(roomRepository.existsByName(createRoomRequest.getName())).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/rooms")
                        .with(user(admin1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRoomRequest)))
                .andExpect(MockMvcResultMatchers.status().isConflict());
    }

    @Test
    @WithMockUser(roles = "Professor")
    public void testUpdateRoom() throws Exception {
        Mockito.when(roomRepository.existsByName(createRoomRequest.getName())).thenReturn(true);
        Mockito.when(roomRepository.findByName(roomName)).thenReturn(Optional.of(room1));
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/rooms/{roomName}", roomName)
                        .with(user(admin1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRoomRequest)))
                .andExpect(content().string("Room updated successfully"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "Professor")
    public void testUpdateRoomNotFound() throws Exception {
        Mockito.when(roomRepository.existsByName(createRoomRequest.getName())).thenReturn(false);
        Mockito.when(roomRepository.findByName(roomName)).thenReturn(Optional.empty());
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/rooms/{roomName}", roomName)
                        .with(user(admin1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRoomRequest)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "Professor")
    public void testUpdateRoomWithNotPermission() throws Exception {
        Mockito.when(roomRepository.existsByName(createRoomRequest.getName())).thenReturn(true);
        Mockito.when(roomRepository.findByName(roomName)).thenReturn(Optional.of(room1));
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/rooms/{roomName}", roomName)
                        .with(user(admin2))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRoomRequest)))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "Professor")
    public void testUpdateRoomWithNotChange() throws Exception {
        LocalDateTime specificDateTime = LocalDateTime.of(2023, 11, 26, 12, 30);
        Room room = new Room(1,"BNA401_ITE_c1","ITE","BNA401","c1",specificDateTime, RoomStatus.NOT_STARTED.getValue() ,admin1 ,Collections.emptyList());
        CreateRoomRequest createRoomRequest = new CreateRoomRequest("ITE","BNA401","c1",specificDateTime);
        Mockito.when(roomRepository.existsByName(createRoomRequest.getName())).thenReturn(true);
        Mockito.when(roomRepository.findByName(roomName)).thenReturn(Optional.of(room));
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/rooms/{roomName}", roomName)
                        .with(user(admin1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRoomRequest)))
                .andExpect(content().string("Nothing change"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "Professor")
    public void testDeleteRoom() throws Exception {
        Mockito.when(roomRepository.findByName(roomName)).thenReturn(Optional.of(room1));
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/rooms/{roomName}", roomName)
                        .with(user(admin1)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "Professor")
    public void testDeleteRoomNotFound() throws Exception {
        String roomName = "NonExistentRoom";

        Mockito.when(roomRepository.findByName(roomName)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/rooms/{roomName}", roomName))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "Professor")
    public void testDeleteRoomWithNotPermission() throws Exception {
        String roomName = "NonExistentRoom";

        Mockito.when(roomRepository.findByName(roomName)).thenReturn(Optional.of(room1));

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/rooms/{roomName}", roomName)
                        .with(user(admin2)))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "Student")
    public void testGetToken() throws Exception {
        Mockito.when(roomRepository.findByName(roomName)).thenReturn(Optional.of(room1));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/rooms/{roomName}/token", roomName)
                        .with(user(admin1)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "Student")
    public void testGetTokenForBannedUser() throws Exception {
        var admin = new AppUser(2L,"Samer_190734","Samer","samer@gmail.com","password", adminRole,null);
        var room = new Room(1,"BNA401_ITE_c1","ITE","BNA401","c1", LocalDateTime.of(2023, 11, 26, 12, 30),RoomStatus.NOT_STARTED.getValue(), admin1 , List.of(admin));
        Mockito.when(roomRepository.findByName(roomName)).thenReturn(Optional.of(room));
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/rooms/{roomName}/token", roomName)
                        .with(user(admin)))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "Student")
    public void getParticipantsTest() throws Exception{
        Mockito.when(roomRepository.existsByName(roomName)).thenReturn(true);
        Mockito.when(roomLiveKit.getParticipantList(roomName)).thenReturn(Collections.emptyList());
        mockMvc.perform(MockMvcRequestBuilders
                    .get("/api/v1/rooms/{roomName}/participants", roomName)
                    .contentType(HAL_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "Professor")
    public void changePublishPermissionTest() throws Exception{
        Mockito.when(roomRepository.findByName(roomName)).thenReturn(Optional.of(room1));

        mockMvc.perform( MockMvcRequestBuilders
                    .post("/api/v1/rooms/{roomName}/participants/{participantIdentity}/canPublish", roomName, "participant123")
                    .contentType(HAL_JSON)
                    .content("true")
                    .with(user(admin1)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "Professor")
    public void muteParticipantTest() throws Exception{
        Mockito.when(roomRepository.findByName(roomName)).thenReturn(Optional.of(room1));

        mockMvc.perform( MockMvcRequestBuilders
                        .post("/api/v1/rooms/{roomName}/participants/{participantIdentity}/mute", roomName, "participant123")
                        .contentType(HAL_JSON)
                        .content("true")
                        .with(user(admin1)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "Professor")
    public void expelParticipantTest() throws Exception{
        Mockito.when(roomRepository.findByName(roomName)).thenReturn(Optional.of(room1));

        mockMvc.perform( MockMvcRequestBuilders
                        .post("/api/v1/rooms/{roomName}/participants/{participantIdentity}/expel", roomName, "participant123")
                        .contentType(HAL_JSON)
                        .with(user(admin1)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }



}