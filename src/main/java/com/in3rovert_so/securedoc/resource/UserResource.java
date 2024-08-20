package com.in3rovert_so.securedoc.resource;

import com.in3rovert_so.securedoc.domain.Response;
import com.in3rovert_so.securedoc.dto.QrCodeRequest;
import com.in3rovert_so.securedoc.dto.User;
import com.in3rovert_so.securedoc.dtorequest.*;
import com.in3rovert_so.securedoc.enumeration.TokenType;
import com.in3rovert_so.securedoc.service.JwtService;
import com.in3rovert_so.securedoc.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.of;
import static com.in3rovert_so.securedoc.utils.RequestUtils.getResponse;
import static java.util.Collections.emptyMap;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = {"/user"})
public class UserResource {
    private final UserService userService;

    //Because we need to pass in the cookie in the response we need to call the JWT services
    private final JwtService jwtservice;
   @PostMapping("/register")
    public ResponseEntity<Response> saveUser(@RequestBody @Valid UserRequest user, HttpServletRequest request) {
        userService.createUser(user.getFirstName(), user.getLastName(), user.getEmail(), user.getPassword());

        return ResponseEntity.created(getUri()).body(getResponse(request, emptyMap(), "Account created. Check your email to enable your account", CREATED));
    }

    /*Todo: setupMfa endpoints
This endpoints is going to all us to set up mfa, and user need to be logged in before they could so this.
 */
    @PatchMapping("/mfa/setup")
    public ResponseEntity<Response> setupMfa(@AuthenticationPrincipal User userPrincipal, HttpServletRequest request) {
        var user = userService.setUpMfa(userPrincipal.getId()); //Set up mfa for each specific id
        return ResponseEntity.ok().body(getResponse(request, Map.of("user", user), "Mfa set up successfully", OK));
    }
    //This is the endpoint that allow use to cancel mfa
    @PatchMapping("/mfa/cancel")
    public ResponseEntity<Response> cancelMfa(@AuthenticationPrincipal User userPrincipal, HttpServletRequest request) {
        var user = userService.cancelMfa(userPrincipal.getId());
        return ResponseEntity.ok().body(getResponse(request, Map.of("user", user), "User MFA cancel successfully", OK));
    }
    @PostMapping("/verify/qrcode")
    public ResponseEntity<Response> verifyQrcode(@RequestBody QrCodeRequest qrCodeRequest, HttpServletResponse response, HttpServletRequest request) {
        var user = userService.verifyQrCode(qrCodeRequest.getUserId(), qrCodeRequest.getQrCode());
        jwtservice.addCookie(response, user, TokenType.ACCESS);
        jwtservice.addCookie(response, user, TokenType.REFRESH);
        return ResponseEntity.ok().body(getResponse(request, Map.of("user", user), "User Qr code verified", OK));
    }

    @GetMapping("/verify/account")
    public ResponseEntity<Response> verifyAccount(@RequestParam("key") String key, HttpServletRequest request) {
        userService.verifyAccountKey(key);
        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "Account Verified. ", OK));
    }
    //TODO: Testing and Building a simple response: Deleted

    // Update user when they are logged in - STARTS
    @PatchMapping("/updatePassword")
    public ResponseEntity<Response> updatePassword(@AuthenticationPrincipal User user, @RequestBody updateUserPasswordRequest passwordRequest, HttpServletRequest request) {
        userService.updatePassword(user.getUserId(), passwordRequest.getPassword(), passwordRequest.getNewPassword(), passwordRequest.getConfirmNewPassword());
        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "User Password Updated Successfully", OK));
    }

    // START - Reset password when user not logged in0
    //Todo: Read and Research more on building and versioning api endpoints, make sure to refactor existing
    @PostMapping("/resetpassword")
    public ResponseEntity<Response> resetPassword(@RequestBody @Valid EmailUserResetPasswordRequest emailRequest, HttpServletRequest request) {
        userService.resetPassword(emailRequest.getEmail());
        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "Kindly check your email for the link to reset your password", OK));
    }
    @GetMapping("/verify/password")
    public ResponseEntity<Response> verifyResetPassword(@RequestParam("key") String key, HttpServletRequest request) {
       var user =  userService.verifyPasswordKey(key);
        return ResponseEntity.ok().body(getResponse(request, of("user", user), "Kindly enter a new password", OK));
    }

    @PostMapping("/resetpassword/reset")
    public ResponseEntity<Response> activateResetPassword(@RequestBody @Valid ResetPasswordRequest resetPasswordRequest, HttpServletRequest request) {
        userService.updatePassword(resetPasswordRequest.getUserId(), resetPasswordRequest.getNewPassword(), resetPasswordRequest.getConfirmNewPassword());
        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "User password reset successful", OK));
    }
    // END - Reset password when user not logged in

    //BEGIN USER PROFILE
    @GetMapping("/profile")
    public ResponseEntity<Response> profile(@AuthenticationPrincipal User userPrincipal, HttpServletRequest request) {
        var user = userService.getUserByUserId(userPrincipal.getUserId());
        return ResponseEntity.ok().body(getResponse(request, of("user", user), "User Profile Retrieved", OK));
    }
    @PatchMapping("/update")
    public ResponseEntity<Response> update(@AuthenticationPrincipal User userPrincipal, @RequestBody UserRequest userRequest, HttpServletRequest request) {
        var user = userService.updateUser(userPrincipal.getUserId(), userRequest.getFirstName(), userRequest.getLastName(), userRequest.getEmail(), userRequest.getPhone(), userRequest.getBio());
        return ResponseEntity.ok().body(getResponse(request, of("user", user), "User updated Successfully", OK));
    }
    @PatchMapping("/updaterole")
    public ResponseEntity<Response> updaterole(@AuthenticationPrincipal User userPrincipal, @RequestBody RoleRequest roleRequest, HttpServletRequest request) {
        // In this case we are not returning a user, we are only updating the roles of the users
        userService.updateRole(userPrincipal.getUserId(), roleRequest.getRole());
        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "User Role Updated Successfully", OK));
    }
    // User Advance Setting Starts
    @PatchMapping("/togglecredentialsexpired")
    public ResponseEntity<Response> toggleCredentialsExpired(@AuthenticationPrincipal User user, HttpServletRequest request) {
        userService.toggleCredentialsExpired(user.getUserId());
        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "User Credential Updated Successfully", OK));
    }

    @PatchMapping("/toggleaccountexpired")
    public ResponseEntity<Response> toggleAccountExpired(@AuthenticationPrincipal User user, HttpServletRequest request) {
        userService.toggleAccountExpired(user.getUserId());
        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "User Expiration Status Updated Successfully", OK));
    }

    @PatchMapping("/toggleaccountlocked")
    public ResponseEntity<Response> toggleAccountLocked(@AuthenticationPrincipal User user, HttpServletRequest request) {
        userService.toggleAccountLocked(user.getUserId());
        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "User locked Status  Updated Successfully", OK));
    }

    @PatchMapping("/toggleaccountenabled")
    public ResponseEntity<Response> toggleAccountEnabled(@AuthenticationPrincipal User user, HttpServletRequest request) {
        userService.toggleAccountEnabled(user.getUserId());
        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "User Enabled Status Updated Successfully", OK));
    }

    // User Advance Setting Ends

    @PatchMapping("/photo")
    public ResponseEntity<Response> uploadPhoto(@AuthenticationPrincipal User user, @RequestParam("file") MultipartFile file, HttpServletRequest request) {
        // In this case we are not returning a user, we are only updating the roles of the users
        var imageUrl = userService.uploadPhoto(user.getUserId(), file);
        return ResponseEntity.ok().body(getResponse(request, of("imageUrl", imageUrl), "Profile Photo Update Successfully", OK));
    }

    private URI getUri() {
        return URI.create("");

    }
}
