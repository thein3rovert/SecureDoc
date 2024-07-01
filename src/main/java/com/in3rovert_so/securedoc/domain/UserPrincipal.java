package com.in3rovert_so.securedoc.domain;

import com.in3rovert_so.securedoc.dto.User;
import com.in3rovert_so.securedoc.entity.CredentialEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

//The user details is is similar to the user details services.

@RequiredArgsConstructor
public class UserPrincipal implements UserDetails {

    private final User user;
    private final CredentialEntity credentialEntity;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
