package com.in3rovert_so.securedoc.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@Table(name = "roles") //Naming the table in the database
@JsonInclude(NON_DEFAULT)
public class RoleEntity extends Auditable{
    private String name;
    private String authorities; // Because we are going  to need to define authorires for each roles and they will be ENUM.
}
