package io.github.ismaele77.LiveMinds.DTO;

import lombok.Data;
import org.springframework.hateoas.RepresentationModel;

@Data
public class ParticipantDto {
    private String name;
    private String identity;
}
