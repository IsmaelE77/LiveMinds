package io.github.ismaele77.LiveMinds.Repository;

import io.github.ismaele77.LiveMinds.Model.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, String> {
}