package io.github.ismaele77.liveminds;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ismaele77.liveminds.controller.RoomController;
import io.github.ismaele77.liveminds.controller.RoomDtoModelAssembler;
import io.github.ismaele77.liveminds.dto.request.CreateRoomRequest;
import io.github.ismaele77.liveminds.enums.RoomStatus;
import io.github.ismaele77.liveminds.exception.RoomNotFoundException;
import io.github.ismaele77.liveminds.model.AppUser;
import io.github.ismaele77.liveminds.model.Role;
import io.github.ismaele77.liveminds.model.Room;
import io.github.ismaele77.liveminds.repository.AppUserRepository;
import io.github.ismaele77.liveminds.repository.RoomRepository;
import io.github.ismaele77.liveminds.service.LiveKitService;
import io.github.ismaele77.liveminds.service.RoomService;
import io.livekit.server.AccessToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.MediaType;

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

import static org.springframework.hateoas.MediaTypes.HAL_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(RoomController.class)
public class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    //controller
    @MockBean
    private RoomService roomService;
    @MockBean
    private RoomRepository roomRepository;
    @MockBean
    private AppUserRepository appUserRepository;
    @InjectMocks
    private RoomDtoModelAssembler assembler;
    @MockBean
    private PagedResourcesAssembler<Room> pagedResourcesAssembler;

    //LIVEKIT
    @MockBean
    LiveKitService roomLiveKit;
    @MockBean
    AccessToken accessToken;
    @MockBean
    io.livekit.server.RoomService liveKitRoomService;


    @MockBean
    CommandLineRunner commandLineRunner;

    private String roomName;
    private Role adminRole;
    private AppUser admin1;
    private AppUser admin2;
    private Room room1;
    private Room room2;
    private CreateRoomRequest createRoomRequest;


    @BeforeEach
    public void init() {
        roomName = "TestRoom";
        adminRole = new Role(1, "Professor");
        admin1 = new AppUser(
                1,
                "Ahmed_190735",
                "Ahmed",
                "ahmed@gmail.com",
                "password",
                adminRole,
                null);
        admin2 = new AppUser(2,
                "Samer_190734",
                "Samer",
                "samer@gmail.com",
                "password", adminRole,
                null);
        room1 = new Room(1,
                "BNA401_ITE_c1",
                "ITE",
                "BNA401",
                "c1",
                OffsetDateTime.of(2029, 11, 26, 12, 30,0,0, ZoneOffset.UTC),
                RoomStatus.NOT_STARTED.getValue(),
                admin1,
                Collections.emptyList());
        room2 = new Room(2,
                "BNA401_ITE_c1",
                "ITE",
                "BNA401",
                "c1",
                OffsetDateTime.of(2029, 11, 26, 12, 30,0,0, ZoneOffset.UTC),
                RoomStatus.NOT_STARTED.getValue(),
                admin2, Collections.emptyList());
        createRoomRequest = new CreateRoomRequest(
                "ITE",
                "BNA",
                "C2",
                OffsetDateTime.of(2029, 11, 26, 12, 30,0,0, ZoneOffset.UTC)
        );
    }

    @Test
    @WithMockUser(roles = "Student")
    public void testFindAllRooms() throws Exception {
        // Mock the service method
        Mockito.when(roomService.findAll(0, 15)).thenReturn(Page.empty());

        // Mock the paged resources assembler
        Mockito.when(pagedResourcesAssembler.toModel(Page.empty(), assembler)).thenReturn(PagedModel.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/rooms")
                        .param("page", "0")
                        .param("size", "15")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }


    @Test
    @WithMockUser(roles = "Student")
    public void testFindRoomByName() throws Exception {
        Mockito.when(roomService.findByNameOrThrow(room1.getName())).thenReturn(room1);
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/rooms/{roomName}", room1.getName())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(room1.getName()));
    }

    @Test
    @WithMockUser(roles = "Student")
    public void testFindRoomByNameNotFound() throws Exception {
        String roomName = "NonExistentRoom";

        Mockito.when(roomService.findByNameOrThrow(roomName)).thenThrow(new RoomNotFoundException(roomName));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/rooms/{roomName}", roomName))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "Professor")
    public void testCreateRoom() throws Exception {
        Mockito.when(roomService.existsByName(roomName)).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/rooms")
                        .with(user(admin1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRoomRequest)))
                .andExpect(content().json("{\n" +
                        "  \"message\": \"Room created successfully\"\n" +
                        "}"))
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    @WithMockUser(roles = "Professor")
    public void testCreateRoomConflict() throws Exception {
        Mockito.when(roomService.existsByName(createRoomRequest.getName())).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/rooms")
                        .with(user(admin1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRoomRequest)))
                .andExpect(MockMvcResultMatchers.status().isConflict());
    }

    @Test
    @WithMockUser(roles = "Professor")
    public void testUpdateRoom() throws Exception {
        Mockito.when(roomService.findByNameOrThrow(room1.getName())).thenReturn(room1);
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/rooms/{roomName}", room1.getName())
                        .with(user(admin1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRoomRequest)))
                .andExpect(content().json("{\n" +
                        "  \"message\": \"Room updated successfully\"\n" +
                        "}"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "Professor")
    public void testUpdateRoomNotFound() throws Exception {
        Mockito.when(roomService.existsByName(createRoomRequest.getName())).thenReturn(false);
        Mockito.when(roomService.findByNameOrThrow(roomName)).thenThrow(new RoomNotFoundException(createRoomRequest.getName()));
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/rooms/{roomName}", roomName)
                        .with(user(admin1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRoomRequest)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "Professor")
    public void testUpdateRoomWithNotPermission() throws Exception {
        Mockito.when(roomService.existsByName(createRoomRequest.getName())).thenReturn(true);
        Mockito.when(roomService.findByNameOrThrow(roomName)).thenReturn(room1);
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/rooms/{roomName}", roomName)
                        .with(user(admin2))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRoomRequest)))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "Professor")
    public void testUpdateRoomWithNotChange() throws Exception {
        OffsetDateTime specificDateTime = OffsetDateTime.of(2026, 11, 26, 12, 30,0,0, ZoneOffset.UTC);
        Room room = new Room(1, "ITE_BNA401_c1", "ITE", "BNA401", "c1", specificDateTime, RoomStatus.NOT_STARTED.getValue(), admin1, Collections.emptyList());
        CreateRoomRequest createRoomRequest = new CreateRoomRequest("ITE", "BNA401", "c1", specificDateTime);
        Mockito.when(roomService.findByNameOrThrow(room.getName())).thenReturn(room);
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/rooms/{roomName}", room.getName())
                        .with(user(admin1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRoomRequest)))
                .andExpect(content().json("{\n" +
                        "  \"message\": \"Nothing changed\"\n" +
                        "}"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "Professor")
    public void testDeleteRoom() throws Exception {
        Mockito.when(roomService.findByNameOrThrow(roomName)).thenReturn(room1);
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/rooms/{roomName}", roomName)
                        .with(user(admin1)))
                .andExpect(content().json("{\n" +
                        "  \"message\": \"Room deleted successfully\"\n" +
                        "}"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "Professor")
    public void testDeleteRoomNotFound() throws Exception {
        String roomName = "NonExistentRoom";

        Mockito.when(roomService.findByNameOrThrow(roomName)).thenThrow(new RoomNotFoundException(roomName));

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/rooms/{roomName}", roomName)
                        .with(user(admin1)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "Professor")
    public void testDeleteRoomWithNotPermission() throws Exception {
        Mockito.when(roomService.findByNameOrThrow(roomName)).thenReturn(room1);
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/rooms/{roomName}", roomName)
                        .with(user(admin2)))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "Student")
    public void testDeleteRoomWithNotPermissionStudentUser() throws Exception {
        Mockito.when(roomService.findByNameOrThrow(roomName)).thenReturn(room1);
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/rooms/{roomName}", roomName))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "Student")
    public void testGetToken() throws Exception {
        Mockito.when(roomService.findByNameOrThrow(roomName)).thenReturn(room1);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/rooms/{roomName}/token", roomName)
                        .with(user(admin1)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "Student")
    public void testGetTokenForBannedUser() throws Exception {
        var admin = new AppUser(2, "Samer_190734", "Samer", "samer@gmail.com", "password", adminRole, null);
        var room = new Room(1, "BNA401_ITE_c1", "ITE", "BNA401", "c1",OffsetDateTime.of(2024, 11, 26, 12, 30,0,0, ZoneOffset.UTC), RoomStatus.NOT_STARTED.getValue(), admin1, List.of(admin));
        Mockito.when(roomService.findByNameOrThrow(roomName)).thenReturn(room);
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/rooms/{roomName}/token", roomName)
                        .with(user(admin)))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "Student")
    public void getParticipantsTest() throws Exception {
        Mockito.when(roomService.existsByName(roomName)).thenReturn(true);
        Mockito.when(roomLiveKit.getParticipantList(roomName)).thenReturn(Collections.emptyList());
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/rooms/{roomName}/participants", roomName)
                        .contentType(HAL_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "Professor")
    public void changePublishPermissionTest() throws Exception {
        Mockito.when(roomService.findByNameOrThrow(roomName)).thenReturn(room1);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/rooms/{roomName}/participants/{participantIdentity}/canPublish", roomName, "participant123")
                        .contentType(HAL_JSON)
                        .content("true")
                        .with(user(admin1)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "Professor")
    public void muteParticipantTest() throws Exception {
        Mockito.when(roomService.findByNameOrThrow(roomName)).thenReturn(room1);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/rooms/{roomName}/participants/{participantIdentity}/mute", roomName, "participant123")
                        .contentType(HAL_JSON)
                        .content("true")
                        .with(user(admin1)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "Professor")
    public void expelParticipantTest() throws Exception {
        Mockito.when(roomService.findByNameOrThrow(roomName)).thenReturn(room1);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/rooms/{roomName}/participants/{participantIdentity}/expel", roomName, "participant123")
                        .contentType(HAL_JSON)
                        .with(user(admin1)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }


}