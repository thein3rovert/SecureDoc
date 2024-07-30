package com.in3rovert_so.securedoc.utils;

import com.fasterxml.jackson.databind.ser.std.StdKeySerializers;
import com.fasterxml.jackson.databind.util.BeanUtil;
import com.in3rovert_so.securedoc.dto.User;
import com.in3rovert_so.securedoc.entity.CredentialEntity;
import com.in3rovert_so.securedoc.entity.RoleEntity;
import com.in3rovert_so.securedoc.entity.UserEntity;
import com.in3rovert_so.securedoc.exception.ApiException;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static com.in3rovert_so.securedoc.constant.Constants.NINETY_DAYS;
import static com.in3rovert_so.securedoc.constant.Constants.THE_IN3ROVERT_LLC;
import static dev.samstevens.totp.util.Utils.getDataUriForImage;
import static java.time.LocalDateTime.now;
import static org.apache.logging.log4j.util.Strings.EMPTY;

public class UserUtils {
    public static UserEntity createUserEntity(String firstName, String lastName, String email, RoleEntity role) {
        return UserEntity.builder()
                .userId(UUID.randomUUID().toString())
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .lastLogin(now())
                .accountNonExpired(true)
                .accountNonLocked(true)
                .mfa(false)
                .enabled(false)
                .loginAttempts(0)
                .qrCodeSecret(EMPTY)
                .phone(EMPTY)
                .bio(EMPTY)
                .imageUrl("")
                .role(role)
                .build();

    } //Todo: Create the method


    public static User fromUserEntity(UserEntity userEntity, RoleEntity role, CredentialEntity credentialEntity) {
        User user = new User();
        BeanUtils.copyProperties(userEntity, user); //Copy all the fields from the userEntity and put in the user.
        user.setLastLogin(userEntity.getLastLogin().toString());
        user.setCredentialsNonExpired(isCredentialsNonExpired(credentialEntity));
        user.setCreatedAt(userEntity.getCreatedAt().toString());
        user.setUpdatedAt(userEntity.getUpdatedAt().toString());
        user.setRole(role.getName());
        System.out.println(role.getName());
        user.setAuthorities(role.getAuthorities().getValue());
        System.out.print(role.getAuthorities().getValue());
        return user;
    }

    public static boolean isCredentialsNonExpired(CredentialEntity credentialEntity) {
        return credentialEntity.getUpdatedAt().plusDays(NINETY_DAYS).isAfter(LocalDateTime.now());
    }

    //2 Helper method to create the QR code data
    public static BiFunction<String, String, QrData> qrDataFunction = (email, qrCodeSecret) -> new QrData.Builder()
            .issuer(THE_IN3ROVERT_LLC)
            .label(email)
            .secret(qrCodeSecret)
            .algorithm(HashingAlgorithm.SHA1)
            .digits(6)
            .period(30) //Refreshes every thirty seconds QR refreshes
            .build();

    // 1 Create the qrcode uri
    public static BiFunction<String, String, String> qrCodeImageUri = (email, qrCodeSecret) -> {
        var data = qrDataFunction.apply(email, qrCodeSecret);
        var generator = new ZxingPngQrGenerator();
        byte[] imageData;
        try {
            imageData = generator.generate(data);
        }catch(Exception exception) {
            throw new ApiException("Unable to create QR code Uri");
        }
        return getDataUriForImage(imageData, generator.getImageMimeType());
    };
    //3 create the qr code secret
    public static Supplier<String> qrCodeSecret = () -> new DefaultSecretGenerator().generate(); // This will generate the secret

}
