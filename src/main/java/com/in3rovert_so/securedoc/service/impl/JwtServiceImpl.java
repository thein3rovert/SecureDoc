package com.in3rovert_so.securedoc.service.impl;

import com.in3rovert_so.securedoc.domain.Token;
import com.in3rovert_so.securedoc.domain.TokenData;
import com.in3rovert_so.securedoc.dto.User;
import com.in3rovert_so.securedoc.enumeration.TokenType;
import com.in3rovert_so.securedoc.function.TriConsumer;
import com.in3rovert_so.securedoc.security.JwtConfiguration;
import com.in3rovert_so.securedoc.service.JwtService;
import com.in3rovert_so.securedoc.service.UserService;
import com.in3rovert_so.securedoc.utils.RequestUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.bridge.ICommand;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.in3rovert_so.securedoc.constant.Constants.*;
import static com.in3rovert_so.securedoc.enumeration.TokenType.ACCESS;
import static com.in3rovert_so.securedoc.enumeration.TokenType.REFRESH;
import static io.jsonwebtoken.Header.JWT_TYPE;
import static io.jsonwebtoken.Header.TYPE;
import static java.time.Instant.now;
import static java.util.Arrays.stream;
import static java.util.Optional.empty;
import static org.springframework.boot.web.server.Cookie.SameSite.NONE;
import static org.springframework.security.core.authority.AuthorityUtils.commaSeparatedStringToAuthorityList;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtServiceImpl extends JwtConfiguration implements JwtService {
    private final UserService userService;

    //Define some helper private methods
    private final Supplier<SecretKey> key = () -> Keys.hmacShaKeyFor(Decoders.BASE64.decode(getSecret()));//For providing a secret key

    /**
     * This function parses a token to extract the claims using a specified key.
     *
     * @param token - The token to parse and extract claims from.
     * @return The Claims extracted from the token.
     */
    private final Function<String, Claims> claimsFunction = token ->
            // Parse the token using the JWT parser.
            Jwts.parser()
                    // Verify the token with a specified key.
                    .verifyWith(key.get())
                    // Build the parser to prepare for parsing signed claims.
                    .build()
                    // Parse the signed claims from the token.
                    .parseSignedClaims(token)
                    // Get the payload (claims) from the parsed claims.
                    .getPayload();
    private final Function<String , String> subject = token -> getClaimsValue(token, Claims::getSubject);
    //private final Function<String , String> date = token -> getClaimsValue(token, Claims::getExpiration);

    /**
     * This function extracts a token from a request cookie.
     *
     * @param request - The HttpServletRequest object from which to extract the token.
     * @param cookieName - The name of the cookie to extract the token from.
     * @return An Optional containing the token as a String, or an empty Optional if the token is not found.
     */
    private final BiFunction<HttpServletRequest, String, Optional<String>> extractToken = (request, cookieName) ->
            // Get the cookies from the request, or an empty array if there are no cookies.
            Optional.of(stream(request.getCookies() == null ? new Cookie [] {new Cookie(EMPTY_VALUE, EMPTY_VALUE)} : request.getCookies())
                            // Filter the cookies to only include the one with the specified name.
                            .filter(cookie -> Objects.equals(cookieName, cookie.getName()))
                            // Map each cookie to its value.
                            .map(Cookie::getValue)
                            // Find the first cookie value.
                            .findAny())
                    // If no token is found, return an empty Optional.
                    .orElse(empty());

    /**
     * This Supplier returns a JwtBuilder object.
     *
     * @return The JwtBuilder object.
     */
    private final Supplier<JwtBuilder> builder = () ->
            // Create a new JwtBuilder.
            Jwts.builder()
                    // Set the header of the JWT to contain a single entry with the key "type" and the value "JWT".
                    .header().add(Map.of(TYPE, JWT_TYPE))
                    // Add the audience (recipient) of the JWT.
                    .and()
                    .audience().add(THE_IN3ROVERT_LLC)
                    // Add the id (unique identifier) of the JWT.
                    .and()
                    .id(UUID.randomUUID().toString())
                    // Set the issued at (creation time) of the JWT.
                    .issuedAt(Date.from(now()))
                    // Set the not before (start time) of the JWT.
                    .notBefore(new Date())
                    // Sign the JWT with a specified key and signature algorithm.
                    .signWith(key.get(), Jwts.SIG.HS512);

    /**
     * Builds a token based on the provided user and token type.
     *
     * @param {User} user - The user for whom the token is being built.
     * @param {TokenType} type - The type of token being built.
     * @return {string} The built token.
     */
    private final BiFunction<User, TokenType, String> buildToken = (user, type) ->
            // Check if the token type is ACCESS
            Objects.equals(type, ACCESS) ?
                    // Build the token with additional claims
                    builder.get()
                            .subject(user.getUserId()) // Set the subject of the token to the user's ID
                            .claim(AUTHORITIES, user.getAuthorities()) // Add the AUTHORITIES claim with the user's authorities
                            .claim(ROLE, user.getRole()) // Add the ROLE claim with the user's roles
                            .expiration(Date.from(now().plusSeconds(getExpiration()))) // Set the expiration time of the token
                            .compact() : // Build the token
                    // Build the token without additional claims if its a refresh token
                    builder.get()
                            .subject(user.getUserId()) // Set the subject of the token to the user's ID
                            .expiration(Date.from(now().plusSeconds(getExpiration()))) // Set the expiration time of the token
                            .compact();

    /**
     * A function that adds a cookie to the response.
     */
    private final TriConsumer<HttpServletResponse, User, TokenType> addCookie = (response, user, type) -> {
        switch (type) {
            case ACCESS -> {
                // Create an access token for the user.
                var accessToken = createToken(user, Token::getAccess);
                // Create a new cookie.
                var cookie = new Cookie(type.getValue(), accessToken);
                // Set the cookie properties.
                cookie.setHttpOnly(true);
                //cookie.setSecure(true);
                cookie.setMaxAge(2 * 60);
                cookie.setPath("/");
                cookie.setAttribute("SameSite", NONE.name());
                // Add the cookie to the response.
                response.addCookie(cookie);
        }
            case REFRESH -> {
                // Create an access token for the user.
                var refreshToken = createToken(user, Token::getRefresh);
                // Create a new cookie.
                var cookie = new Cookie(type.getValue(), refreshToken);
                // Set the cookie properties.
                cookie.setHttpOnly(true);
                //cookie.setSecure(true);
                cookie.setMaxAge(2 * 60 * 60); // Time set for the token to expire
                cookie.setPath("/");
                cookie.setAttribute("SameSite", NONE.name());
                // Add the cookie to the response.
                response.addCookie(cookie);
            }
        }
    };

    /**
     * Extracts a specific value from the claims using the provided function.
     *
     * @param <T> The type of the value to extract.
     * @param token The token containing the claims.
     * @param claims The function to extract the value from the claims.
     * @return The extracted value.
     */
    private <T> T getClaimsValue(String token, Function<Claims, T> claims) {
        // Apply the claimsFunction to extract the Claims object.
        return claimsFunction.andThen(claims).apply(token);
    }

    private final BiFunction<HttpServletRequest, String, Optional<Cookie>> extractCookie = (request, cookieName) ->
            Optional.of(stream(request.getCookies() == null ? new Cookie[] {new Cookie(EMPTY_VALUE, EMPTY_VALUE)} : request.getCookies())
                    .filter(cookie -> Objects.equals(cookieName, cookie.getName()))
                    .findAny())
                    .orElse(empty());

    public Function<String, List<GrantedAuthority>> authorities = token -> //Todo: Define Key Authorities
            commaSeparatedStringToAuthorityList(new StringJoiner(AUTHORITY_DELIMITER)
                    .add(claimsFunction.apply(token).get(AUTHORITIES, String.class))
                    .add(ROLE_PREFIX + claimsFunction.apply(token).get(ROLE, String.class)).toString());
    @Override
    public String createToken(User user, Function<Token, String> tokenFunction) {
        var token = Token.builder().access(buildToken.apply(user, ACCESS)).refresh(buildToken.apply(user, REFRESH)).build();
        return tokenFunction.apply(token);
    }

    @Override
    public Optional<String> extractToken(HttpServletRequest request, String cookieName) {
        return extractToken.apply(request, cookieName);
    }

    @Override
    public void addCookie(HttpServletResponse response, User user, TokenType type) {
        addCookie.accept(response, user, type);
    }

    @Override
    public <T> T getTokenData(String token, Function<TokenData, T> tokenFunction) {
        return tokenFunction.apply(
                TokenData.builder()
                        .valid(Objects.equals(userService.getUserByUserId(subject.apply(token)).getUserId(), claimsFunction.apply(token).getSubject()))
                        .authorities(authorities.apply(token))
                        .claims(claimsFunction.apply(token))
                        .user(userService.getUserByUserId(subject.apply(token)))
                        .build()
        );
    }

    @Override
    public void removeCookie(HttpServletRequest request, HttpServletResponse response, String cookieName) {
        var optionalCookie  = extractCookie.apply(request, cookieName); //In case we have no cookie.
        if(optionalCookie.isPresent()) {
            var cookie = optionalCookie.get();
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        }
    }
}
