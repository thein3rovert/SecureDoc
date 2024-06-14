package com.in3rovert_so.securedoc.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.in3rovert_so.securedoc.domain.Response;
import com.in3rovert_so.securedoc.exception.ApiException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.util.BiConsumer;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;

// import java.time.LocalDateTime;
import java.nio.file.AccessDeniedException;
import java.util.Map;
import java.util.function.BiFunction;

import static java.time.LocalTime.now;
import static java.util.Collections.emptyMap;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;
import static org.apache.logging.log4j.util.Strings.EMPTY;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class RequestUtils {

    /**
     * Writes the response to the HTTP servlet response.
     *
     * @param httpServletResponse the HTTP servlet response
     * @param response the response to be written
     * @throws ApiException if an exception occurs while writing the response
     */
    private static final BiConsumer<HttpServletResponse, Response> writeResponse = (httpServletResponse, response) -> {
        // Helper method for the handleErrorResponse helps to write the response to the HTTP servlet response
        try {
            // Get the output stream from the HTTP servlet response
            var outputStream = httpServletResponse.getOutputStream();
            // Write the response using the ObjectMapper
            new ObjectMapper().writeValue(outputStream, response);
            // Flush the output stream
            outputStream.flush();
        } catch (Exception exception) {
            // Throw an ApiException with the exception message
            throw new ApiException(exception.getMessage());
        }
    };

    /**
     * Returns an error response message based on the provided exception and HTTP status.
     *
     * @param exception The exception that occurred.
     * @param httpStatus The HTTP status code.
     * @return The error response message.
     */
    private static final BiFunction<Exception, HttpStatus, String> errorResponse = (exception, httpStatus) -> {
        // Check if the HTTP status is FORBIDDEN or UNAUTHORIZED
        if(httpStatus.isSameCodeAs(FORBIDDEN)) {
            return "You do not have enough permission";
        }
        if(httpStatus.isSameCodeAs(UNAUTHORIZED)) {
            return "You are not logged in";
        }
        // Check if the exception is one of the specified types
        if(exception instanceof DisabledException || exception instanceof LockedException
                || exception instanceof BadCredentialsException || exception instanceof CredentialsExpiredException
                || exception instanceof ApiException) {
            return exception.getMessage();
        }
        // Check if the HTTP status is a 5xx server error
        if(httpStatus.is5xxServerError()) {
            return "An internal Server error occurred";
        }
        else {
            return "An error occured. Please kindly try again.";
        }
    };


    public static Response getResponse(HttpServletRequest request, Map<?, ?> data, String message, HttpStatus status) {

        return new Response(now().toString(), status.value(), request.getRequestURI(), HttpStatus.valueOf(status.value()), message, EMPTY, data);
    }

    /**
     * Handles an error response by writing the error response inside the response body.
     *
     * @param request  the HTTP servlet request
     * @param response the HTTP servlet response
     * @param exception the exception that occurred
     */
    public static void handleErrorResponse(HttpServletRequest request, HttpServletResponse response, Exception exception) {
        // Check if the exception is an instance of AccessDeniedException
        if(exception instanceof AccessDeniedException) {
            // Get the error response object
            Response apiResponse = getErrorResponse(request, response, exception, FORBIDDEN);
            // Write the error response inside the response body
            writeResponse.accept(response, apiResponse);
        }
    }

    /**
     * Gets an error response object based on the given parameters.
     *
     * @param request   the HTTP servlet request
     * @param response  the HTTP servlet response
     * @param exception the exception that occurred
     * @param status    the HTTP status code for the error response
     * @return an error response object
     */
    private static Response getErrorResponse(HttpServletRequest request, HttpServletResponse response, Exception exception, HttpStatus status) {
        // Set the content type of the response to JSON
        response.setContentType(APPLICATION_JSON_VALUE);
        // Set the HTTP status code of the response
        response.setStatus(status.value());
        // Create and return a new error response object
        return new Response(now().toString(), status.value(), request.getRequestURI(), HttpStatus.valueOf(status.value()), errorResponse.apply(exception, status), getRootCauseMessage(exception), emptyMap());
    }
}