package pl.edu.pw.ee.zai.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.edu.pw.ee.zai.dto.LoginRequest;
import pl.edu.pw.ee.zai.dto.LoginResponse;
import pl.edu.pw.ee.zai.dto.PasswordChangeRequest;
import pl.edu.pw.ee.zai.service.AuthService;

import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        authService.logout(authHeader);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "/password", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> changePassword(Principal principal,
                                               @Valid @RequestBody PasswordChangeRequest request) {
        authService.changePassword(principal.getName(), request);
        return ResponseEntity.noContent().build();
    }
}
