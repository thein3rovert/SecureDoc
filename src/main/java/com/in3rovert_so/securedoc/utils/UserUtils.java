package com.in3rovert_so.securedoc.utils;

import com.in3rovert_so.securedoc.entity.RoleEntity;
import com.in3rovert_so.securedoc.entity.UserEntity;

import java.util.UUID;

import static java.time.LocalDateTime.now;
import static org.apache.logging.log4j.util.Strings.EMPTY;

public class UserUtils {
    public static UserEntity createUserEntity(String firstName, String lastName, String email, RoleEntity role) {
        return UserEntity.builder()
                .userId(UUID.randomUUID().toString())
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .lastLogin(now())
                .accountNonExpired(true)
                .accountNonLocked(true)
                .mfa(false)
                .enabled(false)
                .loginAttempts(0)
                .qrCodeSecret(EMPTY)
                .phone(EMPTY)
                .bio(EMPTY)
                .ImageUrl("")
                .role(role)
                .build();

    } //Todo: Create the method
}
