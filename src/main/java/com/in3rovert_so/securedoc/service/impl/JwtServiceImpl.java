package com.in3rovert_so.securedoc.service.impl;

import com.in3rovert_so.securedoc.domain.Token;
import com.in3rovert_so.securedoc.domain.TokenData;
import com.in3rovert_so.securedoc.dto.User;
import com.in3rovert_so.securedoc.enumeration.TokenType;
import com.in3rovert_so.securedoc.security.JwtConfiguration;
import com.in3rovert_so.securedoc.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtServiceImpl extends JwtConfiguration implements JwtService {

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
