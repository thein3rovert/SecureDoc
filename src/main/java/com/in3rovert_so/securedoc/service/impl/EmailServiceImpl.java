package com.in3rovert_so.securedoc.service.impl;

import com.in3rovert_so.securedoc.exception.ApiException;
import com.in3rovert_so.securedoc.service.EmailService;
import jakarta.mail.internet.HeaderTokenizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import static com.in3rovert_so.securedoc.utils.EmailUtils.getEmailMessage;
import static com.in3rovert_so.securedoc.utils.EmailUtils.getResetPasswordMessage;

@Service
@RequiredArgsConstructor
@Slf4j // for some message logging
public class EmailServiceImpl implements EmailService {
    private static final String NEW_USER_ACCOUNT_VERIFICATION = "New User Account Verification";
    private static final String PASSWORD_RESET_REQUEST = "Password Reset Request";

    private final JavaMailSender sender; //TODO: coming back to work in the sender.

    @Value("${spring.mail.verify.host}")
    private String host;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    @Async
    public void sendNewAccountEmail(String name, String email, String token) {
        try {
            var message = new SimpleMailMessage();
            message.setSubject(NEW_USER_ACCOUNT_VERIFICATION); //ENUM containing the subject of the email.
            message.setFrom(fromEmail);
            //System.out.println(message);
            message.setTo(email);
            //System.out.println(message);
            message.setText(getEmailMessage(name, host, token));
            System.out.println("name, host and token" + message);
            sender.send(message);
            System.out.println(message);
        }catch (Exception exception){
            log.error(exception.getMessage());
            throw new ApiException("Unable to send email"); // Generate a custom email message for the Api exception because it's safer.
        }
    }
    @Override
    @Async
    public void sendPasswordResetEmail(String name, String email, String token) {
        try {
            var  message = new SimpleMailMessage();
            message.setSubject(PASSWORD_RESET_REQUEST); //ENUM containing the subject of the email.
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setText(getResetPasswordMessage(name, host, token));
            sender.send(message);
        }catch (Exception exception){
            log.error(exception.getMessage());
            throw new ApiException("Unable to send email"); // Generate a custom email message for the Api exception because its safer.
        }
    }
}
