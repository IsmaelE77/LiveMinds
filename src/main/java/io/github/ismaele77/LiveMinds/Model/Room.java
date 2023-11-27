package io.github.ismaele77.LiveMinds.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class Room extends RepresentationModel<Room>{

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
    private String ProfessorClass;

    @NotNull
    private LocalDateTime time;

    @NotBlank
    private String status;

    @NotNull
    @ManyToOne
    @JoinColumn(name="broadcaster_id")
    private AppUser broadcaster;

}
