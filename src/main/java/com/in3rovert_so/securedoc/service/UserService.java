package com.in3rovert_so.securedoc.service;

import com.in3rovert_so.securedoc.dto.User;
import com.in3rovert_so.securedoc.entity.CredentialEntity;
import com.in3rovert_so.securedoc.entity.RoleEntity;
import com.in3rovert_so.securedoc.enumeration.LoginType;

public interface UserService {
    void createUser(String firstName, String lastName, String email, String password);
    RoleEntity getRoleName(String name);

    void verifyAccountKey(String key);

    //Todo: Update the login attempt
    void updateLoginAttempt(String email, LoginType loginType);
    User getUserByUserId(String userId);
    User getUserByEmail(String email);

    CredentialEntity getUserCredentialById(Long id);

    User cancelMfa(Long id);

    User setUpMfa(Long id);

    User verifyQrCode(String userId, String qrCode);

    void resetPassword(String email);

    User verifyPasswordKey(String key);

    void updatePassword(String userId, String newPassword, String confirmNewPassword); //Because the user is not logged in we need to pass in the password
    void updatePassword(String userId, String currentPassword, String newPassword, String confirmNewPassword); // New update password for logged-in user

    User updateUser(String userId, String firstName, String lastName, String email, String phone, String bio);

    void updateRole(String userId, String role);

    void toggleCredentialsExpired(String userId);

    void toggleAccountExpired(String userId);

    void toggleAccountEnabled(String userId);

    void toggleAccountLocked(String userId);
}
