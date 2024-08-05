package com.in3rovert_so.securedoc.resource;

import com.in3rovert_so.securedoc.domain.Response;
import com.in3rovert_so.securedoc.dto.QrCodeRequest;
import com.in3rovert_so.securedoc.dto.User;
import com.in3rovert_so.securedoc.dtorequest.UserRequest;
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

import java.net.URI;
import java.util.Map;

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
    private final JwtService jwtservice
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
  // TODO: Testing and Building a simple response: Deleted

    private URI getUri() {
        return URI.create("");

    }
}
