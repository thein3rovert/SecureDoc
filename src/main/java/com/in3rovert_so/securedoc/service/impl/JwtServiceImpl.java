package com.in3rovert_so.securedoc.service.impl;

import com.in3rovert_so.securedoc.domain.Token;
import com.in3rovert_so.securedoc.domain.TokenData;
import com.in3rovert_so.securedoc.dto.User;
import com.in3rovert_so.securedoc.enumeration.TokenType;
import com.in3rovert_so.securedoc.security.JwtConfiguration;
import com.in3rovert_so.securedoc.service.JwtService;
import com.in3rovert_so.securedoc.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.bridge.ICommand;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.in3rovert_so.securedoc.constant.Constants.*;
import static org.springframework.security.core.authority.AuthorityUtils.commaSeparatedStringToAuthorityList;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtServiceImpl extends JwtConfiguration implements JwtService {
    private final UserService user;

    //Define some helper private methods
    private final Supplier<SecretKey> key = () -> Keys.hmacShaKeyFor(Decoders.BASE64.decode(getSecret()));//For providing a secret key

    private final Function<String, Claims> claimsFunction = token ->
            Jwts.parser()
                    .verifyWith(key.get())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
    private final Function<String , String> subject = token -> getClaimsValue(token, Claims::getSubject);

    public Function<String, List<GrantedAuthority>> authorities = token -> //Todo: Define Key Authorities
            commaSeparatedStringToAuthorityList(new StringJoiner(AUTHORITY_DELIMITER)
                    .add(claimsFunction.apply(token).get(AUTHORITIES, String.class))
                    .add(ROLE_PREFIX + claimsFunction.apply(token).get(ROLE, String.class)).toString());
    @Override
    public String createToken(User user, Function<Token, String> tokenFunction) {
        return null;
    }

    @Override
    public Optional<String> extractToken(HttpServletRequest request, String tokenType) {
        return Optional.empty();
    }

    @Override
    public void addCookie(HttpServletResponse response, User user, TokenType type) {

    }

    @Override
    public <T> T getTokenData(String token, Function<TokenData, T> tokenFunction) {
        return null;
    }
}
