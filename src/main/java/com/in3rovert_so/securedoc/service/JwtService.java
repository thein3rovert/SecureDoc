package com.in3rovert_so.securedoc.service;

import com.in3rovert_so.securedoc.domain.Token;
import com.in3rovert_so.securedoc.domain.TokenData;
import com.in3rovert_so.securedoc.dto.User;
import com.in3rovert_so.securedoc.enumeration.TokenType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;
import java.util.function.Function;

public interface JwtService {
    String createToken(User user, Function<Token, String> tokenFunction);
    Optional<String> extractToken(HttpServletRequest request, String tokenType);
    void addCookie(HttpServletResponse response, User user, TokenType type);
    <T> T getTokenData(String token, Function<TokenData, T> tokenFunction);
}

