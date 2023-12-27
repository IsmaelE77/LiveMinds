package io.github.ismaele77.liveminds.specifications;

import io.github.ismaele77.liveminds.model.Room;
import org.springframework.data.jpa.domain.Specification;


public class RoomSpecifications {

    public static Specification<Room> addSpecificationByKey(String key, String value, Specification<Room> specification) {
        return switch (key) {
            case "name" -> specification.and(byName(value));
            case "program" -> specification.and(byProgram(value));
            case "professorClass" -> specification.and(byProfessorClass(value));
            case "status" -> specification.and(byStatus(value));
            case "broadcasterName" -> specification.and(byBroadcasterName(value));
            default -> specification;
        };
    }

    public static Specification<Room> byName(String name) {
        return (root, query, builder) -> builder.like(builder.upper(root.get("name")), "%" + name.toUpperCase() + "%");
    }

    public static Specification<Room> byProgram(String program) {
        return (root, query, builder) -> builder.like(builder.upper(root.get("program")), "%" + program.toUpperCase() + "%");
    }

    public static Specification<Room> byCourse(String course) {
        return (root, query, builder) -> builder.like(builder.upper(root.get("course")), "%" + course.toUpperCase() + "%");
    }

    public static Specification<Room> byProfessorClass(String professorClass) {
        return (root, query, builder) -> builder.like(builder.upper(root.get("professorClass")), "%" + professorClass.toUpperCase() + "%");
    }

    public static Specification<Room> byStatus(String status) {
        return (root, query, builder) -> builder.like(builder.upper(root.get("status")), "%" + status.toUpperCase() + "%");
    }

    public static Specification<Room> byBroadcasterName(String broadcasterName) {
        return (root, query, builder) -> builder.like(builder.upper(root.get("broadcaster").get("name")), "%" + broadcasterName.toUpperCase() + "%");
    }
}
