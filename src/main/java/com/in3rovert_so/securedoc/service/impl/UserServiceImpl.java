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
import static com.in3rovert_so.securedoc.utils.UserUtils.*;
import static java.time.LocalDateTime.now;
import static org.apache.commons.lang3.StringUtils.EMPTY;

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
        System.out.println("User credential entity can be found here" + credentialEntity);
        credentialRepository.save(credentialEntity);
        var confirmationEntity = new ConfirmationEntity(userEntity);
        System.out.println("User credential entity can be found here" + confirmationEntity);
        confirmationRepository.save(confirmationEntity);
        publisher.publishEvent(new UserEvent(userEntity, REGISTRATION, Map.of("key", confirmationEntity.getKey())));
        System.out.println("Confirmation Token" + confirmationEntity.getKey());
    }

    @Override
    public RoleEntity getRoleName(String name) {
        var role = roleRepository.findByNameIgnoreCase(name); //Get the name of the roles.
        System.out.println("The user is " + role);
        return role.orElseThrow(() -> new ApiException("Role is not found"));
    }
    //..............................

    @Override
    public void verifyAccountKey(String key) {
        var confirmationEntity = getUserConfirmation(key);
        System.out.println("The user confirmation key for verification is " + confirmationEntity);
        var userEntity = getUserEntityByEmail(confirmationEntity.getUserEntity().getEmail());
        System.out.println("User account has just been verified" + userEntity);
        userEntity.setEnabled(true);
        userRepository.save(userEntity);
        System.out.println("User entibty to be saved to the database" + userEntity);
        //confirmationRepository.delete(confirmationEntity);
    }


    @Override
    public void updateLoginAttempt(String email, LoginType loginType) {
        var userEntity = getUserEntityByEmail(email);
        System.out.println("Here is the email" + userEntity);
        RequestContext.setUserId(userEntity.getId()); //Set the userId in the request context because we are going to save iin the database we know who did.
        switch (loginType) {
            case LOGIN_ATTEMPT -> {
                //Todo: Check to see if the user is in the Cache
                if(userCache.get(userEntity.getEmail()) == null) { //if the user is not in the cache yet, then their login attempt will be set to 0
                    userEntity.setLoginAttempts(0);
                    userEntity.setAccountNonLocked(true);
                }
                System.out.println("User Entity before update" + userEntity);
                userEntity.setLoginAttempts(userEntity.getLoginAttempts() + 1);
                System.out.println("User Entity after attempt" + userEntity);//If user is in the cache add 1 mto their login attempt
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
        System.out.println("User Entity before saved to db" + userEntity);
        userRepository.save(userEntity);
    }
    //Retrieve users based on their id
    @Override
    public User getUserByUserId(String userId) {
        var userEntity = userRepository.findUserByUserId(userId).orElseThrow(() -> new ApiException("User Id cannot found"));
        return fromUserEntity(userEntity, userEntity.getRole(), getUserCredentialById(userEntity.getId()));
    }

    //Retrieve users based on their email
    @Override
    public User getUserByEmail(String email) {
        UserEntity userEntity = getUserEntityByEmail(email);

        return fromUserEntity(userEntity, userEntity.getRole(), getUserCredentialById(userEntity.getId()));
    }
    //Retrieve users credentials based on their id
    @Override
    public CredentialEntity getUserCredentialById(Long userId) {
       var credentialById = credentialRepository.getCredentialByUserEntityId(userId);
        return credentialById.orElseThrow(()-> new ApiException("Unable to find user credentials"));
    }

    @Override
    public User setUpMfa(Long id) {
        //Lets get the user entity
        var userEntity = getUserEntityById(id);
        //Now let get the secret
        var codeSecret = qrCodeSecret.get();
        userEntity.setQrCodeImageUri(qrCodeImageUri.apply(userEntity.getEmail(), codeSecret));
        userEntity.setQrCodeSecret(codeSecret);
        userEntity.setMfa(true);
        userRepository.save(userEntity);
        return fromUserEntity(userEntity, userEntity.getRole(), getUserCredentialById(userEntity.getId()));
    }

    @Override
    public User cancelMfa(Long id) {
        //Lets get the user entity
        var userEntity = getUserEntityById(id);
        userEntity.setMfa(false);
        userEntity.setQrCodeImageUri(EMPTY);
        userEntity.setQrCodeSecret(EMPTY);
        userRepository.save(userEntity);
        return fromUserEntity(userEntity, userEntity.getRole(), getUserCredentialById(userEntity.getId()));
    }
    private UserEntity getUserEntityById(Long id) {
        var userById = userRepository.findById(id);
        return userById.orElseThrow(() -> new ApiException("User not found for MFA uses"));
    }

    private UserEntity getUserEntityByEmail(String email) {
        var userByEmail = userRepository.findByEmailIgnoreCase(email);
        return userByEmail.orElseThrow(() -> new ApiException("User not Found, UserEntity cannot be found by email"));
    }

    private ConfirmationEntity getUserConfirmation(String key) {
        return confirmationRepository.findByKey(key).orElseThrow(() -> new ApiException("Confirmation Key not Found"));
    }

    //.......................

    //Creating helper method for the createNewUser method
    private UserEntity createNewUser(String firstName, String lastName, String email) {
        var role = getRoleName(Authority.USER.name()); //Find a role in the database by the name USER
        System.out.print(createUserEntity("The roles" + firstName, lastName, email, role));//Todo: Create the method
        return createUserEntity(firstName, lastName, email, role); //Todo: Create the method
    }
}
