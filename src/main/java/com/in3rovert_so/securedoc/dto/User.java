package com.in3rovert_so.securedoc.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.in3rovert_so.securedoc.entity.RoleEntity;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class User {
        private Long id;
        private Long createdBy;
        private Long updatedBy;
        private String userId;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String bio;
        private String ImageUrl;
        private String qrCodeImageUrl;
        private String lastLogin;
        private String createdAt;
        private String updatedAt;
        private String roles;
        private String authorities;
        private boolean accountNonExpired;
        private boolean accountNonLocked;
        private boolean credentialsNonExpired;
        private boolean enabled;
        private boolean mfa;
}
