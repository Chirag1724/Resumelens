package com.resumelens.service;

import com.resumelens.algorithm.SimilarityCalculator;
import com.resumelens.algorithm.TfIdfCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Tests for keyword extraction, canonical matching, job fit score cap,
 * and single source of truth per SCORING_RULES.md.
 * Expected values are computed by hand as literals.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class KeywordMatchingAndCapTest {

    private final TfIdfCalculator tfidf = new TfIdfCalculator();
    private final SimilarityCalculator similarity = new SimilarityCalculator();

    @Mock
    private ResumeParserService resumeParser;

    private ATSScoreEngine engine;
    private SuggestionEngine suggestionEngine;

    @BeforeEach
    void setUp() {
        engine = new ATSScoreEngine(tfidf, similarity, resumeParser);
        suggestionEngine = new SuggestionEngine(similarity, tfidf);
        when(resumeParser.hasContactInfo(anyString())).thenReturn(true);
        when(resumeParser.extractSections(anyString())).thenAnswer(inv -> new ResumeParserService().extractSections(inv.getArgument(0)));
    }

    /**
     * (a) A fixture resume containing modern, leading, created, smart, track, database, mysql,
     * deployment, building against a JD containing models, learning, related, spark, train,
     * data, deploy, build must match none of those keywords.
     */
    @Test
    void testA_looseKeywordsMatchNone() {
        String resume = "EDUCATION\nBachelor of Science\nEXPERIENCE\n" +
                "modern leading created smart track database mysql deployment building\n" +
                "SKILLS\nmodern leading created smart track database mysql deployment building";

        List<String> testKeywords = List.of(
                "models", "learning", "related", "spark", "train", "data", "deploy", "build"
        );

        for (String kw : testKeywords) {
            boolean matched = similarity.isKeywordMatched(kw, resume);
            assertFalse(matched, "Keyword '" + kw + "' must not match unrelated resume words");
        }
    }

    /**
     * (b) A JD containing ReactJS, NextJS, Node JS, Mongo DB, Typescript and Github Actions
     * against a resume containing React, Next.js, Node.js, MongoDB, TypeScript and GitHub Actions
     * must match all six.
     */
    @Test
    void testB_canonicalFormsMatchAllSix() {
        String jd = "ReactJS, NextJS, Node JS, Mongo DB, Typescript and Github Actions";
        String resume = "EDUCATION\nBachelor of Science\nEXPERIENCE\n" +
                "Developed full-stack web applications.\n" +
                "SKILLS\nReact, Next.js, Node.js, MongoDB, TypeScript and GitHub Actions";

        List<String> keywords = tfidf.topKeywords(jd, 30);
        assertTrue(keywords.contains("reactjs"), "JD keywords must contain reactjs");
        assertTrue(keywords.contains("nextjs"), "JD keywords must contain nextjs");
        assertTrue(keywords.contains("node js"), "JD keywords must contain node js");
        assertTrue(keywords.contains("mongo db"), "JD keywords must contain mongo db");
        assertTrue(keywords.contains("typescript"), "JD keywords must contain typescript");
        assertTrue(keywords.contains("github actions"), "JD keywords must contain github actions");

        for (String kw : keywords) {
            assertTrue(similarity.isKeywordMatched(kw, resume),
                    "Keyword '" + kw + "' must match resume via canonical equality");
        }
    }

    /**
     * (c) A machine learning JD against a web developer fixture resume must give
     * Skills Validation below 40 and an overall score of at most 54 with scoreCapped true.
     */
    @Test
    void testC_machineLearningJdAgainstWebDevResume_cappedAt54() {
        String mlJd = "Machine Learning Engineer with deep learning, PyTorch, TensorFlow, " +
                "computer vision, NLP, linear algebra, statistics, and MLOps experience.";

        String webDevResume = "EDUCATION\nBachelor of Science in Computer Science\n" +
                "EXPERIENCE\n" +
                "- Built responsive user interfaces and REST APIs achieving 40% performance gain\n" +
                "- Managed MySQL database and deployment pipelines\n" +
                "SKILLS\n" +
                "React, Next.js, Node.js, Express, JavaScript, TypeScript, HTML, CSS, Tailwind, MySQL, Git";

        ATSScoreEngine.ScoreResult r = engine.score(webDevResume, mlJd, false, false);

        assertTrue(r.skillsScore() < 40.0,
                "Skills Validation score must be below 40.0, but was " + r.skillsScore());
        assertTrue(r.overallScore() <= 54.0,
                "Overall score must be capped at 54.0, but was " + r.overallScore());
        assertEquals(54.0, r.overallScore(), 0.01,
                "Overall score should equal the 54.0 cap literal");
        assertTrue(r.scoreCapped(),
                "scoreCapped must be true when skills score is below 40");
    }

    /**
     * (d) Cap boundaries with direct engine inputs:
     * skills 39.99 gives cap 54, 40 gives cap 69, 59.99 gives cap 69, 60 gives no cap.
     */
    @Test
    void testD_capBoundariesDirectEngineInputs() {
        double uncappedScore = 80.00;

        // Boundary 1: skills 39.99 -> cap 54.0
        ATSScoreEngine.CapResult r1 = ATSScoreEngine.applyScoreCap(uncappedScore, 39.99);
        assertEquals(54.0, r1.cappedScore(), 1e-9);
        assertTrue(r1.scoreCapped());

        // Boundary 2: skills 40.00 -> cap 69.0
        ATSScoreEngine.CapResult r2 = ATSScoreEngine.applyScoreCap(uncappedScore, 40.00);
        assertEquals(69.0, r2.cappedScore(), 1e-9);
        assertTrue(r2.scoreCapped());

        // Boundary 3: skills 59.99 -> cap 69.0
        ATSScoreEngine.CapResult r3 = ATSScoreEngine.applyScoreCap(uncappedScore, 59.99);
        assertEquals(69.0, r3.cappedScore(), 1e-9);
        assertTrue(r3.scoreCapped());

        // Boundary 4: skills 60.00 -> no cap (retains uncapped 80.00)
        ATSScoreEngine.CapResult r4 = ATSScoreEngine.applyScoreCap(uncappedScore, 60.00);
        assertEquals(80.00, r4.cappedScore(), 1e-9);
        assertFalse(r4.scoreCapped());
    }

    /**
     * (e) Deep learning stays one keyword.
     */
    @Test
    void testE_deepLearningStaysOneKeyword() {
        String jd = "We are seeking an engineer experienced in deep learning systems.";
        List<String> keywords = tfidf.topKeywords(jd, 30);

        assertTrue(keywords.contains("deep learning"), "Keywords must contain 'deep learning'");
        assertFalse(keywords.contains("deep"), "Keywords must not contain split 'deep'");
        assertFalse(keywords.contains("learning"), "Keywords must not contain split 'learning'");
    }

    /**
     * (f) Generic words never appear as keywords.
     */
    @Test
    void testF_genericWordsNeverAppearAsKeywords() {
        String jd = "Responsibilities include requirements gathering. Hiring manager looking to join team. " +
                "Candidate with strong experience and ability. Large scale using related work.";

        List<String> keywords = tfidf.topKeywords(jd, 30);

        String[] generic = {
                "responsibilities", "requirements", "requirement", "hiring", "apply", "large",
                "using", "related", "strong", "experience", "team", "work", "ability",
                "knowledge", "looking", "join", "role", "candidate", "years", "production"
        };

        for (String word : generic) {
            assertFalse(keywords.contains(word),
                    "Generic word '" + word + "' must never appear as a keyword");
        }
    }

    /**
     * (h) The suggestion's missing keywords equal the Skills Validation missing list.
     */
    @Test
    void testH_suggestionMissingKeywordsEqualSkillsValidationMissingList() {
        String jd = "We require java, docker, kubernetes, aws, and redis.";
        String resume = "EDUCATION\nBachelor of Science\nEXPERIENCE\nJava developer with redis\nSKILLS\njava redis";

        ATSScoreEngine.ScoreResult scoreResult = engine.score(resume, jd, false, false);
        List<String> suggestionMissing = suggestionEngine.findMissingKeywords(resume, jd);

        assertEquals(scoreResult.missingKeywords(), suggestionMissing,
                "Suggestion missing keywords must equal Skills Validation missing keywords list");
    }
}
