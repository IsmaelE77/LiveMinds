package io.github.ismaele77.liveminds.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@AllArgsConstructor
public class muteRequest {
    @NotNull
    private boolean mute;
}
