package com.in3rovert_so.securedoc.exception;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.in3rovert_so.securedoc.domain.Response;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.TransactionalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.nio.file.AccessDeniedException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.stream.Collectors;

import static com.in3rovert_so.securedoc.utils.RequestUtils.handleErrorResponse;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;
import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class HandleException extends ResponseEntityExceptionHandler implements ErrorController {
    private final HttpServletRequest request;

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception exception, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest webRequest) {
        log.error(String.format("handleExceptionInternal: %s", exception.getMessage()));
        return new ResponseEntity<>(handleErrorResponse(exception.getMessage(), getRootCauseMessage(exception), request, statusCode), statusCode);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception, HttpHeaders headers, HttpStatusCode statusCode, WebRequest webRequest) {
    log.error(String.format("handleMethodArgumentNotValid: %s", exception.getMessage()));
    var fieldErrors = exception.getBindingResult().getFieldErrors();
    var fieldsMessage = fieldErrors.stream().map(FieldError::getDefaultMessage).collect(Collectors.joining(", "));
        return new ResponseEntity<>(handleErrorResponse(fieldsMessage, getRootCauseMessage(exception), request, statusCode), statusCode);
    }
    /*
    Because we need to be able to get all the message 
    in the dtoRequest and turn them in to a single string, then pass in the message for the error we will return
    to the user.
     */

    /* This takes care of every Api_exception in the application */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Response> apiException(ApiException exception) {
        log.error(String.format("ApiException: %s", exception.getMessage()));
        return new ResponseEntity<>(handleErrorResponse(exception.getMessage(), getRootCauseMessage(exception), request, BAD_REQUEST), BAD_REQUEST);
    }
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Response> badCredentialsException(BadCredentialsException exception) {
        log.error(String.format("BadCredentialsException: %s", exception.getMessage()));
        return new ResponseEntity<>(handleErrorResponse(exception.getMessage(), getRootCauseMessage(exception), request, BAD_REQUEST), BAD_REQUEST);
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ResponseEntity<Response> sQLIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException exception) {
        log.error(String.format("SQLIntegrityConstraintViolationException: %s", exception.getMessage()));
        return new ResponseEntity<>(handleErrorResponse(exception.getMessage().contains("Duplicate entry") ? "Information already exist": exception.getMessage(), getRootCauseMessage(exception), request, BAD_REQUEST), BAD_REQUEST);
    }

    @ExceptionHandler(UnrecognizedPropertyException.class)
    public ResponseEntity<Response> unrecognizedPropertyException(UnrecognizedPropertyException exception) {
        log.error(String.format("UnrecognizedPropertyException: %s", exception.getMessage()));
        return new ResponseEntity<>(handleErrorResponse(exception.getMessage(), getRootCauseMessage(exception), request, BAD_REQUEST), BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Response> accessDeniedException(AccessDeniedException exception) {
        log.error(String.format("AccessDeniedException: %s", exception.getMessage()));
        return new ResponseEntity<>(handleErrorResponse("Access denied. You don't have access", getRootCauseMessage(exception), request, FORBIDDEN), FORBIDDEN);
    }
    @ExceptionHandler(Exception.class) //Mother of all exception
    public ResponseEntity<Response> exception(Exception exception) {
        log.error(String.format("Exception: %s", exception.getMessage()));
        return new ResponseEntity<>(handleErrorResponse(processErrorMessage(exception), getRootCauseMessage(exception), request, INTERNAL_SERVER_ERROR), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(TransactionalException.class) //Mother of all exception
    public ResponseEntity<Response> transactionalException(TransactionalException exception) {
        log.error(String.format("TransactionalException: %s", exception.getMessage()));
        return new ResponseEntity<>(handleErrorResponse(processErrorMessage(exception), getRootCauseMessage(exception), request, INTERNAL_SERVER_ERROR), INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(EmptyResultDataAccessException.class) //Mother of all exception
    public ResponseEntity<Response> emptyResultDataAccessException(EmptyResultDataAccessException exception) {
        log.error(String.format("EmptyResultDataAccessException: %s", exception.getMessage()));
        return new ResponseEntity<>(handleErrorResponse(exception.getMessage(), getRootCauseMessage(exception), request, BAD_REQUEST), BAD_REQUEST);
    }

    @ExceptionHandler(CredentialsExpiredException.class) //Mother of all exception
    public ResponseEntity<Response> credentialsExpiredException(CredentialsExpiredException exception) {
        log.error(String.format("CredentialsExpiredException: %s", exception.getMessage()));
        return new ResponseEntity<>(handleErrorResponse(exception.getMessage(), getRootCauseMessage(exception), request, BAD_REQUEST), BAD_REQUEST);
    }
    @ExceptionHandler(DisabledException.class) //Mother of all exception
    public ResponseEntity<Response> disabledException(DisabledException exception) {
        log.error(String.format("DisabledException: %s", exception.getMessage()));
        return new ResponseEntity<>(handleErrorResponse("Your account is currently disabled", getRootCauseMessage(exception), request, BAD_REQUEST), BAD_REQUEST);
    }
    @ExceptionHandler(LockedException.class) //Mother of all exception
    public ResponseEntity<Response> lockedException(LockedException exception) {
        log.error(String.format("LockedException: %s", exception.getMessage()));
        return new ResponseEntity<>(handleErrorResponse(exception.getMessage(), getRootCauseMessage(exception), request, BAD_REQUEST), BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateKeyException.class) //Mother of all exception
    public ResponseEntity<Response> duplicateKeyException(DuplicateKeyException exception) {
        log.error(String.format("DuplicateKeyException: %s", exception.getMessage()));
        return new ResponseEntity<>(handleErrorResponse(processErrorMessage(exception), getRootCauseMessage(exception), request, BAD_REQUEST), BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class) //Mother of all exception
    public ResponseEntity<Response> dataIntegrityViolationException(DataIntegrityViolationException exception) {
        log.error(String.format("DataIntegrityViolationException: %s", exception.getMessage()));
        return new ResponseEntity<>(handleErrorResponse(processErrorMessage(exception), getRootCauseMessage(exception), request, BAD_REQUEST), BAD_REQUEST);
    }
    @ExceptionHandler(DataAccessException.class) //Mother of all exception
    public ResponseEntity<Response> DataAccessException(DataAccessException exception) {
        log.error(String.format("DataAccessException: %s", exception.getMessage()));
        return new ResponseEntity<>(handleErrorResponse(processErrorMessage(exception), getRootCauseMessage(exception), request, BAD_REQUEST), BAD_REQUEST);
    }
    private String processErrorMessage(Exception exception) {
        if(exception instanceof ApiException) { return exception.getMessage();}
        if(exception.getMessage() != null) {
            if(exception.getMessage().contains("duplicate") && exception.getMessage().contains("AccountVerifications")) {
                return "You already verified your account.";
            }
            if(exception.getMessage().contains("duplicate") && exception.getMessage().contains("ResetPasswordVerification")) {
                return "An email has already been set to reset your account.";
            }
            if(exception.getMessage().contains("duplicate") && exception.getMessage().contains("Key (email)")) {
                return "Email already exists. Use a different emial and try again.";
            }
            if(exception.getMessage().contains("duplicate")){
                return "Duplicate entry. Kindly try again";
            }
        }
        return "An error occurred. Please kindly try again";
    }



}
