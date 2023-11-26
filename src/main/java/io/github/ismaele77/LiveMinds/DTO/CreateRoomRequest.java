package io.github.ismaele77.LiveMinds.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.sql.Date;
import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoomRequest {
    @NotBlank
    private String program;

    @NotBlank
    private String course;

    @NotBlank
    private String professorClass;

    @NotNull
    private LocalDateTime time;

    public String getName(){
        return program + "_" + course + "_" + professorClass;
    }
}
