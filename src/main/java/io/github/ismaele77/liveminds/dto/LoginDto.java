package io.github.ismaele77.liveminds.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginDto {
    @NotBlank
    private String usernameOrEmail;
    @NotBlank
    private String password;
}
