package io.github.ismaele77.liveminds.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@JsonIgnoreProperties({"id", "broadcaster", "bannedUsers"})
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank
    private String name;

    @NotBlank
    private String program;

    @NotBlank
    private String course;

    @NotBlank
    private String professorClass;

    @NotNull
    private LocalDateTime time;

    @NotBlank
    private String status;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "broadcaster_id")
    private AppUser broadcaster;

    @ManyToMany
    @JoinTable(
            name = "room_banned_users",
            joinColumns = @JoinColumn(name = "room_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<AppUser> bannedUsers;

}
