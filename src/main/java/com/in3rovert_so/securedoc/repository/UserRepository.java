package com.in3rovert_so.securedoc.repository;

import com.in3rovert_so.securedoc.entity.UserEntity;
import org.apache.catalina.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity>findByEmailIgnoreCase(String email);

    Optional<UserEntity>findUserByUserId(String userId);
}
