package io.github.ismaele77.LiveMinds.Repository;

import io.github.ismaele77.LiveMinds.Model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
}