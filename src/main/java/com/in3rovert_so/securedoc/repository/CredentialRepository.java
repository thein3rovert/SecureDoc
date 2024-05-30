package com.in3rovert_so.securedoc.repository;

import com.in3rovert_so.securedoc.entity.CredentialEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CredentialRepository extends JpaRepository<CredentialEntity, Long> {
    Optional<CredentialEntity> getCredentialEntitiesById(Long userId);
}
