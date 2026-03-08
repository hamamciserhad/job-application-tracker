package com.jobtracker.controller;

import com.jobtracker.dto.request.LoginRequest;
import com.jobtracker.dto.request.RefreshRequest;
import com.jobtracker.dto.request.RegisterRequest;
import com.jobtracker.dto.response.AccessTokenResponse;
import com.jobtracker.dto.response.ApiResponse;
import com.jobtracker.dto.response.AuthResponse;
import com.jobtracker.dto.response.UserResponse;
import com.jobtracker.entity.User;
import com.jobtracker.exception.InvalidTokenException;
import com.jobtracker.exception.ResourceNotFoundException;
import com.jobtracker.mapper.UserMapper;
import com.jobtracker.security.JwtTokenProvider;
import com.jobtracker.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.register(request);
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        AuthResponse body = new AuthResponse(accessToken, refreshToken, userMapper.toResponse(user));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(body));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.authenticate(request.email(), request.password());
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        AuthResponse body = new AuthResponse(accessToken, refreshToken, userMapper.toResponse(user));
        return ResponseEntity.ok(ApiResponse.of(body));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AccessTokenResponse>> refresh(@Valid @RequestBody RefreshRequest request) {
        String token = request.refreshToken();
        if (!jwtTokenProvider.validateToken(token) || !jwtTokenProvider.isRefreshToken(token)) {
            throw new InvalidTokenException("Invalid or expired refresh token");
        }
        Long userId = jwtTokenProvider.getUserId(token);
        User user = userService.findById(userId)
                .orElseThrow(() -> new InvalidTokenException("User associated with token no longer exists"));
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
        return ResponseEntity.ok(ApiResponse.of(new AccessTokenResponse(newAccessToken)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> me(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(ApiResponse.of(userMapper.toResponse(user)));
    }
}
