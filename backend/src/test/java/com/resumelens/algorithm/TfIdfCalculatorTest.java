package com.resumelens.algorithm;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TfIdfCalculator.
 * No Spring context needed - pure algorithm tests.
 */
class TfIdfCalculatorTest {

    private final TfIdfCalculator calc = new TfIdfCalculator();

    /**
     * Verifies the core IDF-discriminativeness property of topKeywords:
     * "alpha" appears once in every line -> high TF, low IDF (ubiquitous across corpus).
     * "beta"  appears 3 times but only in one line -> moderate TF, high IDF (distinctive).
     *
     * With sentence-level IDF corpus (N = 5 lines):
     *   IDF("alpha") = log(6/6) + 1 = 1.0   (in all 5 lines)
     *   IDF("beta")  = log(6/2) + 1 ≈ 2.10  (in 1 line)
     *
     * TF over full text (25 tokens):
     *   TF("alpha") = 5/25 = 0.20  -> TF-IDF ≈ 0.20
     *   TF("beta")  = 3/25 = 0.12  -> TF-IDF ≈ 0.25
     *
     * Expected: "beta" ranks above "alpha".
     */
    @Test
    void topKeywords_distinctiveTermRanksAboveUbiquitousTerm() {
        // 5 lines; "alpha" appears in every line (ubiquitous), "beta" appears 3x in line 1 only
        String text = String.join("\n",
            "alpha beta beta beta foo",
            "alpha baz qux quux corge",
            "alpha grault garply waldo fred",
            "alpha plugh thud grunt blort",
            "alpha zap zing zoom zork"
        );

        List<String> keywords = calc.topKeywords(text, 10);

        assertTrue(keywords.contains("beta"),  "beta must appear in top keywords");
        assertTrue(keywords.contains("alpha"), "alpha must appear in top keywords");

        int betaRank  = keywords.indexOf("beta");
        int alphaRank = keywords.indexOf("alpha");

        assertTrue(betaRank < alphaRank,
            "Distinctive term 'beta' (rare across lines) must rank above ubiquitous 'alpha'. " +
            "Actual order: " + keywords);
    }

    /**
     * Verifies that JD filler terms are excluded from the results even when
     * they appear frequently in the text.
     */
    @Test
    void topKeywords_fillerTermsAreExcluded() {
        // "experience", "team", "skills" are in the JD_FILLER list
        String text = String.join("\n",
            "experience experience experience team skills java",
            "experience team skills python developer",
            "experience team skills kubernetes docker",
            "experience required team skills react",
            "experience years team skills spring"
        );

        List<String> keywords = calc.topKeywords(text, 30);

        assertFalse(keywords.contains("experience"), "'experience' is a filler term and must be excluded");
        assertFalse(keywords.contains("team"),       "'team' is a filler term and must be excluded");
        assertFalse(keywords.contains("skills"),     "'skills' is a filler term and must be excluded");

        // Technical terms that are NOT in the filler list must survive
        assertTrue(keywords.contains("java")       || keywords.contains("python") ||
                   keywords.contains("kubernetes") || keywords.contains("docker"),
            "At least one non-filler technical term must be present");
    }

    /**
     * Verifies that topKeywords returns no more than topN results.
     */
    @Test
    void topKeywords_respectsTopNLimit() {
        String text = "java python react spring docker kubernetes mysql postgresql redis kafka";
        List<String> keywords = calc.topKeywords(text, 3);
        assertTrue(keywords.size() <= 3, "topKeywords must return at most topN results");
    }

    /**
     * Verifies that topKeywords on blank input returns an empty list without throwing.
     */
    @Test
    void topKeywords_emptyInputReturnsEmptyList() {
        List<String> keywords = calc.topKeywords("   ", 10);
        assertNotNull(keywords);
        assertTrue(keywords.isEmpty(), "Empty input should produce an empty keyword list");
    }

    /**
     * Verifies that a single-paragraph input (no newlines) is still split into
     * multiple IDF documents via semicolons, so IDF discrimination is preserved.
     *
     * "alpha" appears in every semicolon-delimited clause -> low IDF.
     * "beta"  appears 3 times but only in the first clause -> high IDF.
     *
     * Without semicolon splitting the whole paragraph is one document and every
     * term gets the same IDF = 1.0, collapsing ranking to TF-only (alpha wins).
     * With splitting, beta's higher IDF pushes its TF-IDF above alpha's.
     */
    @Test
    void topKeywords_singleParagraphWithSemicolons_discriminatesRareTerms() {
        // No newlines; only semicolons separate clauses
        String singleParagraph =
            "alpha beta beta beta foo; alpha baz qux quux; alpha grault garply waldo; " +
            "alpha plugh thud grunt; alpha zap zing zoom";

        List<String> keywords = calc.topKeywords(singleParagraph, 10);

        assertTrue(keywords.contains("beta"),  "beta must appear in top keywords");
        assertTrue(keywords.contains("alpha"), "alpha must appear in top keywords");

        int betaRank  = keywords.indexOf("beta");
        int alphaRank = keywords.indexOf("alpha");

        assertTrue(betaRank < alphaRank,
            "Distinctive 'beta' (appears in 1 clause) must rank above ubiquitous 'alpha' " +
            "(appears in all 5 clauses) via semicolon-based IDF corpus. Actual: " + keywords);
    }

    @Test
    void topKeywords_keywordNoiseTermsAndGenericPhraseAreExcluded() {
        String jd = "Looking for a software engineer or engineers with good engineering practices and practice. " +
                "Must evaluate modern techniques and technique. Requires a degree: bachelor, master, or phd in a technical field. " +
                "Degree in computer science or science preferred. Experience with apache projects.";

        List<String> keywords = calc.topKeywords(jd, 30);

        List<String> noiseTerms = List.of(
            "engineer", "engineers", "practices", "practice", "evaluate",
            "techniques", "technique", "degree", "bachelor", "master", "phd",
            "field", "science", "apache", "computer science"
        );

        for (String noise : noiseTerms) {
            assertFalse(keywords.contains(noise), "Noise term '" + noise + "' must never become a keyword");
        }
    }

    @Test
    void topKeywords_multiwordSkillsPreservedAsSingleKeywords() {
        String jd = "Seeking experience with data science, apache spark, and apache kafka streaming pipelines.";
        List<String> keywords = calc.topKeywords(jd, 30);

        assertTrue(keywords.contains("data science"), "data science must be preserved as one keyword");
        assertTrue(keywords.contains("apache spark"), "apache spark must be preserved as one keyword");
        assertTrue(keywords.contains("apache kafka"), "apache kafka must be preserved as one keyword");

        assertFalse(keywords.contains("apache"), "component word 'apache' must not be added as a separate keyword");
        assertFalse(keywords.contains("science"), "component word 'science' must not be added as a separate keyword");
    }

    @Test
    void testC_keywordFillExcludesCommonEnglishWordsAndRetainsTechSkills() {
        String jd = String.join("\n",
            "Build user interfaces with ReactJS and NextJS",
            "Develop backend services using Node JS and Express JS",
            "Store data in Mongo DB",
            "Write clean code in Typescript",
            "Style applications with Tailwind and use Github Actions for deployment"
        );

        List<String> keywords = calc.topKeywords(jd, 30);

        List<String> expectedSkills = List.of(
            "reactjs", "nextjs", "node js", "express", "mongo db", "typescript", "tailwind", "github actions"
        );
        for (String skill : expectedSkills) {
            assertTrue(keywords.contains(skill), "Keywords must contain: " + skill);
        }

        List<String> forbidden = List.of(
            "use", "user", "store", "clean", "style", "services", "interfaces", "backend"
        );
        for (String word : forbidden) {
            assertFalse(keywords.contains(word), "Keywords must not contain common word: " + word);
        }
    }

    @Test
    void testD_missingKeywordsListIsEmptyForMatchingResume() {
        String jd = String.join("\n",
            "Build user interfaces with ReactJS and NextJS",
            "Develop backend services using Node JS and Express JS",
            "Store data in Mongo DB",
            "Write clean code in Typescript",
            "Style applications with Tailwind and use Github Actions for deployment"
        );
        String resume = "React.js, Next.js, Node.js, Express.js, MongoDB, TypeScript, Tailwind CSS and GitHub Actions";

        SimilarityCalculator sim = new SimilarityCalculator();
        List<String> keywords = calc.topKeywords(jd, 30);

        List<String> missing = new ArrayList<>();
        for (String kw : keywords) {
            if (!sim.isKeywordMatched(kw, resume)) {
                missing.add(kw);
            }
        }
        assertTrue(missing.isEmpty(), "Missing list must be empty, but contained: " + missing);
    }

    @Test
    void testE_fullStackJobDescriptionHasNoneOfThroughStackSprint() {
        String jd = "Looking for a developer with experience working through full stack development across sprint cycles.";
        List<String> keywords = calc.topKeywords(jd, 30);

        assertFalse(keywords.contains("through"), "Keywords must not contain 'through'");
        assertFalse(keywords.contains("stack"), "Keywords must not contain 'stack'");
        assertFalse(keywords.contains("sprint"), "Keywords must not contain 'sprint'");
    }

    @Test
    void testCommonEnglishWordsSetSize() {
        assertTrue(TfIdfCalculator.COMMON_ENGLISH_WORDS.size() >= 300,
            "Common English words set must have at least 300 words");
    }
}
