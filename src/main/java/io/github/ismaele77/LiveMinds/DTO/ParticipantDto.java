package io.github.ismaele77.LiveMinds.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

@Data
@AllArgsConstructor
public class ParticipantDto {
    private String name;
    private String identity;
}
