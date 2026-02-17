package com.irrigo.usermanagementservice.repositories;

import com.irrigo.usermanagementservice.entities.ERole;
import com.irrigo.usermanagementservice.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name);
}