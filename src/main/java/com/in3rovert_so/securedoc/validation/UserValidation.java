package com.in3rovert_so.securedoc.validation;

import com.in3rovert_so.securedoc.entity.UserEntity;
import com.in3rovert_so.securedoc.exception.ApiException;

public class UserValidation {

    public static void verifyAccountStatus(UserEntity user) {
        if (!user.isEnabled()) {
            throw new ApiException("User Account is disable, unable to verify ResetPassword cannot be verified");
        }
        if (!user.isAccountNonExpired()) {
            throw new ApiException("User Account is Expired, unable to verify ResetPassword");
        }
        if (!user.isAccountNonLocked()) {
            throw new ApiException("User Account is Locked, unable to verify ResetPassword");
        }

    }

}
