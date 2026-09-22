package com.resumelens.service;

import com.resumelens.dto.response.AuthResponse;
import com.resumelens.entity.User;
import com.resumelens.exception.InvalidInputException;
import com.resumelens.repository.UserRepository;
import com.resumelens.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse register(String email, String password, String name) {
        if (userRepository.existsByEmail(email)) {
            throw new InvalidInputException("An account with this email already exists.");
        }
        User user = new User(email, passwordEncoder.encode(password), name);
        userRepository.save(user);
        String token = jwtTokenProvider.generateToken(email);
        return new AuthResponse(token, email, name);
    }

    public AuthResponse login(String email, String password) {
        // authenticate() throws BadCredentialsException on failure — result is intentionally unused
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, password));
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new InvalidInputException("User not found after authentication."));
        String token = jwtTokenProvider.generateToken(email);
        return new AuthResponse(token, email, user.getName());
    }
}