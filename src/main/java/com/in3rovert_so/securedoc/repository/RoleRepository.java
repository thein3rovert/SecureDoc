package com.in3rovert_so.securedoc.repository;

import com.in3rovert_so.securedoc.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity>findByNameIgnoreCase(String name); //Optional because we might not find them.
}
