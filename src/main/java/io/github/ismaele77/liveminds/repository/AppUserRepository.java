package io.github.ismaele77.liveminds.repository;

import io.github.ismaele77.liveminds.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Integer> {
    Optional<AppUser> findByUserName(String userName);
}