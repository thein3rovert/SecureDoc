package com.in3rovert_so.securedoc.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_DEFAULT;

@JsonInclude(NON_DEFAULT)
public record Response(String time, int code , String path, HttpStatus status, String message, String exception, Map<?, ?> data) {
}