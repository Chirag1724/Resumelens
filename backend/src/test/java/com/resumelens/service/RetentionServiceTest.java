package com.resumelens.service;

import com.resumelens.entity.*;
import com.resumelens.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class RetentionServiceTest {

    @Autowired
    private RetentionService retentionService;

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private MatchResultRepository matchResultRepository;

    @Autowired
    private SectionScoreRepository sectionScoreRepository;

    @Autowired
    private JobDescriptionRepository jobDescriptionRepository;

    @Autowired
    private UserRepository userRepository;

    private JobDescription jobDescription;

    @BeforeEach
    void setUp() {
        jobDescription = jobDescriptionRepository.save(new JobDescription("Backend Engineer", "java spring postgres", "skills"));
    }

    @Test
    @SuppressWarnings("null")
    void guestDataRetention_deletesOldGuestResumesAndPreservesNewGuestAndUserResumes() {
        // Explicit cutoff timestamp: 2026-10-24T00:00:00
        LocalDateTime cutoff = LocalDateTime.of(2026, 10, 24, 0, 0, 0);

        // 1. Old guest resume (uploaded 2026-10-15T12:00:00, before cutoff)
        Resume oldGuestResume = new Resume(null, "old_guest.pdf", "old guest resume content", "Backend Engineer", 3);
        oldGuestResume.setUploadedAt(LocalDateTime.of(2026, 10, 15, 12, 0, 0));
        oldGuestResume = resumeRepository.save(oldGuestResume);

        MatchResult oldGuestResult = new MatchResult(oldGuestResume, jobDescription);
        oldGuestResult.setOverallScore(70.0);
        oldGuestResult.setStatusLabel("Strong Match");
        oldGuestResult = matchResultRepository.save(oldGuestResult);

        SectionScore oldGuestScore1 = new SectionScore(oldGuestResult, SectionScore.ATS_COMPATIBILITY, 75.0, "Good", "[]", "[]");
        SectionScore oldGuestScore2 = new SectionScore(oldGuestResult, SectionScore.SKILLS_VALIDATION, 65.0, "Average", "[]", "[]");
        sectionScoreRepository.save(oldGuestScore1);
        sectionScoreRepository.save(oldGuestScore2);

        // 2. New guest resume (uploaded 2026-10-28T12:00:00, after cutoff)
        Resume newGuestResume = new Resume(null, "new_guest.pdf", "new guest resume content", "Backend Engineer", 2);
        newGuestResume.setUploadedAt(LocalDateTime.of(2026, 10, 28, 12, 0, 0));
        newGuestResume = resumeRepository.save(newGuestResume);

        MatchResult newGuestResult = new MatchResult(newGuestResume, jobDescription);
        newGuestResult.setOverallScore(85.0);
        newGuestResult.setStatusLabel("Excellent Match");
        newGuestResult = matchResultRepository.save(newGuestResult);

        SectionScore newGuestScore = new SectionScore(newGuestResult, SectionScore.ATS_COMPATIBILITY, 85.0, "Great", "[]", "[]");
        sectionScoreRepository.save(newGuestScore);

        // 3. Old resume that belongs to a user (uploaded 2026-10-15T12:00:00, before cutoff, but user is not null)
        User registeredUser = userRepository.save(new User("registered@example.com", "bcrypt_hash", "Jane"));
        Resume userResume = new Resume(registeredUser, "user_resume.pdf", "user resume content", "Backend Engineer", 5);
        userResume.setUploadedAt(LocalDateTime.of(2026, 10, 15, 12, 0, 0));
        userResume = resumeRepository.save(userResume);

        MatchResult userResult = new MatchResult(userResume, jobDescription);
        userResult.setOverallScore(90.0);
        userResult.setStatusLabel("Excellent Match");
        userResult = matchResultRepository.save(userResult);

        SectionScore userScore = new SectionScore(userResult, SectionScore.ATS_COMPATIBILITY, 90.0, "Excellent", "[]", "[]");
        sectionScoreRepository.save(userScore);

        // Execute cleanup with the explicit cutoff
        int deletedRows = retentionService.cleanupExpiredGuestData(cutoff);

        // Expected deleted rows: 2 section scores + 1 match result + 1 resume = 4 rows
        assertEquals(4, deletedRows);

        // Verify: old guest resume and its children are deleted
        assertFalse(resumeRepository.findById(oldGuestResume.getId()).isPresent(), "Old guest resume must be deleted");
        assertFalse(matchResultRepository.findById(oldGuestResult.getId()).isPresent(), "Old guest match result must be deleted");
        assertTrue(sectionScoreRepository.findByMatchResultId(oldGuestResult.getId()).isEmpty(), "Old guest section scores must be deleted");

        // Verify: new guest resume and its children stay
        assertTrue(resumeRepository.findById(newGuestResume.getId()).isPresent(), "New guest resume must stay");
        assertTrue(matchResultRepository.findById(newGuestResult.getId()).isPresent(), "New guest match result must stay");
        assertEquals(1, sectionScoreRepository.findByMatchResultId(newGuestResult.getId()).size(), "New guest section scores must stay");

        // Verify: old resume belonging to a user and its children stay
        assertTrue(resumeRepository.findById(userResume.getId()).isPresent(), "Old user resume must stay");
        assertTrue(matchResultRepository.findById(userResult.getId()).isPresent(), "Old user match result must stay");
        assertEquals(1, sectionScoreRepository.findByMatchResultId(userResult.getId()).size(), "Old user section scores must stay");
    }
}

