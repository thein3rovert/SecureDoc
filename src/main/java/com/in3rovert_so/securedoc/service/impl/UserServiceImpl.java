package com.in3rovert_so.securedoc.service.impl;

import com.in3rovert_so.securedoc.entity.ConfirmationEntity;
import com.in3rovert_so.securedoc.entity.CredentialEntity;
import com.in3rovert_so.securedoc.enumeration.EventType;
import com.in3rovert_so.securedoc.event.UserEvent;
import com.in3rovert_so.securedoc.repository.ConfirmationRepository;
import com.in3rovert_so.securedoc.repository.CredentialRepository;
import com.in3rovert_so.securedoc.repository.RoleRepository;
import com.in3rovert_so.securedoc.repository.UserRepository;
import com.in3rovert_so.securedoc.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Transactional(rollbackOn = Exception.class) //Any Exception that occurs roll everything back.
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CredentialRepository credentialRepository;
    private final ConfirmationRepository confirmationRepository;
    //private final BCryptPasswordEncoder encoder;
    private final ApplicationEventPublisher publisher; //To public event when a user is created so that we can get an email.

    @Override
    public void createUser(String firstName, String lastName, String email, String password) {
        var userEntity = userRepository.save(createNewUser(firstName, lastName, email));
        var credentialEntity = new CredentialEntity(userEntity, password);
        credentialRepository.save(credentialEntity);
        var confirmationEntity = new ConfirmationEntity(userEntity);
        confirmationRepository.save(confirmationEntity);
        publisher.publishEvent(new UserEvent(userEntity, EventType.REGISTRATION, Map.of("Key", confirmationEntity.getKey())));
    }
}
