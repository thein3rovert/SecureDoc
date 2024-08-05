package com.in3rovert_so.securedoc.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class QrCodeRequest {
    @NotEmpty(message = "UserID in the request cannot be empty or null")
    private String userId;
    @NotEmpty(message = "QR code in the request cannot be empty or null")
    private String qrCode;
}
