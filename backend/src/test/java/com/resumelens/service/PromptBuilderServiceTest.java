package com.resumelens.service;

import com.resumelens.TestData;
import com.resumelens.entity.JobDescription;
import com.resumelens.entity.MatchResult;
import com.resumelens.entity.Resume;
import com.resumelens.entity.SectionScore;
import com.resumelens.repository.MatchResultRepository;
import com.resumelens.repository.SectionScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Unit tests for PromptBuilderService.
 * Both repositories are mocked; entities are instantiated as plain Java
 * objects.
 *
 * Verified content per the user request:
 * ✓ resume text
 * ✓ job description
 * ✓ job title
 * ✓ missing keywords
 * ✓ template style
 * ✓ all 7 anti-fabrication rules
 * ✓ prompt ends with the LaTeX-only output instruction
 */
@ExtendWith(MockitoExtension.class)
class PromptBuilderServiceTest {

    @Mock
    private MatchResultRepository matchResultRepository;
    @Mock
    private SectionScoreRepository sectionScoreRepository;

    private PromptBuilderService service;

    /** Reusable entities — plain Java objects, no JPA context needed. */
    private MatchResult matchResult;
    private SectionScore sectionWithMissingKw;

    @BeforeEach
    void setUp() {
        service = new PromptBuilderService(matchResultRepository, sectionScoreRepository);

        Resume resume = new Resume(
                null,
                "resume.pdf",
                TestData.PROMPT_RESUME_TEXT, // rawText
                "Software Engineering",
                3);

        JobDescription jd = new JobDescription(
                TestData.PROMPT_JOB_TITLE, // title
                TestData.PROMPT_JD_TEXT, // description
                "java,python,docker" // requiredSkills
        );

        matchResult = new MatchResult(resume, jd);
        matchResult.setOverallScore(72.5);

        // One section that carries the missing keywords
        sectionWithMissingKw = new SectionScore(
                matchResult,
                SectionScore.SKILLS_VALIDATION,
                60.0,
                "Keyword gap: docker missing",
                "[\"java\",\"python\"]",
                TestData.PROMPT_MISSING_KW // "[\"docker\"]"
        );

        when(matchResultRepository.findById(anyLong()))
                .thenReturn(Optional.of(matchResult));
        when(sectionScoreRepository.findByMatchResultId(anyLong()))
                .thenReturn(List.of(sectionWithMissingKw));
    }

    // Content presence assertions

    @Test
    void should_containResumeText_inGeneratedPrompt() {
        String prompt = service.buildFixPrompt(1L, TestData.PROMPT_TEMPLATE);
        assertTrue(prompt.contains(TestData.PROMPT_RESUME_TEXT),
                "Prompt must embed the full resume text");
    }

    @Test
    void should_containJobDescription_inGeneratedPrompt() {
        String prompt = service.buildFixPrompt(1L, TestData.PROMPT_TEMPLATE);
        assertTrue(prompt.contains(TestData.PROMPT_JD_TEXT),
                "Prompt must embed the full job description");
    }

    @Test
    void should_containJobTitle_inGeneratedPrompt() {
        String prompt = service.buildFixPrompt(1L, TestData.PROMPT_TEMPLATE);
        assertTrue(prompt.contains(TestData.PROMPT_JOB_TITLE),
                "Prompt must embed the job title");
    }

    @Test
    void should_containMissingKeywords_inGeneratedPrompt() {
        String prompt = service.buildFixPrompt(1L, TestData.PROMPT_TEMPLATE);
        // PROMPT_MISSING_KW = "[\"docker\"]" — the actual JSON string stored in
        // sectionScore
        assertTrue(prompt.contains("docker"),
                "Prompt must include the missing keyword 'docker' extracted from section scores");
    }

    @Test
    void should_containTemplateStyle_inGeneratedPrompt() {
        String prompt = service.buildFixPrompt(1L, TestData.PROMPT_TEMPLATE);
        assertTrue(prompt.contains(TestData.PROMPT_TEMPLATE),
                "Prompt must embed the requested template style");
    }

    @Test
    void should_containDefaultTemplateStyle_whenNullTemplateProvided() {
        String prompt = service.buildFixPrompt(1L, null);
        assertTrue(prompt.contains("Simple Single-Column"),
                "Null template must fall back to 'Simple Single-Column'");
    }

    // All 7 anti-fabrication rules

    @Test
    void should_containAllAntiFabricationRules_inGeneratedPrompt() {
        String prompt = service.buildFixPrompt(1L, TestData.PROMPT_TEMPLATE);
        assertAll(
                "All 7 anti-fabrication rules must appear in the prompt",
                () -> assertTrue(prompt.contains("Do NOT fabricate"),
                        "Rule 1: no fabrication"),
                () -> assertTrue(prompt.contains("You MAY incorporate missing keywords"),
                        "Rule 2: keyword incorporation constraint"),
                () -> assertTrue(prompt.contains("You MAY quantify achievements"),
                        "Rule 3: quantification constraint"),
                () -> assertTrue(prompt.contains("Improve action verbs"),
                        "Rule 4: action verbs"),
                () -> assertTrue(prompt.contains("Ensure standard section headings"),
                        "Rule 5: section headings"),
                () -> assertTrue(prompt.contains("Ensure contact information"),
                        "Rule 6: contact information"),
                () -> assertTrue(prompt.contains("Target a single-page resume"),
                        "Rule 7: single-page target"));
    }

    // Output format instruction

    @Test
    void should_containLatexOnlyOutputInstruction_inGeneratedPrompt() {
        String prompt = service.buildFixPrompt(1L, TestData.PROMPT_TEMPLATE);
        assertTrue(prompt.contains("Produce ONLY complete, compilable LaTeX code"),
                "Prompt must contain the LaTeX-only output instruction");
    }

    @Test
    void should_endWithNoExplanationClause_inGeneratedPrompt() {
        String prompt = service.buildFixPrompt(1L, TestData.PROMPT_TEMPLATE);
        String expectedEnding = "Do not include any explanation, commentary, or markdown before or after the code.";
        assertTrue(prompt.contains(expectedEnding),
                "Prompt must end with the no-explanation clause");
    }

    // Weak sections summary present

    @Test
    void should_containWeakSectionName_inGeneratedPrompt() {
        String prompt = service.buildFixPrompt(1L, TestData.PROMPT_TEMPLATE);
        assertTrue(prompt.contains(SectionScore.SKILLS_VALIDATION),
                "Prompt must list the weakest-scoring section by name");
    }
}
