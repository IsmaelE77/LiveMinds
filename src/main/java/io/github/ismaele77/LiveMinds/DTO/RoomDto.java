package io.github.ismaele77.LiveMinds.DTO;

import io.github.ismaele77.LiveMinds.Model.Room;
import lombok.Data;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.time.LocalDateTime;

@Data
@Relation(collectionRelation = "rooms") // to change name in findAll in room controller
public class RoomDto extends RepresentationModel<RoomDto> {
    private String name;
    private String program;
    private String course;
    private String professorClass;
    private LocalDateTime time;
    private String status;
    private String broadcasterName;

    public RoomDto(){
    }
    public RoomDto(Room room){
        createNewRoomDto(room);
    }
    public void createNewRoomDto(Room room){
        this.name = room.getName() ;
        this.program = room.getProgram();
        this.course = room.getCourse();
        this.professorClass = room.getProfessorClass();
        this.time = room.getTime();
        this.status = room.getStatus();
        this.broadcasterName = room.getBroadcaster().getName();
    }
}

