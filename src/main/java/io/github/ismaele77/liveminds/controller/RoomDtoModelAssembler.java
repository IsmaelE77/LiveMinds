package io.github.ismaele77.liveminds.controller;

import io.github.ismaele77.liveminds.dto.RoomDto;
import io.github.ismaele77.liveminds.model.Room;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
public class RoomDtoModelAssembler extends RepresentationModelAssemblerSupport<Room, RoomDto> {
    public RoomDtoModelAssembler() {
        super(RoomController.class, RoomDto.class);
    }

    @Override
    public RoomDto toModel(Room entity) {
        RoomDto model = new RoomDto();
        // Both CustomerModel and Customer have the same property names. So copy the values from the Entity to the Model
        model.mapFromRoom(entity);
        Link selfLink = linkTo(RoomController.class).slash(entity.getName()).withSelfRel();
        model.add(selfLink);
        return model;
    }
}
