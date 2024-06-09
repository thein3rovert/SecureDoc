package com.in3rovert_so.securedoc.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MyOwnAuthenticationProvider implements AuthenticationProvider {
    private final UserDetailsService userDetailsService; //User Saves in our database

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        var user = (UsernamePasswordAuthenticationToken) authentication; //User coming in from the request.
        //Comparing both users from db and User from Request
        var userFromDb = userDetailsService.loadUserByUsername((String) user.getPrincipal()); //Load user
        //if the user credentials is the same as user from Db credentials.
        var password = (String)user.getCredentials(); //Fixing failed comparison error
        //if((user.getCredentials()).equals(userFromDb.getPassword())) {
        if(password.equals(userFromDb.getPassword())){
            return UsernamePasswordAuthenticationToken.authenticated(userFromDb, "[PASSWORD PROTECTED]", userFromDb.getAuthorities());
        }
        throw new BadCredentialsException("Unable to login");
    }

    //What type of authentication is supported by this class
    @Override
    public boolean supports(Class<?> authentication) {
        //return true; or:
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
