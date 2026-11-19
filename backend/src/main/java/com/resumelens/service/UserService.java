package com.resumelens.service;

import com.resumelens.dto.request.OAuthRequest;
import com.resumelens.dto.response.AuthResponse;
import com.resumelens.dto.response.UserProfileResponse;
import com.resumelens.entity.MatchResult;
import com.resumelens.entity.Resume;
import com.resumelens.entity.SectionScore;
import com.resumelens.entity.User;
import com.resumelens.exception.InvalidInputException;
import com.resumelens.exception.ResourceNotFoundException;
import com.resumelens.repository.MatchResultRepository;
import com.resumelens.repository.ResumeRepository;
import com.resumelens.repository.SectionScoreRepository;
import com.resumelens.repository.UserRepository;
import com.resumelens.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;
    private final MatchResultRepository matchResultRepository;
    private final SectionScoreRepository sectionScoreRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public UserService(UserRepository userRepository,
                       ResumeRepository resumeRepository,
                       MatchResultRepository matchResultRepository,
                       SectionScoreRepository sectionScoreRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.resumeRepository = resumeRepository;
        this.matchResultRepository = matchResultRepository;
        this.sectionScoreRepository = sectionScoreRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse register(String email, String password, String name) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new InvalidInputException("An account with this email already exists.");
        }
        User user = new User(normalizedEmail, passwordEncoder.encode(password), name);
        userRepository.save(user);
        String token = jwtTokenProvider.generateToken(normalizedEmail);
        return new AuthResponse(token, normalizedEmail, name);
    }

    public AuthResponse login(String email, String password) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(normalizedEmail, password));
        User user = userRepository.findByEmail(normalizedEmail)
            .orElseThrow(() -> new InvalidInputException("User not found after authentication."));
        String token = jwtTokenProvider.generateToken(normalizedEmail);
        return new AuthResponse(token, normalizedEmail, user.getName());
    }

    @Transactional
    public AuthResponse oauthLogin(OAuthRequest request) {
        String provider = request.provider().trim().toUpperCase(Locale.ROOT);
        if (!provider.equals("GOOGLE") && !provider.equals("GITHUB")) {
            throw new InvalidInputException("Unsupported OAuth provider: " + request.provider());
        }

        String rawEmail = request.email();
        if (rawEmail == null || rawEmail.isBlank()) {
            throw new InvalidInputException("Email is required for OAuth authentication.");
        }
        String normalizedEmail = rawEmail.trim().toLowerCase(Locale.ROOT);

        Optional<User> existingUser = userRepository.findByEmail(normalizedEmail);
        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
        } else {
            String randomHash = passwordEncoder.encode(UUID.randomUUID().toString());
            String displayName = (request.name() != null && !request.name().isBlank())
                ? request.name().trim()
                : normalizedEmail.split("@")[0];
            user = new User(normalizedEmail, randomHash, displayName, provider, request.providerId());
            userRepository.save(user);
        }

        String token = jwtTokenProvider.generateToken(normalizedEmail);
        return new AuthResponse(token, normalizedEmail, user.getName());
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String email) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        User user = userRepository.findByEmail(normalizedEmail)
            .orElseThrow(() -> new ResourceNotFoundException("User", normalizedEmail));
        long count = resumeRepository.countByUserId(user.getId());
        return new UserProfileResponse(
            user.getEmail(),
            user.getName(),
            user.getProvider(),
            user.getCreatedAt(),
            count
        );
    }

    @Transactional
    public void deleteAccount(String email) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        User user = userRepository.findByEmail(normalizedEmail)
            .orElseThrow(() -> new ResourceNotFoundException("User", normalizedEmail));

        List<Resume> userResumes = resumeRepository.findByUserId(user.getId());
        if (!userResumes.isEmpty()) {
            List<MatchResult> matchResults = matchResultRepository.findByResumeIn(userResumes);
            if (!matchResults.isEmpty()) {
                List<SectionScore> scores = sectionScoreRepository.findByMatchResultIn(matchResults);
                if (!scores.isEmpty()) {
                    sectionScoreRepository.deleteAll(scores);
                }
                matchResultRepository.deleteAll(matchResults);
            }
            resumeRepository.deleteAll(userResumes);
        }

        userRepository.delete(user);
    }
}
