package com.codeguard.backend.controller;


import com.codeguard.backend.dto.LoginRequest;
import com.codeguard.backend.dto.SignupRequest;
import com.codeguard.backend.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin("*")
public class AuthController {

    private final AuthService  authService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignupRequest signupRequest)
    {
        String result = authService.signup(signupRequest);
        return ResponseEntity.ok(result);
    }
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        String result = authService.login(loginRequest);
        return ResponseEntity.ok(result);
    }

}
