package com.resumelens.service;

import com.resumelens.entity.Resume;
import com.resumelens.entity.User;
import com.resumelens.repository.ResumeRepository;
import com.resumelens.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Owns the resume upload lifecycle: text extraction, optional user linking, and persistence.
 * Extracted from ResumeController per layered architecture (controllers must not call repositories directly).
 */
@Service
public class ResumeService {

    private final ResumeParserService resumeParser;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;

    public ResumeService(ResumeParserService resumeParser,
                         ResumeRepository resumeRepository,
                         UserRepository userRepository) {
        this.resumeParser = resumeParser;
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
    }

    /**
     * Extracts text from the uploaded file, optionally links it to an authenticated user,
     * persists the Resume entity, and returns its public UUID.
     *
     * @param file             The uploaded resume file (PDF or DOCX).
     * @param targetRole       Optional target role hint; may be null.
     * @param experienceYears  Optional years of experience; may be null.
     * @param email            Authenticated user's email, or null for guest uploads.
     * @return The public UUID of the persisted Resume.
     */
    public UUID save(MultipartFile file, String targetRole, Integer experienceYears, String email) {
        String rawText = resumeParser.extractText(file);
        boolean hasGraphics = resumeParser.hasGraphics(file);
        User user = email != null ? userRepository.findByEmail(email).orElse(null) : null;
        Resume resume = resumeRepository.save(
            new Resume(user, file.getOriginalFilename(), rawText, targetRole, experienceYears, hasGraphics));
        return resume.getPublicId();
    }
}

