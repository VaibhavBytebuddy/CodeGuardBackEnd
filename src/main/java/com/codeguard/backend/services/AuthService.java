package com.codeguard.backend.services;

import com.codeguard.backend.dto.LoginRequest;
import com.codeguard.backend.dto.SignupRequest;
import com.codeguard.backend.model.User;
import com.codeguard.backend.repository.UserRepository;
import com.codeguard.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public String signup(SignupRequest signupRequest)
    {
        Optional<User> userExist = userRepository.findByEmail(signupRequest.getEmail());
        if(userExist.isPresent())
        {
            return "user already exists with this email!";
        }
        User user=User.builder()
                .username(signupRequest.getUsername())
                .email(signupRequest.getEmail())
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .role("ROLE_USER")
                .build();

        userRepository.save(user);
        return "Signup Successfully";

    }

    public String login(LoginRequest loginRequest)
    {
        Optional<User> user = userRepository.findByEmail(loginRequest.getEmail());

        if ((user.isPresent()))
        {
            boolean passwordMatches = passwordEncoder.matches(loginRequest.getPassword(), user.get().getPassword());
            if (passwordMatches)
            {
                String token=jwtUtil.generateToken(user.get().getEmail());
                return "Bearer "+token;
            }else {
                return "Invalid password";
            }
        }else {
            return "User not found";
        }
    }
}
