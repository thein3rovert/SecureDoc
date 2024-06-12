package com.in3rovert_so.securedoc.dtorequest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginRequest {
        //Fields for Login request
        @NotEmpty(message = "Email cannot be empty or null")
        @Email(message = "Invalid email address")
        private String email;
        @NotEmpty(message = "Password cannot be empty or null")
        private String password;
}
