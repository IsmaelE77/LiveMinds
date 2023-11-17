package io.github.ismaele77.LiveMinds.Model;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @NotBlank(message = "UserName is mandatory")
    private String userName;

    @NotBlank(message = "Name is mandatory")
    private String name;

    @NotNull
    @Email
    private String email;

    @NotNull
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z]).{8,}$")
    private String password;

    @NotNull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "role_id")
    private Set<Role> roles;


    @OneToMany(mappedBy = "broadcaster", cascade = CascadeType.ALL , fetch = FetchType.LAZY)
    private List<Room> rooms;
}
