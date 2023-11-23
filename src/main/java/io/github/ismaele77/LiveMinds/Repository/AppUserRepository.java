package io.github.ismaele77.LiveMinds.Repository;

import io.github.ismaele77.LiveMinds.Model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUserName(String userName);
}