package com.in3rovert_so.securedoc.security;

import com.in3rovert_so.securedoc.domain.ApiAuthentication;
import com.in3rovert_so.securedoc.domain.UserPrincipal;
import com.in3rovert_so.securedoc.exception.ApiException;
import com.in3rovert_so.securedoc.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.in3rovert_so.securedoc.constant.Constants.NINETY_DAYS;

/**
 * Custom authentication provider for handling user authentication.
 */
@Component
@RequiredArgsConstructor
public class MyOwnAuthenticationProvider implements AuthenticationProvider {

    /**
     * Service for managing user-related operations.
     */
    private final UserService userService; //User Saves in our database

    /**
     * Encoder for hashing and comparing passwords.
     */
    private final BCryptPasswordEncoder encoder; //Fixed

    /**
     * Function for converting Authentication to ApiAuthentication.
     */
    private final Function<Authentication, ApiAuthentication> authenticationFunction = authentication -> (ApiAuthentication) authentication;

    /**
     * Consumer to validate user account status.
     */
    private final Consumer<UserPrincipal> validAccount = userPrincipal -> {
        if(userPrincipal.isAccountNonLocked()) {throw new LockedException("Your account is currently locked");}
        if(userPrincipal.isEnabled()) {throw new DisabledException("Your account is currently disabled");}
        if(userPrincipal.isCredentialsNonExpired()) {throw new CredentialsExpiredException("Your account has expired please contact the account admin");}
        if(userPrincipal.isAccountNonExpired()) {throw new DisabledException("Your account is currently locked");}
    };

    /**
     * Authenticates the user based on the provided authentication information.
     * @param authentication The authentication object containing user credentials.
     * @return An Authentication object if the user is successfully authenticated.
     * @throws AuthenticationException If authentication fails.
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        var apiAuthentication = authenticationFunction.apply(authentication);

        var user = userService.getUserByEmail(apiAuthentication.getEmail()); //User we are going to pass in as the logged in user
        if (user != null) {
            var userCredential = userService.getUserCredentialById(user.getId());
            //After getting the credentials we have to check if the credentials is expired.
            if (userCredential.getUpdatedAt().minusDays(NINETY_DAYS).isAfter(LocalDateTime.now())) {
                throw new ApiException("Credentials are expired please reset your password");
            }
            var userPrincipal = new UserPrincipal(user, userCredential);
            validAccount.accept(userPrincipal);
            if(encoder.matches(apiAuthentication.getPassword(), userCredential.getPassword())) {
                return ApiAuthentication.authenticated(user, userPrincipal.getAuthorities());
            } else throw new ApiException("Bad Credentials(Email and /or password incorrect. Please try again");
        } throw new ApiException("Unable to authenticate user");
    }

    /**
     * Determines if this authentication provider supports the authentication class provided.
     * @param authentication The authentication class to check.
     * @return True if the authentication class is supported, false otherwise.
     */
    @Override
    public boolean supports(Class<?> authentication) {
        //return true; or:
        return ApiAuthentication.class.isAssignableFrom(authentication);
    }
}
