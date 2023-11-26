package io.github.ismaele77.LiveMinds.DTO;

import io.github.ismaele77.LiveMinds.Model.Room;
import lombok.Data;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

@Data
public class RoomDto extends RepresentationModel<RoomDto> {
    private String name;
    private String program;
    private String course;
    private String professorClass;
    private LocalDateTime time;
    private String broadcasterName;

    public RoomDto(){
    }
    public RoomDto(Room room){
        CreateNewUserDto(room);
    }
    public void CreateNewUserDto(Room room){
        this.name = room.getName() ;
        this.program = room.getProgram();
        this.course = room.getCourse();
        this.professorClass = room.getProfessorClass();
        this.time = room.getTime();
        this.broadcasterName = room.getBroadcaster().getName();
    }
}

