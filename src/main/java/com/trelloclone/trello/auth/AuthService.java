package com.trelloclone.trello.auth;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.trelloclone.trello.security.JwtService;
import com.trelloclone.trello.user.User;
import com.trelloclone.trello.user.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public void signup(SignupRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();

        String hashedPassword = passwordEncoder.encode(request.getPassword());

        user.setEmail(request.getEmail());
        user.setPassword(hashedPassword);
        user.setUserName(request.getUserName());

        userRepository.save(user);
    }

    public String signin(SigninRequest request) {
        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());

        if (optionalUser.isEmpty()) {
            throw new RuntimeException("Invalid credentials");
        }

        User user = optionalUser.get();

        boolean isMatching = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!isMatching) {
            throw new RuntimeException("Invalid credentials");
        }

        return jwtService.generateToken(user);

    }

    public UserResponse getCurrentUser(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("user not found"));

        return new UserResponse(user.getEmail(), user.getUserName());
    }
}
