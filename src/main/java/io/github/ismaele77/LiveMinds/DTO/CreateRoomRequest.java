package io.github.ismaele77.LiveMinds.DTO;

import io.github.ismaele77.LiveMinds.Model.AppUser;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

@Setter
@Getter
public class CreateRoomRequest {
    @NotBlank
    private String program;

    @NotBlank
    private String course;

    @NotBlank
    private String professorClass;

    @NotNull
    private Date time;

    @NotNull
    private Long broadcasterId;

    public String getName(){
        return program + "_" + course + "_" + professorClass;
    }
}
