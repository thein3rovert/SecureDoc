package com.in3rovert_so.securedoc.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder//Because we need a Builder pattern
@Getter
@Setter
public class Token {
    private String access;
    private String refresh;
}
