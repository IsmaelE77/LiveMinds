package io.github.ismaele77.liveminds.repository;

import io.github.ismaele77.liveminds.model.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface RoomRepository extends CrudRepository<Room, Integer>,
        JpaSpecificationExecutor<Room> {

    Page<Room> findAll(Pageable pageable);

    boolean existsByName(String roomName);

    @Transactional
    @Modifying
    @Query("delete from Room r where r.name = ?1")
    void deleteByName(String name);

    Optional<Room> findByName(String roomName);
}