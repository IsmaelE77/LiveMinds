package io.github.ismaele77.LiveMinds.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDto {
    @NotBlank
    private String Username;
    @NotBlank
    private String password;
}
