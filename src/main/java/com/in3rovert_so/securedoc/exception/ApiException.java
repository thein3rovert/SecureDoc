package com.in3rovert_so.securedoc.exception;

/* This class allow the application to catch any Api exception instance in the code.
 */
public class ApiException extends RuntimeException{
    public ApiException(String message) {super (message); }
    public ApiException() {super ("An error occurred -> Custom"); }
}
