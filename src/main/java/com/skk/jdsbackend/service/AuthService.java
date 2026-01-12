package com.skk.jdsbackend.service;

import com.skk.jdsbackend.dto.AuthResponse;
import com.skk.jdsbackend.dto.LoginRequest;
import com.skk.jdsbackend.dto.RegisterRequest;
import com.skk.jdsbackend.entity.RefreshToken;
import com.skk.jdsbackend.entity.Role;
import com.skk.jdsbackend.entity.User;
import com.skk.jdsbackend.exception.TokenRefreshException;
import com.skk.jdsbackend.repository.UserRepository;
import com.skk.jdsbackend.security.JwtUtils;
import com.skk.jdsbackend.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RefreshTokenService refreshTokenService;

    public AuthResponse register(RegisterRequest registerRequest) {
        // Check if username already exists
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("Error: Username is already taken!");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        // Create new user
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        // Set default role
        Set<Role> roles = new HashSet<>();
        roles.add(Role.CASE_WORKER);
        user.setRoles(roles);

        userRepository.save(user);

        // Auto-login after registration
        return login(new LoginRequest(registerRequest.getUsername(), registerRequest.getPassword()));
    }

    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String jwt = jwtUtils.generateJwtToken(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        return new AuthResponse(
                jwt,
                refreshToken.getToken(),
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles);
    }

    public AuthResponse refreshToken(String requestRefreshToken) {
        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtUtils.generateTokenFromUsername(user.getUsername());
                    RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());
                    List<String> roles = user.getRoles().stream()
                            .map(Role::name)
                            .collect(Collectors.toList());

                    // System.out.println("Refresh token generated: " + refreshToken.getToken());

                    return new AuthResponse(
                            token,
                            refreshToken.getToken(),
                            user.getId(),
                            user.getUsername(),
                            user.getEmail(),
                            roles);
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                        "Refresh token is not in database!"));
    }

    public void logout(String refreshToken) {
        refreshTokenService.deleteByToken(refreshToken);
    }
}
