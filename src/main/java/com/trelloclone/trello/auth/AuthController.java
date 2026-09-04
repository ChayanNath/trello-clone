package com.trelloclone.trello.auth;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trelloclone.trello.common.ApiResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ApiResponse signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);

        return new ApiResponse("Success", "User created successfully");
    }

    @PostMapping("/signin")
    public ApiResponse signin(@Valid @RequestBody SigninRequest request) {
        authService.signin(request);

        return new ApiResponse("Success", "Logged in successfully");
    }
}
