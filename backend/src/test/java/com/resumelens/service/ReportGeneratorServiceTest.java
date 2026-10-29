package com.resumelens.service;

import com.resumelens.entity.JobDescription;
import com.resumelens.entity.MatchResult;
import com.resumelens.entity.Resume;
import com.resumelens.entity.SectionScore;
import com.resumelens.exception.ResourceNotFoundException;
import com.resumelens.repository.MatchResultRepository;
import com.resumelens.repository.SectionScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportGeneratorServiceTest {

    private static final UUID TEST_UUID = UUID.randomUUID();

    @Mock
    private MatchResultRepository matchResultRepository;
    @Mock
    private SectionScoreRepository sectionScoreRepository;

    private ReportGeneratorService service;

    private MatchResult matchResult;
    private Resume resume;
    private JobDescription jobDescription;

    @BeforeEach
    void setUp() {
        service = new ReportGeneratorService(matchResultRepository, sectionScoreRepository);

        resume = new Resume(
            null,
            "jane_doe_resume.pdf",
            "Experienced software engineer with React and Java background.",
            "Senior Frontend Engineer",
            5,
            false,
            false
        );

        jobDescription = new JobDescription(
            "Senior Frontend Engineer",
            "Looking for strong React and TypeScript experience.",
            "React, TypeScript, Docker"
        );

        matchResult = new MatchResult(resume, jobDescription);
        matchResult.setOverallScore(84.0);
        matchResult.setStatusLabel("STRONG");
        matchResult.setKeywordMatchPercent(0.75);
    }

    @Test
    void should_generateValidPdf_whenValidPublicIdProvided() {
        List<SectionScore> sections = List.of(
            new SectionScore(matchResult, SectionScore.ATS_COMPATIBILITY, 85.0, "Standard formatting.", null, null),
            new SectionScore(matchResult, SectionScore.SKILLS_VALIDATION, 75.0, "Matched core skills.", "[\"React\",\"TypeScript\"]", "[\"Docker\",\"GraphQL\"]"),
            new SectionScore(matchResult, SectionScore.IMPACT_ACHIEVEMENTS, 65.0, "Needs more metrics.", null, null),
            new SectionScore(matchResult, SectionScore.PROJECT_QUALITY, 80.0, "Projects are solid.", null, null),
            new SectionScore(matchResult, SectionScore.WRITING_QUALITY, 90.0, "Grammar is clean.", null, null),
            new SectionScore(matchResult, SectionScore.PROFESSIONALISM, 88.0, "Structure is sound.", null, null)
        );

        when(matchResultRepository.findByPublicId(TEST_UUID)).thenReturn(Optional.of(matchResult));
        when(sectionScoreRepository.findByMatchResultPublicId(TEST_UUID)).thenReturn(sections);

        byte[] pdf = service.generatePdfReport(TEST_UUID);

        assertNotNull(pdf, "Generated PDF must not be null");
        assertTrue(pdf.length > 500, "PDF must have non-trivial size");

        // Verify PDF Header %PDF-
        assertEquals('%', (char) pdf[0]);
        assertEquals('P', (char) pdf[1]);
        assertEquals('D', (char) pdf[2]);
        assertEquals('F', (char) pdf[3]);
        assertEquals('-', (char) pdf[4]);
    }

    @Test
    void should_handleGraphicsAndFormattingRisks_inPdfGeneration() {
        Resume riskResume = new Resume(
            null,
            "portfolio_resume.pdf",
            "Graphic design resume with complex columns.",
            "UI Designer",
            3,
            true,
            true
        );

        MatchResult riskResult = new MatchResult(riskResume, jobDescription);
        riskResult.setOverallScore(45.0);
        riskResult.setStatusLabel("WEAK");
        riskResult.setKeywordMatchPercent(0.35);

        List<SectionScore> sections = List.of(
            new SectionScore(riskResult, SectionScore.ATS_COMPATIBILITY, 40.0, "Embedded images detected.", null, null),
            new SectionScore(riskResult, SectionScore.SKILLS_VALIDATION, 45.0, "Missing many skills.", "[]", "[\"Figma\",\"Design Systems\"]")
        );

        when(matchResultRepository.findByPublicId(TEST_UUID)).thenReturn(Optional.of(riskResult));
        when(sectionScoreRepository.findByMatchResultPublicId(TEST_UUID)).thenReturn(sections);

        byte[] pdf = service.generatePdfReport(TEST_UUID);

        assertNotNull(pdf);
        assertTrue(pdf.length > 500);
    }

    @Test
    void should_throwResourceNotFoundException_whenPublicIdDoesNotExist() {
        when(matchResultRepository.findByPublicId(TEST_UUID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.generatePdfReport(TEST_UUID));
    }
}
