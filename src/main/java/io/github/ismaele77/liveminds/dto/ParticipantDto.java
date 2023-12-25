package io.github.ismaele77.liveminds.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ParticipantDto {
    private String name;
    private String identity;
}
