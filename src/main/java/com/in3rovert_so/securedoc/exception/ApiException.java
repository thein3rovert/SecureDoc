package com.in3rovert_so.securedoc.exception;

public class ApiException extends RuntimeException{
    public ApiException(String message) {super (message); }
    public ApiException() {super ("An error occurred -> Custom"); }
}
