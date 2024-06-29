package com.in3rovert_so.securedoc.security;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.in3rovert_so.securedoc.domain.ApiAuthentication;
import com.in3rovert_so.securedoc.domain.Response;
import com.in3rovert_so.securedoc.dto.User;
import com.in3rovert_so.securedoc.dtorequest.LoginRequest;

import com.in3rovert_so.securedoc.enumeration.TokenType;
import com.in3rovert_so.securedoc.service.JwtService;
import com.in3rovert_so.securedoc.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;
import java.util.Map;

import static com.fasterxml.jackson.core.JsonParser.Feature.AUTO_CLOSE_SOURCE;
import static com.in3rovert_so.securedoc.enumeration.LoginType.LOGIN_ATTEMPT;
import static com.in3rovert_so.securedoc.enumeration.LoginType.LOGIN_SUCCESS;
import static com.in3rovert_so.securedoc.utils.RequestUtils.getResponse;
import static com.in3rovert_so.securedoc.utils.RequestUtils.handleErrorResponse;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
public class AuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private final UserService userService; //Todo: Update the login Attempt of the User.
    private final JwtService jwtService; //Import Jwtservice

    public AuthenticationFilter(AuthenticationManager authenticationManager, UserService userService, JwtService jwtService) {
        super(new AntPathRequestMatcher("/user/login", POST.name()), authenticationManager);
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        try {
            //Grab the user information to create the authentication after getting the login types
            var user = new ObjectMapper().configure(AUTO_CLOSE_SOURCE, true).readValue(request.getInputStream(), LoginRequest.class);
            userService.updateLoginAttempt(user.getEmail(), LOGIN_ATTEMPT);
            var authentication = ApiAuthentication.unauthenticated(user.getEmail(), user.getPassword());
            //Pass the credentials to the authentication manager
            return getAuthenticationManager().authenticate(authentication);
        } catch (Exception exception) {
            log.error(exception.getMessage());
            handleErrorResponse(request, response, exception); //Todo: Create method handle
            return null;
        }
    }

    /**
     * Handles successful authentication by updating user login attempt and responding based on MFA status
     * @param request - the HttpServletRequest object
     * @param response - the HttpServletResponse object
     * @param chain - the FilterChain object
     * @param authentication - the Authentication object
     * @throws IOException
     * @throws ServletException
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
        // Call super method for successful authentication
        // super.successfulAuthentication(request, response, chain, authentication);
        // Get the authenticated user
        var user = (User) authentication.getPrincipal();
        // Update the user's login attempt
        userService.updateLoginAttempt(user.getEmail(), LOGIN_SUCCESS);
        // Determine response based on MFA status
        var httpResponse = user.isMfa() ? sendQrCode(request, user) : sendResponse(request, response, user);
        // Set response content type and status
        response.setContentType(APPLICATION_JSON_VALUE);
        response.setStatus(OK.value());
        // Write response to output stream
        var out = response.getOutputStream();
        var mapper = new ObjectMapper();
        mapper.writeValue(out, httpResponse);
        out.flush();
    }

    /**
     * Sends a response with cookies and a success message
     * @param request - the HttpServletRequest object
     * @param response - the HttpServletResponse object
     * @param user - the User object
     * @return a Response object
     */
    private Response sendResponse(HttpServletRequest request, HttpServletResponse response, User user) {
        // Add access token cookie
        jwtService.addCookie(response, user, TokenType.ACCESS);
        // Add refresh token cookie
        jwtService.addCookie(response, user, TokenType.REFRESH);
        // Return a response with user information and success message
        return getResponse(request, Map.of("user", user), "Login Success", OK);
    }

    private Response sendQrCode(HttpServletRequest request, User user) {
        return getResponse(request, Map.of("user", user), "Please enter QR code", OK);
    }
}
