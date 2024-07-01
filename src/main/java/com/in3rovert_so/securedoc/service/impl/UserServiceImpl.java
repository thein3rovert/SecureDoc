package com.in3rovert_so.securedoc.service.impl;

import com.in3rovert_so.securedoc.cache.CacheStore;
import com.in3rovert_so.securedoc.domain.RequestContext;
import com.in3rovert_so.securedoc.dto.User;
import com.in3rovert_so.securedoc.entity.ConfirmationEntity;
import com.in3rovert_so.securedoc.entity.CredentialEntity;
import com.in3rovert_so.securedoc.entity.RoleEntity;
import com.in3rovert_so.securedoc.entity.UserEntity;
import com.in3rovert_so.securedoc.enumeration.Authority;
import com.in3rovert_so.securedoc.enumeration.LoginType;
import com.in3rovert_so.securedoc.event.UserEvent;
import com.in3rovert_so.securedoc.exception.ApiException;
import com.in3rovert_so.securedoc.repository.ConfirmationRepository;
import com.in3rovert_so.securedoc.repository.CredentialRepository;
import com.in3rovert_so.securedoc.repository.RoleRepository;
import com.in3rovert_so.securedoc.repository.UserRepository;
import com.in3rovert_so.securedoc.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.in3rovert_so.securedoc.enumeration.EventType.REGISTRATION;
import static com.in3rovert_so.securedoc.utils.UserUtils.createUserEntity;
import static java.time.LocalDateTime.now;

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
    private final CacheStore<String, Integer> userCache;
    @Override
    public void createUser(String firstName, String lastName, String email, String password) {
        var userEntity = userRepository.save(createNewUser(firstName, lastName, email)); //Todo: Create the createNewUser helper method.
        var credentialEntity = new CredentialEntity(userEntity, password);
        credentialRepository.save(credentialEntity);
        var confirmationEntity = new ConfirmationEntity(userEntity);
        confirmationRepository.save(confirmationEntity);
        publisher.publishEvent(new UserEvent(userEntity, REGISTRATION, Map.of("key", confirmationEntity.getKey())));
        System.out.println("Confirmation Token" + confirmationEntity.getKey());
    }

    @Override
    public RoleEntity getRoleName(String name) {
        var role = roleRepository.findByNameIgnoreCase(name); //Get the name of the roles.
        return role.orElseThrow(() -> new ApiException("Role is not found"));
    }
    //..............................

    @Override
    public void verifyAccountKey(String key) {
        var confirmationEntity = getUserConfirmation(key);
        var userEntity = getUserEntityByEmail(confirmationEntity.getUserEntity().getEmail());
        userEntity.setEnabled(true);
        userRepository.save(userEntity);
        confirmationRepository.delete(confirmationEntity);
    }

    @Override
    public void updateLoginAttempt(String email, LoginType loginType) {
        var userEntity = getUserEntityByEmail(email);
        RequestContext.setUserId(userEntity.getId()); //Set the userId in the request context because we are going to save iin the database we know who did.
        switch (loginType) {
            case LOGIN_ATTEMPT -> {
                //Todo: Check to see if the user is in the Cache
                if(userCache.get(userEntity.getEmail()) == null) { //if the user is not in the cache yet, then their login attempt will be set to 0
                    userEntity.setLoginAttempts(0);
                    userEntity.setAccountNonLocked(true);
                }
                userEntity.setLoginAttempts(userEntity.getLoginAttempts() + 1); //If user is in the cache add 1 mto their login attempt
                userCache.put(userEntity.getEmail(), userEntity.getLoginAttempts());//Then put their email and their login attempt in the cache.
                if (userCache.get(userEntity.getEmail()) > 5) { //if the cache is greater than 5, meaning if the user login more than 5 time
                    userEntity.setAccountNonLocked(false);//Lock the user account.
                }
            }
            case LOGIN_SUCCESS -> {
                userEntity.setAccountNonLocked(true);
                userEntity.setLoginAttempts(0);
                userEntity.setLastLogin(now());
                userCache.evict(userEntity.getEmail());
            }
        }
        userRepository.save(userEntity);
    }

    @Override
    public User getUserByUserId(String userId) {
        return null;
    }

    @Override
    public User getUserByEmail(String email) {
        return null;
    }
    @Override
    public CredentialEntity getUserCredentialById(Long id) {
        return null;
    }

    private UserEntity getUserEntityByEmail(String email) {
        var userByEmail = userRepository.findByEmailIgnoreCase(email);
        return userByEmail.orElseThrow(() -> new ApiException("User not Found"));
    }

    private ConfirmationEntity getUserConfirmation(String key) {
        return confirmationRepository.findByKey(key).orElseThrow(() -> new ApiException("Confirmation Key not Found"));
    }

    //.......................

    //Creating helper method for the createNewUser method
    private UserEntity createNewUser(String firstName, String lastName, String email) {
        var role = getRoleName(Authority.USER.name()); //Find a role in the database by the name USER
        return createUserEntity(firstName, lastName, email, role); //Todo: Create the method
    }
}
