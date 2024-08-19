package com.in3rovert_so.securedoc.dtorequest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class updateUserPasswordRequest {
    @NotEmpty(message = "Password cannot be empty or null")
    private String password;
    @NotEmpty(message = "New Password cannot be empty or null")
    private String newPassword;
    @NotEmpty(message = "Confirm new Password cannot be empty or null")
    private String confirmNewPassword;
}
