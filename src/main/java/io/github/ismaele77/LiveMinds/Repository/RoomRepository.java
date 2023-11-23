package io.github.ismaele77.LiveMinds.Repository;

import io.github.ismaele77.LiveMinds.Model.Room;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.Optional;

public interface RoomRepository extends CrudRepository<Room, Integer> {
    boolean existsByName(String roomName);
    boolean deleteByName(String roomName);
    Optional<Room> findByName(String roomName);
}