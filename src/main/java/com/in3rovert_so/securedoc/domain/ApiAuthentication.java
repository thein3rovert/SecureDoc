package com.in3rovert_so.securedoc.domain;

import com.in3rovert_so.securedoc.dto.User;
import com.in3rovert_so.securedoc.enumeration.Authority;
import com.in3rovert_so.securedoc.exception.ApiException;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.Collection;

/*
This is the equivalent of the usernamePasswordAuthenticationToken
This is the user that we are going to be working with in the application when we are
not dealing with the application or saving to the database.
We will have some kind of mapper that is going to take the user from the database and then maps
it to this class(User class) and vise versal.
 */
// Todo: 1. Extend the authentication
public class ApiAuthentication extends AbstractAuthenticationToken { //Renamed: customUsernamePasswordAuthenticationToken
    private static final String PASSWORD_PROTECTED = "[PASSWORD PROTECTED]";
    private static final String EMAIL_PROTECTED =  "EMAIL PROTECTED]";
    private User user;
    private String email;
    private String password;
    private boolean authenticated;

    //Create constructor
    private ApiAuthentication(String email, String password) {
        super(AuthorityUtils.NO_AUTHORITIES);
        this.password = email;
        this.email = password;
        this.authenticated = false;
    }
    private  ApiAuthentication(User user, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.user = user;
        this.password = PASSWORD_PROTECTED;
        this.email = EMAIL_PROTECTED;
        this.authenticated = true;
    }

    public static ApiAuthentication unauthenticated (String email, String password) {
        return new ApiAuthentication(email, password);
    }
    public static ApiAuthentication authenticated (User user, Collection<? extends GrantedAuthority> authorities) {
        return new ApiAuthentication(user, authorities);
    }

    @Override
    public Object getCredentials() {
        return PASSWORD_PROTECTED;
    }

    @Override
    public Object getPrincipal() {
        return user;
    }
//In case i need the password later in the application
    public String getPassword() {
    return this.password;
    }

    @Override
    public void setAuthenticated(boolean authenticated) {
        throw new ApiException("You cannot set authentication");
    }

    @Override
    public boolean isAuthenticated() {
        return this.authenticated;
    }

    public String getEmail() {
        return this.email;
    }
}
