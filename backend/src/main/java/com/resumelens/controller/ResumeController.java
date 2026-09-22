package com.resumelens.controller;

import com.resumelens.dto.response.ApiResponse;
import com.resumelens.entity.Resume;
import com.resumelens.entity.User;
import com.resumelens.exception.InvalidInputException;
import com.resumelens.repository.ResumeRepository;
import com.resumelens.repository.UserRepository;
import com.resumelens.service.ResumeParserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/resumes")
public class ResumeController {

    private final ResumeParserService resumeParserService;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;

    public ResumeController(ResumeParserService resumeParserService,
                            ResumeRepository resumeRepository,
                            UserRepository userRepository) {
        this.resumeParserService = resumeParserService;
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
    }

    /**
     * Accepts a multipart file. Links resume to authenticated user if a valid
     * JWT is present; stores as guest upload otherwise.
     */
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Map<String, Long>>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "targetRole", required = false) String targetRole,
            @RequestParam(value = "experienceYears", required = false) Integer experienceYears,
            Authentication authentication) {

        if (file.isEmpty()) throw new InvalidInputException("Uploaded file is empty.");

        String rawText = resumeParserService.extractText(file);

        User user = null;
        if (authentication != null && authentication.isAuthenticated()) {
            user = userRepository.findByEmail(authentication.getName()).orElse(null);
        }

        Resume resume = resumeRepository.save(
            new Resume(user, file.getOriginalFilename(), rawText, targetRole, experienceYears));

        return ResponseEntity.ok(ApiResponse.success(Map.of("resumeId", resume.getId())));
    }
}