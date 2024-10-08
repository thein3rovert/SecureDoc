package com.in3rovert_so.securedoc.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.in3rovert_so.securedoc.enumeration.Authority;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_DEFAULT;

@Getter
@Setter
@ToString
@Builder //Need to know what this is for
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
@JsonInclude(NON_DEFAULT)
public class RoleEntity extends Auditable{
    private String name;
    private Authority authorities;
}
