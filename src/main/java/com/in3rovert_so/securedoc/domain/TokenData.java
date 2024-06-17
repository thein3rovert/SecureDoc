package com.in3rovert_so.securedoc.domain;

import com.in3rovert_so.securedoc.dto.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

@Builder//Because we need a Builder pattern
@Getter
@Setter
public class TokenData {
    private User user;
    //private Claims claims;
    private boolean valid;
    private List<GrantedAuthority> authorities; //AUTHORITIES ASSOCIATED WITH THE TOKEN.
}
