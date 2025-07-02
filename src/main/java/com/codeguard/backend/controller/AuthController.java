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
@CrossOrigin("http://localhost:5173")
public class AuthController {

    private final AuthService  authService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignupRequest signupRequest)
    {
        String result = authService.signup(signupRequest);
        System.out.println("signup"+signupRequest);
        return ResponseEntity.ok(result);
    }
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        String result = authService.login(loginRequest);
        System.out.println(result);
        return ResponseEntity.ok(result);
    }

}
