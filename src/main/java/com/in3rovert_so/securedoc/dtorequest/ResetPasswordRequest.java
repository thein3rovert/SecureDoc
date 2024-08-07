package com.in3rovert_so.securedoc.dtorequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResetPasswordRequest {
    //Fields for user request
    @NotEmpty(message = "User Id cannot be empty or null")
    private String userId;
    @NotEmpty(message = "Password cannot be empty or null")
    private String newPassword;
    @NotEmpty(message = "Confirm Password cannot be empty or null")
    private String confirmNewPassword;
}
