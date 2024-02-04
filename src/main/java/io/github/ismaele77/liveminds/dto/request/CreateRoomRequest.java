package io.github.ismaele77.liveminds.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class CreateRoomRequest {
    @NotBlank
    private String program;

    @NotBlank
    private String course;

    @NotBlank
    private String professorClass;

    @NotNull
    @Future(message = "The time must be in the future")
    private OffsetDateTime time;

    public String getName() {
        return program + "_" + course + "_" + professorClass;
    }
}
