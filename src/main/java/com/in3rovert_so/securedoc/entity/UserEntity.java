package com.in3rovert_so.securedoc.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_DEFAULT;

@Getter
@Setter
@ToString
@Builder //Need to know what this is for
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users") //Naming the table
@JsonInclude(NON_DEFAULT)

public class UserEntity extends Auditable {
    //Enforce for the userID to be Unique
    @Column(updatable = false, unique = true, nullable = false) // We cannot have a user without an id
    private String userId;
    private String firstName;
    private String lastName;
    @Column(unique = true, nullable = false) //We cannot have a user without an email also
    private String email;
    private Integer loginAttempts; //Using this to keep track of the user login so we can block when it exceeds a limit.
    private LocalDateTime lastLogin;
    private String phone;
    private String bio;
    private String imageUrl;

    // Fields needed for spring security.
    private boolean accountNonExpired; //This helps to load the user from database and use some of the values to create
    //a user details that we can pass into spring security so that spring security can do auth for us.
    private boolean accountNonLocked;
    private boolean enabled;
    private boolean mfa;

    @JsonIgnore
    private String qrCodeSecret;

    @Column(columnDefinition = "text")
    private String qrCodeImageUrl;

    @ManyToOne(fetch = FetchType.EAGER)
    //Many user can only have one role,  the Eager means when ever we load a user, we want
    //load their roles.
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(
                    name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(
                    name = "role_id", referencedColumnName = "id")
    )
    private RoleEntity role;

}
