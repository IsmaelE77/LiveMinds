package io.github.ismaele77.liveminds.dto;

import io.github.ismaele77.liveminds.model.Room;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Relation(collectionRelation = "rooms") // to change name in findAll in room controller
public class RoomDto extends RepresentationModel<RoomDto> {
    private String name;
    private String program;
    private String course;
    private String professorClass;
    private OffsetDateTime time;
    private String status;
    private String broadcasterName;

    public RoomDto(Room room) {
        mapFromRoom(room);
    }

    public void mapFromRoom(Room room) {
        this.name = room.getName();
        this.program = room.getProgram();
        this.course = room.getCourse();
        this.professorClass = room.getProfessorClass();
        this.time = room.getTime();
        this.status = room.getStatus();
        this.broadcasterName = room.getBroadcaster().getName();
    }
}

