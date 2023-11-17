package io.github.ismaele77.LiveMinds.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Room {

    @Id
    @NotBlank
    private String name;

    @NotBlank
    private String program;

    @NotBlank
    private String course;

    @NotBlank
    private String ProfessorClass;

    @NotBlank
    private Date time;

    @NotNull
    @ManyToOne
    @JoinColumn(name="broadcaster_id")
    private AppUser broadcaster;

}
