package io.github.ismaele77.liveminds.dto;

import io.github.ismaele77.liveminds.model.AppUser;
import io.github.ismaele77.liveminds.model.Room;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.server.core.Relation;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Relation(collectionRelation = "users")
public class UserDto {
    private String name;
    private String userName;
    private String email;
    private String role;

    public void mapFromAppUser(AppUser user) {
        this.name = user.getName();
        this.userName = user.getUsername();
        this.email = user.getEmail();
        this.role = user.getRole().getName();
    }
}
