package com.resumelens.service;

import com.resumelens.TestData;
import com.resumelens.algorithm.SimilarityCalculator;
import com.resumelens.dto.response.SuggestionResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SuggestionEngine.
 * Uses a real SimilarityCalculator (pure algorithm, no I/O).
 */
class SuggestionEngineTest {

    private final SuggestionEngine engine = new SuggestionEngine(new SimilarityCalculator());

    // Missing keyword detection

    @Test
    void should_identifyMissingKeywords_whenJdTermsAbsentFromResume() {
        // JD has "java kubernetes docker react"; resume has only "java spring boot"
        List<String> missing = engine.findMissingKeywords(TestData.SUGGESTION_RESUME, TestData.SUGGESTION_JD);

        assertFalse(missing.isEmpty(), "Missing keyword list must not be empty");
        assertTrue(missing.contains("kubernetes"), "'kubernetes' must be flagged as missing");
        assertTrue(missing.contains("docker"), "'docker' must be flagged as missing");
        assertTrue(missing.contains("react"), "'react' must be flagged as missing");
    }

    @Test
    void should_notFlagKeyword_whenTermPresentInResume() {
        List<String> missing = engine.findMissingKeywords(TestData.SUGGESTION_RESUME, TestData.SUGGESTION_JD);
        assertFalse(missing.contains("java"),
                "'java' is present in both resume and JD — must not be in missing list");
    }

    @Test
    void should_notFlagShortTokens_whenJdTermIsOnly1Or2Characters() {
        // length ≤ 2 tokens must be filtered out
        String jd = "java go c"; // "go" and "c" are too short
        String resume = "python spring";
        List<String> missing = engine.findMissingKeywords(resume, jd);

        assertFalse(missing.contains("go"), "'go' (length 2) must be excluded from missing list");
        assertFalse(missing.contains("c"), "'c' (length 1) must be excluded from missing list");
    }

    @Test
    void should_returnEmptyMissingList_whenResumeContainsAllJdKeywords() {
        String resume = "java kubernetes docker react experienced developer";
        List<String> missing = engine.findMissingKeywords(resume, TestData.SUGGESTION_JD);
        assertTrue(missing.isEmpty(),
                "When all JD keywords are in resume the missing list must be empty");
    }

    // Weak verb detection

    @Test
    void should_detectWeakVerbs_whenResumeContainsWeakPhrases() {
        List<String> findings = engine.detectWeakVerbs(TestData.SUGGESTION_RESUME);

        assertFalse(findings.isEmpty(), "Weak verb findings must not be empty");
        // "responsible for" and "helped with" are both in the resume
        assertTrue(findings.stream().anyMatch(f -> f.contains("responsible for")),
                "Finding must reference the 'responsible for' weak phrase");
        assertTrue(findings.stream().anyMatch(f -> f.contains("helped with")),
                "Finding must reference the 'helped with' weak phrase");
    }

    @Test
    void should_includeSuggestedReplacement_inEachWeakVerbFinding() {
        List<String> findings = engine.detectWeakVerbs(TestData.SUGGESTION_RESUME);
        findings.forEach(f -> assertTrue(f.contains("→"),
                "Each weak-verb finding must include a replacement suggestion separated by '→'"));
    }

    @Test
    void should_returnEmptyWeakVerbList_whenResumeHasOnlyActionVerbs() {
        String strongResume = "EXPERIENCE\nLed the team to deliver a 40% improvement.\n" +
                "Built and deployed kubernetes pipelines.\nSKILLS\njava python";
        List<String> findings = engine.detectWeakVerbs(strongResume);
        assertTrue(findings.isEmpty(),
                "Resume with only strong action verbs must produce an empty weak-verb list");
    }

    // Quantification detection

    @Test
    void should_flagBulletLinesWithoutNumericMetrics() {
        List<String> unquantified = engine.checkQuantification(TestData.BULLETS_WITH_MIXED_METRICS);

        assertFalse(unquantified.isEmpty(), "At least one unquantified bullet must be found");
        assertTrue(unquantified.stream().anyMatch(b -> b.contains("managed the team")),
                "Bullet without number must be in unquantified list");
        assertTrue(unquantified.stream().anyMatch(b -> b.contains("built the product")),
                "Bullet without number must be in unquantified list");
    }

    @Test
    void should_notFlagBulletLines_whenNumericMetricIsPresent() {
        List<String> unquantified = engine.checkQuantification(TestData.BULLETS_WITH_MIXED_METRICS);
        assertFalse(unquantified.stream().anyMatch(b -> b.contains("40%")),
                "Bullet containing '40%' must not be in the unquantified list");
    }

    @Test
    void should_returnEmptyUnquantifiedList_whenNoBulletPointsPresent() {
        String noBullets = "Software engineer with java and python experience.";
        List<String> unquantified = engine.checkQuantification(noBullets);
        assertTrue(unquantified.isEmpty(),
                "Text with no bullet points must produce an empty unquantified list");
    }

    // generateTopSuggestions — priority and content

    @Test
    void should_generateHighPrioritySuggestion_whenMissingKeywordsPresent() {
        List<String> missing = List.of("kubernetes", "docker", "react");
        List<SuggestionResponse> suggestions = engine.generateTopSuggestions(missing, List.of(), List.of(), 70.0);

        assertTrue(suggestions.stream().anyMatch(s -> "HIGH".equals(s.priority())),
                "Missing keywords must produce at least one HIGH priority suggestion");
    }

    @Test
    void should_generateLowPrioritySuggestion_whenOverallScoreIsBelow55() {
        List<SuggestionResponse> suggestions = engine.generateTopSuggestions(List.of(), List.of(), List.of(), 40.0);

        assertTrue(suggestions.stream().anyMatch(s -> "LOW".equals(s.priority())),
                "Overall score < 55 must produce a LOW priority structure suggestion");
    }

    @Test
    void should_returnNoSuggestions_whenResumeHasNoIssues() {
        // No missing keywords, no weak verbs, no unquantified bullets, high score
        List<SuggestionResponse> suggestions = engine.generateTopSuggestions(List.of(), List.of(), List.of(), 90.0);
        assertTrue(suggestions.isEmpty(),
                "Perfect resume with no issues must produce zero suggestions");
    }
}
