package com.in3rovert_so.securedoc.service;

import com.in3rovert_so.securedoc.entity.RoleEntity;
import com.in3rovert_so.securedoc.enumeration.Authority;

public interface UserService {
    void createUser(String firstName, String lastName, String email, String password);
    RoleEntity getRoleName(String name);

    void verifyAccountKey(String key);
}
