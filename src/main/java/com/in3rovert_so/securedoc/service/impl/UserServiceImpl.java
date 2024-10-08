package com.in3rovert_so.securedoc.service.impl;

import com.in3rovert_so.securedoc.cache.CacheStore;
import com.in3rovert_so.securedoc.constant.Constants;
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
import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.BiFunction;

import static com.in3rovert_so.securedoc.constant.Constants.NINETY_DAYS;
import static com.in3rovert_so.securedoc.constant.Constants.PHOTO_DIR;
import static com.in3rovert_so.securedoc.enumeration.EventType.REGISTRATION;
import static com.in3rovert_so.securedoc.enumeration.EventType.RESETPASSWORD;
import static com.in3rovert_so.securedoc.utils.UserUtils.*;
import static com.in3rovert_so.securedoc.validation.UserValidation.verifyAccountStatus;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
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
    private final BCryptPasswordEncoder encoder;
    private final ApplicationEventPublisher publisher; //To public event when a user is created so that we can get an email.
    private final CacheStore<String, Integer> userCache;
    @Override
    public void createUser(String firstName, String lastName, String email, String password) {
        var userEntity = userRepository.save(createNewUser(firstName, lastName, email)); //Todo: Create the createNewUser helper method.
        var credentialEntity = new CredentialEntity(userEntity, encoder.encode(password)); //Instead of passing in the raw passwords is should be encoded
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
        System.out.println("User account to be verified" + userEntity);
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

    //Perform Qrcode verification
    @Override
    public User verifyQrCode(String userId, String qrCode) {
        var userEntity = getUserEntityByUserId(userId);
        System.out.println("UserId for verification purposes main -------------" + userEntity);
        verifyCode(qrCode, userEntity.getQrCodeSecret());
        return fromUserEntity(userEntity, userEntity.getRole(), getUserCredentialById(userEntity.getId()));
    }
    //Verify the codes
    private boolean verifyCode(String qrCode, String qrCodeSecret) {
        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator();
        CodeVerifier codeVerifier = new DefaultCodeVerifier(codeGenerator, timeProvider);

        System.out.println(qrCode + qrCodeSecret);

        if (codeVerifier.isValidCode(qrCodeSecret, qrCode)) {
            return true;
        } else {
            throw new ApiException("Invalid QR code. Please try again");
        }
    }

    private UserEntity getUserEntityByUserId(String userId) {
        var userByUserId = userRepository.findUserByUserId(userId);
        System.out.println("UserId for verificxation purposes" + userByUserId);
        return userByUserId.orElseThrow(() -> new ApiException("User not found"));
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
    @Override
    public void resetPassword(String email) {
        //Lets get the user
        var user = getUserEntityByEmail(email);
        //Let get the user confirmation
        var confirmation = getUserConfirmation(user);
        if (confirmation != null) {
            //Send existing confirmation
            publisher.publishEvent(new UserEvent(user, RESETPASSWORD, Map.of("key", confirmation.getKey())));
        } else {
            // Create new confirmation, save and send
            var confirmationEntity = new ConfirmationEntity(user);
            System.out.println("Confirmation Entity for ResetPassword -------------: " + confirmationEntity);
            confirmationRepository.save(confirmationEntity);
            publisher.publishEvent(new UserEvent(user, RESETPASSWORD, Map.of("key", confirmationEntity.getKey())));
        }
    }

    @Override
    public User verifyPasswordKey(String key) {
        // Find the confirmation entity in the database, it confirmation not found
        var confirmationEntity = getUserConfirmation(key);
        if (confirmationEntity == null) { throw new ApiException("Unable to find the token (key) for confirmation");}
        // If the confirmation is found
        var userEntity = getUserEntityByEmail(confirmationEntity.getUserEntity().getEmail());
        // If userEntity not found
        if (userEntity == null) { throw new ApiException("Incorrect token)key");}
        verifyAccountStatus(userEntity);
        confirmationRepository.delete(confirmationEntity); //Optional but we don't want to many confirmation data for each user.
        return fromUserEntity(userEntity, userEntity.getRole(), getUserCredentialById(userEntity.getId()));
    }

    @Override
    public void updatePassword(String userId, String newPassword, String confirmNewPassword) {
        if(!confirmNewPassword.equals(newPassword)) {
            throw new ApiException("Password don't match. Please try again");
        }
        var user = getUserByUserId(userId);
        var credentials = getUserCredentialById(user.getId());
        credentials.setPassword(encoder.encode(newPassword));
        credentialRepository.save(credentials);
    }

    @Override
    public void updatePassword(String userId, String currentPassword, String newPassword, String confirmNewPassword) {
        if (!confirmNewPassword.equals(newPassword)) { throw new ApiException("New currentPassword don't match"); }
        var user = getUserEntityByUserId(userId);
        // Verify if the user is allowed to update their account
        verifyAccountStatus(user);
        var credentials = getUserCredentialById(user.getId());
        // Check if the current password from db matches the currentPassword in the request/provided
        if (!encoder.matches(currentPassword, credentials.getPassword())) { throw new ApiException(" Existing currentPassword is incorrect, Kindly try again"); }
        credentials.setPassword(encoder.encode(newPassword));
        credentialRepository.save(credentials);
    }

    @Override
    public User updateUser(String userId, String firstName, String lastName, String email, String phone, String bio) {
        var userEntity = getUserEntityByUserId(userId); // UserId is coming from the logged-in user
        userEntity.setFirstName(firstName);
        userEntity.setLastName(lastName);
        userEntity.setEmail(email);
        userEntity.setPhone(phone);
        userEntity.setBio(bio);
        userRepository.save(userEntity);
        return fromUserEntity(userEntity, userEntity.getRole(), getUserCredentialById(userEntity.getId()));
    }

    @Override
    public void updateRole(String userId, String role) {
        //We need the UserEntity
        var userEntity = getUserEntityByUserId(userId);
        System.out.println("User role to be updated" + userEntity);
        //Update the role name
        userEntity.setRole(getRoleName(role));
        System.out.println("User role to be updated" + userEntity);
        //Save role name to database
        userRepository.save(userEntity);

    }

    @Override
    public void toggleCredentialsExpired(String userId) {
        var userEntity = getUserEntityByUserId(userId);
        var credentials = getUserCredentialById(userEntity.getId());
        //credentials.setUpdatedAt(LocalDateTime.of(1995, 7, 12, 11, 11));
        // A better approach
        // If the credentials is over 90days
        if(credentials.getUpdatedAt().plusDays(NINETY_DAYS).isAfter(now())) {
            // Make the updated at to now
            credentials.setUpdatedAt(now());
        } else {
            // Make it expire
            credentials.setUpdatedAt(LocalDateTime.of(1995, 7, 12, 11, 11));
        }
        userRepository.save(userEntity);
        //STOPPED HERE
    }

    @Override
    public void toggleAccountExpired(String userId) {
        var userEntity = getUserEntityByUserId(userId);
        userEntity.setAccountNonExpired(!userEntity.isAccountNonExpired());
        userRepository.save(userEntity);
    }

    @Override
    public void toggleAccountEnabled(String userId) {
        var userEntity = getUserEntityByUserId(userId);
        userEntity.setEnabled(!userEntity.isEnabled());
        userRepository.save(userEntity);
    }

    @Override
    public void toggleAccountLocked(String userId) {
        var userEntity = getUserEntityByUserId(userId);
        userEntity.setAccountNonLocked(!userEntity.isAccountNonLocked());
        userRepository.save(userEntity);

    }

    @Override
    public String uploadPhoto(String userId, MultipartFile file) {
        //Get the user entity
        var user = getUserEntityByUserId(userId);
        //Create helper method
        var photoUrl = photoFunction.apply(userId, file);
        /* Make sure the url is diff everytime its updated so the will not refetch the image
        The reason we want to change the url everytime is because
        we want the browser to fetch the image everytime the sources attribute is different
         */
        user.setImageUrl(photoUrl + "?timestamp=" + System.currentTimeMillis());
        userRepository.save(user);
        return photoUrl;
    }

    @Override
    public User getUserById(Long id) {
        var userEntity = userRepository.findById(id).orElseThrow(() -> new ApiException("User not Found"));
        System.out.println("User for document debug " + userEntity);
        return fromUserEntity(userEntity, userEntity.getRole(), getUserCredentialById(userEntity.getId()));
    }

    private final BiFunction<String, MultipartFile, String> photoFunction = (id, file) -> {
        /*
        Going to use the string UUID which is the userId of the user can also
        use a random generated string or UUID as the image url of the user.
         */
        var filename = id + ".png";
        try{
           // var fileStorageLocation = Paths.get(System.getProperty("user.home") + "/Downloads/uploads").toAbsolutePath().normalize();
            var fileStorageLocation = Paths.get(PHOTO_DIR).toAbsolutePath().normalize();
            if(!Files.exists(fileStorageLocation)) {
                Files.createDirectories(fileStorageLocation); }
            Files.copy(file.getInputStream(), fileStorageLocation.resolve(filename), REPLACE_EXISTING);
            return ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path("/user/image/" + filename).toUriString();
        }catch (Exception exception) {
            throw new ApiException("Unable to save image");
        }
    };

    private ConfirmationEntity getUserConfirmation(UserEntity user) {
        return confirmationRepository.findByUserEntity(user).orElse(null);
    }


    //Creating helper method for the createNewUser method
    private UserEntity createNewUser(String firstName, String lastName, String email) {
        var role = getRoleName(Authority.USER.name()); //Find a role in the database by the name USER
        System.out.print(createUserEntity("The roles" + firstName, lastName, email, role));//Todo: Create the method
        return createUserEntity(firstName, lastName, email, role); //Todo: Create the method
    }
}
