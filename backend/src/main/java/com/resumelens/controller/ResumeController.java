package com.resumelens.controller;

import com.resumelens.dto.response.ApiResponse;
import com.resumelens.exception.InvalidInputException;
import com.resumelens.service.ResumeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
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
        String email = (authentication != null && authentication.isAuthenticated())
                       ? authentication.getName() : null;
        Long resumeId = resumeService.save(file, targetRole, experienceYears, email);
        return ResponseEntity.ok(ApiResponse.success(Map.of("resumeId", resumeId)));
    }
}