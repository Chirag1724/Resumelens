package com.resumelens.service;

import com.resumelens.TestData;
import com.resumelens.algorithm.SimilarityCalculator;
import com.resumelens.algorithm.TfIdfCalculator;
import com.resumelens.entity.SectionScore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ATSScoreEngine — one test per rule from docs/SCORING_RULES.md.
 *
 * Real TfIdfCalculator and SimilarityCalculator are used (pure algorithms).
 * ResumeParserService is mocked (has I/O dependencies).
 *
 * Default mock behaviour per @BeforeEach:
 * hasContactInfo → true (no contact deduction)
 * extractSections → Map.of() (no projects section → projectScore = 50 neutral)
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ATSScoreEngineTest {

    private final TfIdfCalculator tfidf = new TfIdfCalculator();
    private final SimilarityCalculator similarity = new SimilarityCalculator();

    @Mock
    private ResumeParserService resumeParser;

    private ATSScoreEngine engine;

    @BeforeEach
    void setUp() {
        engine = new ATSScoreEngine(tfidf, similarity, resumeParser);
        when(resumeParser.hasContactInfo(anyString())).thenReturn(true);
        when(resumeParser.extractSections(anyString())).thenAnswer(inv -> new ResumeParserService().extractSections(inv.getArgument(0)));
    }

    // ATS COMPATIBILITY — section headings

    @Test
    void should_deduct8PerMissingSection_when2StandardSectionsMissing() {
        // Resume has only SKILLS; EDUCATION and EXPERIENCE are absent → –16
        String resume = "SKILLS\njava python achieved 40% performance improvement\n" +
                "java python developer role";
        ATSScoreEngine.ScoreResult r = engine.score(resume, TestData.JD_TWO_TERMS, false, false);
        assertEquals(84.0, r.atsScore(), 0.01,
                "2 missing standard sections × –8 = –16 → atsScore should be 84");
    }

    @Test
    void should_capMissingSectionDeductionAt24_when3StandardSectionsMissing() {
        // No EDUCATION, EXPERIENCE, or SKILLS headings → max deduction –24
        String resume = "java python engineer achieved 40% improvement in productivity pipeline";
        ATSScoreEngine.ScoreResult r = engine.score(resume, TestData.JD_TWO_TERMS, false, false);
        assertEquals(76.0, r.atsScore(), 0.01,
                "3 missing sections capped at –24 → atsScore should be 76");
    }

    // ATS COMPATIBILITY — contact info

    @Test
    void should_deduct10_whenContactInfoIsAbsent() {
        when(resumeParser.hasContactInfo(anyString())).thenReturn(false);
        ATSScoreEngine.ScoreResult r = engine.score(TestData.GOOD_RESUME, TestData.JD_TWO_TERMS, false, false);
        assertEquals(90.0, r.atsScore(), 0.01,
                "Contact info absent → –10 → atsScore should be 90");
    }

    // ATS COMPATIBILITY — layout risks

    @Test
    void should_deduct10_whenMultiColumnFormattingIsDetected() {
        ATSScoreEngine.ScoreResult r = engine.score(TestData.GOOD_RESUME, TestData.JD_TWO_TERMS, true, false);
        assertEquals(90.0, r.atsScore(), 0.01,
                "Multi-column formatting risk → –10 → atsScore should be 90");
    }

    @Test
    void should_deduct8_whenGraphicsOrNonStandardFontIsDetected() {
        ATSScoreEngine.ScoreResult r = engine.score(TestData.GOOD_RESUME, TestData.JD_TWO_TERMS, false, true);
        assertEquals(92.0, r.atsScore(), 0.01,
                "Graphics/non-standard font risk → –8 → atsScore should be 92");
    }

    // ATS COMPATIBILITY — keyword density

    @Test
    void should_deduct15_whenKeywordDensityIsBelow30Percent() {
        // JD keywords: rust, golang, swift — none present in GOOD_RESUME
        ATSScoreEngine.ScoreResult r = engine.score(TestData.GOOD_RESUME, TestData.JD_FOREIGN_TERMS, false, false);
        assertEquals(85.0, r.atsScore(), 0.01,
                "0% keyword density < 30% threshold → –15 → atsScore should be 85");
    }

    @Test
    void should_deduct8_whenKeywordDensityIsBetween30And50Percent() {
        // JD_THREE_TERMS = "java rust golang". Resume has "java" (1/3 = 33%) → –8
        ATSScoreEngine.ScoreResult r = engine.score(TestData.GOOD_RESUME, TestData.JD_THREE_TERMS, false, false);
        assertEquals(92.0, r.atsScore(), 0.01,
                "1 of 3 JD keywords matched = 33% in [30%,50%) → –8 → atsScore should be 92");
    }

    // ATS COMPATIBILITY — measurable achievements

    @Test
    void should_deduct5_whenNoMeasurableAchievementsDetected() {
        // Good structure but no numeric pattern (no %, $, x, million, etc.)
        String resume = "EDUCATION\nBachelor of Science\n" +
                "EXPERIENCE\nSoftware Engineer using java and python tools\n" +
                "SKILLS\njava python programming";
        ATSScoreEngine.ScoreResult r = engine.score(resume, TestData.JD_TWO_TERMS, false, false);
        assertEquals(95.0, r.atsScore(), 0.01,
                "No measurable achievement pattern found → –5 → atsScore should be 95");
    }

    // IMPACT & ACHIEVEMENTS — percentage-to-score mapping

    @Test
    void should_return100ImpactScore_whenAllBulletsHaveActionVerbsOrMetrics() {
        // 2 bullets, both with action verbs and/or numbers → 100% → score 100
        String resume = "EDUCATION\nEXPERIENCE\n" +
                "- led team to deliver 40% reduction in deploy time\n" +
                "- built new java pipeline saving $5000\n" +
                "SKILLS\njava python";
        ATSScoreEngine.ScoreResult r = engine.score(resume, TestData.JD_TWO_TERMS, false, false);
        assertEquals(100.0, r.impactScore(), 0.01,
                "All bullets have impact signal → 100% ratio → impactScore 100");
    }

    @Test
    void should_return80ImpactScore_whenImpactBulletRatioIsBetween50And69() {
        // 2 bullets, 1 with action verb → 50% → score 80
        String resume = "EDUCATION\nEXPERIENCE\n" +
                "- led the project to completion\n" +
                "- worked on deployment\n" +
                "SKILLS\njava";
        ATSScoreEngine.ScoreResult r = engine.score(resume, TestData.JD_TWO_TERMS, false, false);
        assertEquals(80.0, r.impactScore(), 0.01,
                "1/2 impact bullets = 50% → impactScore 80");
    }

    @Test
    void should_return60ImpactScore_whenImpactBulletRatioIsBetween30And49() {
        // 3 bullets, 1 with impact → 33% → score 60
        String resume = "EDUCATION\nEXPERIENCE\n" +
                "- led the team\n" +
                "- worked on project\n" +
                "- assisted in testing\n" +
                "SKILLS\njava";
        ATSScoreEngine.ScoreResult r = engine.score(resume, TestData.JD_TWO_TERMS, false, false);
        assertEquals(60.0, r.impactScore(), 0.01,
                "1/3 impact bullets = 33% → impactScore 60");
    }

    @Test
    void should_return40ImpactScore_whenImpactBulletRatioIsBetween10And29() {
        // 4 bullets, 1 with impact → 25% → score 40
        String resume = "EDUCATION\nEXPERIENCE\n" +
                "- led the project delivery\n" +
                "- worked on tasks\n" +
                "- participated in planning\n" +
                "- involved in review\n" +
                "SKILLS\njava";
        ATSScoreEngine.ScoreResult r = engine.score(resume, TestData.JD_TWO_TERMS, false, false);
        assertEquals(40.0, r.impactScore(), 0.01,
                "1/4 impact bullets = 25% → impactScore 40");
    }

    @Test
    void should_return20ImpactScore_whenImpactBulletRatioIsBetween1And9() {
        // 20 bullets, 1 with impact → 5% → score 20
        StringBuilder resume = new StringBuilder("EDUCATION\nEXPERIENCE\n- led the team to deliver project\n");
        for (int i = 0; i < 19; i++)
            resume.append("- worked on stuff\n");
        resume.append("SKILLS\njava");
        ATSScoreEngine.ScoreResult r = engine.score(resume.toString(), TestData.JD_TWO_TERMS, false, false);
        assertEquals(20.0, r.impactScore(), 0.01,
                "1/20 impact bullets = 5% → impactScore 20");
    }

    @Test
    void should_return0ImpactScore_whenNoBulletPointsPresent() {
        // No bullet lines at all
        String resume = "EDUCATION\nEXPERIENCE\nSoftware engineer doing things\nSKILLS\njava";
        ATSScoreEngine.ScoreResult r = engine.score(resume, TestData.JD_TWO_TERMS, false, false);
        assertEquals(0.0, r.impactScore(), 0.01,
                "No bullet points → impactScore 0");
    }

    // PROJECT QUALITY

    @Test
    void should_returnNeutral50ProjectScore_whenNoProjectSectionDetected() {
        // extractSections returns empty Map (default mock) → no PROJECT key → 50
        // neutral
        ATSScoreEngine.ScoreResult r = engine.score(TestData.GOOD_RESUME, TestData.JD_TWO_TERMS, false, false);
        assertEquals(50.0, r.projectScore(), 0.01,
                "No project section in resume → neutral 50 project score");
    }

    // WRITING QUALITY

    @Test
    void should_deduct10FromWritingScore_whenAverageSentenceExceeds35Words() {
        // 40 unique tokens, no sentence boundary → one sentence with 40+ words
        String resume = "EDUCATION\nEXPERIENCE\nSKILLS\n" + TestData.uniqueTokens(40);
        ATSScoreEngine.ScoreResult r = engine.score(resume, TestData.JD_TWO_TERMS, false, false);
        assertEquals(90.0, r.writingScore(), 0.01,
                "Single sentence with >35 words → –10 → writingScore 90");
    }

    @Test
    void should_deduct15FromWritingScore_whenFirstPersonPronounsPresent() {
        String resume = "EDUCATION\nBachelor degree\nEXPERIENCE\nI led the java team\nSKILLS\njava";
        ATSScoreEngine.ScoreResult r = engine.score(resume, TestData.JD_TWO_TERMS, false, false);
        assertEquals(85.0, r.writingScore(), 0.01,
                "First-person pronoun 'I' found → –15 → writingScore 85");
    }

    @Test
    void should_deductUpTo15FromWritingScore_forFillerWordsAt3PerOccurrence() {
        // 5 filler phrases → 5×3 = 15, capped at 15
        String resume = "EDUCATION\nEXPERIENCE\n" +
                "responsible for coding, helped with testing, involved in design, " +
                "worked on deployment, assisted in planning.\n" +
                "SKILLS\njava";
        ATSScoreEngine.ScoreResult r = engine.score(resume, TestData.JD_TWO_TERMS, false, false);
        assertEquals(85.0, r.writingScore(), 0.01,
                "5 filler phrases × 3 = 15, capped → –15 → writingScore 85");
    }

    @Test
    void should_deductUpTo15FromWritingScore_forWordRepetitionAt5PerWord() {
        // 3 distinct plain non-skill, non-generic words each repeated >5 times (6 times each) -> 3 * 5 = 15, capped at 15
        String resume = "EDUCATION\nEXPERIENCE\nSKILLS\n" +
                "managed managed managed managed managed managed " + // 6 -> 1 over-repeated word
                "custom custom custom custom custom custom " + // 6 -> 2
                "simple simple simple simple simple simple"; // 6 -> 3 -> total 15, capped
        ATSScoreEngine.ScoreResult r = engine.score(resume, TestData.JD_TWO_TERMS, false, false);
        assertEquals(85.0, r.writingScore(), 0.01,
                "3 over-repeated words * 5 = 15, capped -> -15 -> writingScore 85");
    }

    @Test
    void should_deduct10FromWritingScore_whenPassiveVoiceExceeds40Percent() {
        // 3 passive sentences out of 4 = 75% > 40%
        String resume = "EDUCATION\nEXPERIENCE\nSKILLS. " +
                "The project was managed by the team. " +
                "Tasks were implemented carefully. " +
                "Work was delivered on time";
        ATSScoreEngine.ScoreResult r = engine.score(resume, TestData.JD_TWO_TERMS, false, false);
        assertEquals(90.0, r.writingScore(), 0.01,
                "75% passive voice > 40% threshold → –10 → writingScore 90");
    }

    // PROFESSIONALISM

    @Test
    void should_deduct5PerSpellingError_cappedAt20() {
        // "recieve" + "managment" = 2 errors → 2×5 = 10
        String resume = "EDUCATION\nEXPERIENCE\n" +
                "I recieve great managment skills in java projects\nSKILLS\njava";
        ATSScoreEngine.ScoreResult r = engine.score(resume, TestData.JD_TWO_TERMS, false, false);
        // Also has first-person 'I' → writingScore deduction, but professionalismScore
        // tested separately
        assertEquals(90.0, r.professionalismScore(), 0.01,
                "2 spelling errors × 5 = 10 → professionalismScore 90");
    }

    @Test
    void should_deduct10_whenDateFormattingIsInconsistent() {
        // Mix of alphabetic (Jan 2020) and numeric (01/2023) date formats
        String resume = "EDUCATION\nJan 2020 to 01/2023\nEXPERIENCE\nEngineer java 40%\nSKILLS\njava";
        ATSScoreEngine.ScoreResult r = engine.score(resume, TestData.JD_TWO_TERMS, false, false);
        assertEquals(90.0, r.professionalismScore(), 0.01,
                "Mixed date formats (Jan 2020 and 01/2023) → –10 → professionalismScore 90");
    }

    @Test
    void should_deduct5_whenBulletStyleIsInconsistent() {
        // Mix of dash (>= 2 lines) and bullet styles
        String resume = "EDUCATION\nEXPERIENCE\n" +
                "- java developer\n" +
                "- backend engineer\n" +
                "• python expert\n" +
                "• cloud architect\n" +
                "SKILLS\njava python 40%";
        ATSScoreEngine.ScoreResult r = engine.score(resume, TestData.JD_TWO_TERMS, false, false);
        assertEquals(95.0, r.professionalismScore(), 0.01,
                "Mixed bullet styles (- and •) -> -5 -> professionalismScore 95");
    }

    @Test
    void should_deduct10_whenEmailAddressIsCasual() {
        // "cool420" triggers the CASUAL_EMAIL pattern
        String resume = "EDUCATION\nEXPERIENCE\nSKILLS\n" +
                "cool420@gmail.com java python developer achieved 40% improvement";
        ATSScoreEngine.ScoreResult r = engine.score(resume, TestData.JD_TWO_TERMS, false, false);
        assertEquals(90.0, r.professionalismScore(), 0.01,
                "Casual email address (cool420@gmail.com) → –10 → professionalismScore 90");
    }

    @Test
    void should_deduct5_whenResumeLengthExceeds700Words() {
        // Build a resume with >700 words: 3 section labels + 701 unique tokens
        String resume = "EDUCATION\nEXPERIENCE\nSKILLS java python achieved 40% improvement\n" +
                TestData.uniqueTokens(701);
        ATSScoreEngine.ScoreResult r = engine.score(resume, TestData.JD_TWO_TERMS, false, false);
        assertEquals(95.0, r.professionalismScore(), 0.01,
                "Word count > 700 → –5 → professionalismScore 95");
    }

    // STATUS LABEL — boundary tests (SCORING_RULES.md thresholds)

    @Test
    void should_returnExcellentMatch_atExactly85() {
        assertEquals("Excellent Match", ATSScoreEngine.statusLabel(85.0),
                "Score exactly 85 should be 'Excellent Match'");
    }

    @Test
    void should_returnStrongMatch_onePointBelow85() {
        assertEquals("Strong Match", ATSScoreEngine.statusLabel(84.99),
                "Score 84.99 should be 'Strong Match'");
    }

    @Test
    void should_returnStrongMatch_atExactly70() {
        assertEquals("Strong Match", ATSScoreEngine.statusLabel(70.0),
                "Score exactly 70 should be 'Strong Match'");
    }

    @Test
    void should_returnModerateMatch_onePointBelow70() {
        assertEquals("Moderate Match", ATSScoreEngine.statusLabel(69.99),
                "Score 69.99 should be 'Moderate Match'");
    }

    @Test
    void should_returnModerateMatch_atExactly55() {
        assertEquals("Moderate Match", ATSScoreEngine.statusLabel(55.0),
                "Score exactly 55 should be 'Moderate Match'");
    }

    @Test
    void should_returnWeakMatch_onePointBelow55() {
        assertEquals("Weak Match", ATSScoreEngine.statusLabel(54.99),
                "Score 54.99 should be 'Weak Match'");
    }

    @Test
    void should_returnWeakMatch_atExactly40() {
        assertEquals("Weak Match", ATSScoreEngine.statusLabel(40.0),
                "Score exactly 40 should be 'Weak Match'");
    }

    @Test
    void should_returnPoorMatch_onePointBelow40() {
        assertEquals("Poor Match", ATSScoreEngine.statusLabel(39.99),
                "Score 39.99 should be 'Poor Match'");
    }

    // SECTION STATUS - shared threshold tests (85, 70, 55, 40)

    @Test
    void should_returnExcellentSectionStatus_atExactly85() {
        assertEquals(SectionScore.EXCELLENT, SectionScore.deriveStatus(85.0));
    }

    @Test
    void should_returnStrongSectionStatus_onePointBelow85() {
        assertEquals(SectionScore.STRONG, SectionScore.deriveStatus(84.99));
    }

    @Test
    void should_returnStrongSectionStatus_atExactly70() {
        assertEquals(SectionScore.STRONG, SectionScore.deriveStatus(70.0));
    }

    @Test
    void should_returnModerateSectionStatus_onePointBelow70() {
        assertEquals(SectionScore.MODERATE, SectionScore.deriveStatus(69.99));
    }

    @Test
    void should_returnModerateSectionStatus_atExactly55() {
        assertEquals(SectionScore.MODERATE, SectionScore.deriveStatus(55.0));
    }

    @Test
    void should_returnWeakSectionStatus_onePointBelow55() {
        assertEquals(SectionScore.WEAK, SectionScore.deriveStatus(54.99));
    }

    @Test
    void should_returnWeakSectionStatus_atExactly40() {
        assertEquals(SectionScore.WEAK, SectionScore.deriveStatus(40.0));
    }

    @Test
    void should_returnPoorSectionStatus_onePointBelow40() {
        assertEquals(SectionScore.POOR, SectionScore.deriveStatus(39.99));
    }

    // WEIGHTED SCORE — formula and rounding

    @Test
    void should_computeOverall_asWeightedAverageOf6Sections_roundedTo2DecimalPlaces() {
        ATSScoreEngine.ScoreResult r = engine.score(TestData.GOOD_RESUME, TestData.JD_TWO_TERMS, false, false);

        double expected = Math.round(
                (r.atsScore() * ATSScoreEngine.WEIGHT_ATS +
                        r.skillsScore() * ATSScoreEngine.WEIGHT_SKILLS +
                        r.impactScore() * ATSScoreEngine.WEIGHT_IMPACT +
                        r.projectScore() * ATSScoreEngine.WEIGHT_PROJECT +
                        r.writingScore() * ATSScoreEngine.WEIGHT_WRITING +
                        r.professionalismScore() * ATSScoreEngine.WEIGHT_PROFESSIONAL)
                        * 100.0)
                / 100.0;

        assertEquals(expected, r.overallScore(), 0.001,
                "overallScore must equal the weighted average of all 6 section scores " +
                        "using the exact weights from SCORING_RULES.md, rounded to 2 decimal places");
    }

    // EDGE CASES

    @Test
    void should_returnZeroSkillsScore_whenJdIsBlankProducingNoKeywords() {
        // Blank JD → topKeywords returns [] → calcSkills short-circuits to 0.0
        ATSScoreEngine.ScoreResult r = engine.score(TestData.GOOD_RESUME, "   ", false, false);
        assertEquals(0.0, r.skillsScore(), 0.01,
                "Blank JD produces no keywords → skillsScore should be 0");
    }

    @Test
    void should_scoreMissingAndEmptySectionsWithMaximumPenalties() {
        // Resume has no standard sections, no keywords, no bullets, no achievements
        String resume = "software engineer looking for opportunities";
        ATSScoreEngine.ScoreResult r = engine.score(resume, TestData.JD_FOREIGN_TERMS, false, false);
        assertTrue(r.atsScore() <= 60,
                "Resume missing all sections + 0% keywords + no achievements should score <= 60 on ATS");
        assertEquals(0.0, r.impactScore(), 0.01,
                "Resume with no bullet points should have impactScore 0");
    }

    // REQUIREMENT 2 TESTS — Hand-computed literals from docs/SCORING_RULES.md

    @Test
    void should_matchScoringRulesWeightsAndSumToOne() {
        assertEquals(0.25, ATSScoreEngine.WEIGHT_ATS, 1e-9);
        assertEquals(0.25, ATSScoreEngine.WEIGHT_SKILLS, 1e-9);
        assertEquals(0.20, ATSScoreEngine.WEIGHT_IMPACT, 1e-9);
        assertEquals(0.15, ATSScoreEngine.WEIGHT_PROJECT, 1e-9);
        assertEquals(0.10, ATSScoreEngine.WEIGHT_WRITING, 1e-9);
        assertEquals(0.05, ATSScoreEngine.WEIGHT_PROFESSIONAL, 1e-9);
        assertEquals(1.0,
                ATSScoreEngine.WEIGHT_ATS + ATSScoreEngine.WEIGHT_SKILLS +
                ATSScoreEngine.WEIGHT_IMPACT + ATSScoreEngine.WEIGHT_PROJECT +
                ATSScoreEngine.WEIGHT_WRITING + ATSScoreEngine.WEIGHT_PROFESSIONAL,
                1e-9);
    }

    @Test
    void should_computeHardcodedOverallScore_forFixedFixtureFromScoringRules() {
        // TestData.GOOD_RESUME with TestData.JD_TWO_TERMS:
        // ats = 100.0, skills = 100.0, impact = 0.0, project = 50.0 (neutral),
        // writing = 100.0, professionalism = 100.0.
        // Hand-computed literal overall score from SCORING_RULES.md:
        // (100.0 * 0.25) + (100.0 * 0.25) + (0.0 * 0.20) + (50.0 * 0.15) + (100.0 * 0.10) + (100.0 * 0.05) = 72.50
        ATSScoreEngine.ScoreResult r = engine.score(TestData.GOOD_RESUME, TestData.JD_TWO_TERMS, false, false);
        assertEquals(72.50, r.overallScore(), 0.001,
                "overallScore must equal 72.50 literal calculated by hand from SCORING_RULES.md");
    }

    @Test
    void should_handleKeywordDensityBoundariesCorrectly() {
        // 10 distinct technical keywords (length >= 6 to avoid accidental fuzzy match)
        String jd10 = "kubernetes\ndocker\npostgres\nmongodb\ngraphql\nterraform\njenkins\ntypescript\nmicroservices\nrabbitmq";

        // Exactly 30% keyword match (3 of 10 keywords: kubernetes, docker, postgres): gives -8 deduction, not -15
        String resume30 = "EDUCATION\nEXPERIENCE\nSKILLS\nkubernetes docker postgres developer achieved 40% improvement";
        ATSScoreEngine.ScoreResult r30 = engine.score(resume30, jd10, false, false);
        assertEquals(92.0, r30.atsScore(), 0.01,
                "Exactly 30% keyword density gives -8 deduction (100 - 8 = 92), not -15");

        // Exactly 50% keyword match (5 of 10 keywords): gives no deduction (0)
        String resume50 = "EDUCATION\nEXPERIENCE\nSKILLS\nkubernetes docker postgres mongodb graphql developer achieved 40% improvement";
        ATSScoreEngine.ScoreResult r50 = engine.score(resume50, jd10, false, false);
        assertEquals(100.0, r50.atsScore(), 0.01,
                "Exactly 50% keyword density gives no deduction (100 - 0 = 100)");

        // Just below 30% keyword match: 7 distinct keywords, 2 match -> 2/7 = 28.57% < 30%
        String jd7 = "kubernetes\ndocker\npostgres\nmongodb\ngraphql\nterraform\njenkins";
        String resumeBelow30 = "EDUCATION\nEXPERIENCE\nSKILLS\nkubernetes docker developer achieved 40% improvement";
        ATSScoreEngine.ScoreResult rBelow30 = engine.score(resumeBelow30, jd7, false, false);
        assertEquals(85.0, rBelow30.atsScore(), 0.01,
                "Just below 30% keyword density (2/7 = 28.57%) gives -15 deduction (100 - 15 = 85)");
    }

    @Test
    void isFuzzyMatchReturnsTrueAtThresholdAndFalseAtThresholdPlusOne() {
        assertEquals(2, similarity.levenshteinDistance("java", "javaaa"));
        assertTrue(similarity.isFuzzyMatch("java", "javaaa", 2),
                "isFuzzyMatch returns true when distance equals threshold (2 == 2)");

        assertEquals(3, similarity.levenshteinDistance("java", "javaaaa"));
        assertFalse(similarity.isFuzzyMatch("java", "javaaaa", 2),
                "isFuzzyMatch returns false when distance equals threshold + 1 (3 > 2)");
    }

    @Test
    void should_floorAtsWritingAndProfessionalismAtZero_neverNegative() {
        when(resumeParser.hasContactInfo(anyString())).thenReturn(false);

        // Worst-case text hitting every deduction in ATS, Writing, and Professionalism:
        // - No education, experience, skills headings (-24)
        // - Contact info absent (-10)
        // - Multi-column formatting risk (-10)
        // - Graphics risk (-8)
        // - Keyword density < 30% (-15)
        // - No measurable achievements (0: 2020 is a 4-digit standalone number so -5 risk does not fire under new rule)
        // ATS total deductions: -67 -> score 33.0 (>= 0 floor)
        //
        // Writing deductions:
        // - Sentence length > 35 words (-10)
        // - First-person pronoun 'I' (-15)
        // - 5 filler phrases (-15)
        // - 3 over-repeated words (-15)
        // - Passive voice > 40% (3 of 4 sentences = 75%) (-10)
        // Writing total deductions: -65 -> score 35.0 (>= 0 floor)
        //
        // Professionalism deductions:
        // - 4 spelling errors (-20)
        // - Mixed date formats Jan 2020 and 01/2023 (-10)
        // - Mixed bullets (-5)
        // - Casual email cool420@ (-10)
        // - Word count > 700 (-5)
        // Professionalism total deductions: -50 -> score 50.0 (>= 0 floor)

        StringBuilder sb = new StringBuilder();
        sb.append("The project was managed by the team. ");
        sb.append("Tasks were implemented carefully. ");
        sb.append("Work was delivered on schedule. ");
        sb.append("I am responsible for coding and helped with testing and assisted in design and involved in planning and was part of review. ");
        sb.append("repeatone repeatone repeatone repeatone repeatone repeatone ");
        sb.append("repeattwo repeattwo repeattwo repeattwo repeattwo repeattwo ");
        sb.append("repeatthree repeatthree repeatthree repeatthree repeatthree repeatthree ");
        sb.append("recieve achievment managment developement ");
        sb.append("Jan 2020 and 01/2023 ");
        sb.append("- bullet one\n* bullet two\n• bullet three\n");
        sb.append("cool420@example.com ");
        sb.append(TestData.uniqueTokens(720));

        ATSScoreEngine.ScoreResult r = engine.score(sb.toString(), TestData.JD_FOREIGN_TERMS, true, true);

        assertEquals(33.0, r.atsScore(), 0.01, "ATS score must equal 33.0 literal worst-case score");
        assertEquals(35.0, r.writingScore(), 0.01, "Writing score must equal 35.0 literal worst-case score");
        assertEquals(50.0, r.professionalismScore(), 0.01, "Professionalism score must equal 50.0 literal worst-case score");

        assertTrue(r.atsScore() >= 0.0, "ATS score must never be negative");
        assertTrue(r.writingScore() >= 0.0, "Writing score must never be negative");
        assertTrue(r.professionalismScore() >= 0.0, "Professionalism score must never be negative");
    }

    @Test
    void should_computeSkillsScoreWithKnownMatchRatioAndCosineBonus_andCapAt100() {
        // JD: "java python", Resume: "java"
        // matched = 1 / 2 = 50.0 base score
        // Cosine bonus hand-computed from docs/SCORING_RULES.md:
        // idf(java) = 1.0, idf(python) = 1.4055
        // cosine similarity = 0.5797 -> bonus = 5.80
        // Expected skills score = 55.80
        ATSScoreEngine.ScoreResult rPartial = engine.score("java", TestData.JD_TWO_TERMS, false, false);
        assertEquals(55.80, rPartial.skillsScore(), 0.01,
                "Skills score must equal base 50.0 + 5.80 cosine bonus = 55.80");

        // When base score is 100.0, cosine bonus cannot exceed 100.0 cap
        ATSScoreEngine.ScoreResult rFull = engine.score(TestData.GOOD_RESUME, TestData.JD_TWO_TERMS, false, false);
        assertEquals(100.0, rFull.skillsScore(), 0.01,
                "Skills score must be capped at 100.0");
    }

    @Test
    void should_evaluateProjectQuality_2Of4Scores50_all4Scores100_andTwoProjectsAveraged() {
        // Project 1: meets 2 of 4 criteria (has title, has tech stack "java") -> 2/4 = 50
        String project1 = "Project Alpha\njava code";

        // Project 2: meets all 4 criteria (title, tech stack "python", description >= 10 words, outcome "automated") -> 4/4 = 100
        String project2 = "Project Beta\nBuilt an automated python tool that processes data across multiple servers daily";

        // Test Project 1 alone -> scores 50.0
        when(resumeParser.extractSections(anyString())).thenReturn(Map.of("PROJECTS", project1));
        ATSScoreEngine.ScoreResult r1 = engine.score(TestData.GOOD_RESUME, TestData.JD_TWO_TERMS, false, false);
        assertEquals(50.0, r1.projectScore(), 0.01,
                "Project meeting 2 of 4 criteria must score 50.0");

        // Test Project 2 alone -> scores 100.0
        when(resumeParser.extractSections(anyString())).thenReturn(Map.of("PROJECTS", project2));
        ATSScoreEngine.ScoreResult r2 = engine.score(TestData.GOOD_RESUME, TestData.JD_TWO_TERMS, false, false);
        assertEquals(100.0, r2.projectScore(), 0.01,
                "Project meeting all 4 criteria must score 100.0");

        // Test two projects together (separated by blank line) -> averaged: (50.0 + 100.0) / 2 = 75.0
        String twoProjects = project1 + "\n\n" + project2;
        when(resumeParser.extractSections(anyString())).thenReturn(Map.of("PROJECTS", twoProjects));
        ATSScoreEngine.ScoreResult rBoth = engine.score(TestData.GOOD_RESUME, TestData.JD_TWO_TERMS, false, false);
        assertEquals(75.0, rBoth.projectScore(), 0.01,
                "Two projects meeting 2/4 and 4/4 criteria must be averaged to 75.0");
    }

    @Test
    void should_deductExactly8_whenGraphicsArePresent() {
        ATSScoreEngine.ScoreResult withoutGraphics = engine.score(TestData.GOOD_RESUME, TestData.JD_TWO_TERMS, false, false);
        ATSScoreEngine.ScoreResult withGraphics = engine.score(TestData.GOOD_RESUME, TestData.JD_TWO_TERMS, false, true);

        assertEquals(100.0, withoutGraphics.atsScore(), 0.01);
        assertEquals(92.0, withGraphics.atsScore(), 0.01);
        assertEquals(8.0, withoutGraphics.atsScore() - withGraphics.atsScore(), 0.01,
                "Graphics present must give exactly minus 8 deduction");
    }

    // RISKS TESTS

    @Test
    void should_produceEmptyRisksList_whenResumeIsClean() {
        ATSScoreEngine.ScoreResult r = engine.score(TestData.GOOD_RESUME, TestData.JD_TWO_TERMS, false, false);
        assertNotNull(r.risks(), "Risks list must not be null");
        assertTrue(r.risks().isEmpty(), "A clean resume must produce an empty risks list");
    }

    @Test
    void should_produceMediumRiskWithNonEmptyDescriptionAndFix_whenNoContactInfo() {
        when(resumeParser.hasContactInfo(anyString())).thenReturn(false);
        ATSScoreEngine.ScoreResult r = engine.score(TestData.GOOD_RESUME, TestData.JD_TWO_TERMS, false, false);

        assertNotNull(r.risks(), "Risks list must not be null");
        assertEquals(1, r.risks().size(), "Only contact info risk should fire");

        var contactRisk = r.risks().get(0);
        assertEquals("MEDIUM", contactRisk.severity(), "Contact info deduction (10 points) must produce MEDIUM severity");
        assertNotNull(contactRisk.title());
        assertFalse(contactRisk.title().isBlank(), "Risk title must not be blank");
        assertNotNull(contactRisk.description());
        assertFalse(contactRisk.description().isBlank(), "Risk description must not be blank");
        assertNotNull(contactRisk.fix());
        assertFalse(contactRisk.fix().isBlank(), "Risk fix must not be blank");
    }

    @Test
    void should_orderRisksBySeverityHighMediumLow() {
        // Trigger HIGH (first person: -15), MEDIUM (no contact: -10), LOW (no measurable achievements: -5)
        when(resumeParser.hasContactInfo(anyString())).thenReturn(false);
        String resume = "EDUCATION\nBachelor\nEXPERIENCE\nI worked as software engineer with java and python\nSKILLS\njava python";
        ATSScoreEngine.ScoreResult r = engine.score(resume, TestData.JD_TWO_TERMS, false, false);

        assertNotNull(r.risks());
        assertFalse(r.risks().isEmpty());

        int prevRank = 0;
        for (var risk : r.risks()) {
            int currentRank = switch (risk.severity()) {
                case "HIGH" -> 1;
                case "MEDIUM" -> 2;
                case "LOW" -> 3;
                default -> 4;
            };
            assertTrue(currentRank >= prevRank, "Risks must be sorted in order of severity (HIGH -> MEDIUM -> LOW)");
            prevRank = currentRank;
        }
    }

    // REQUIREMENT 5 TESTS - Hand-computed literals for false positive reduction

    @Test
    void should_countStandaloneNumbersAsQuantified_andRejectGluedNumbers() {
        // Standalone numbers count
        assertTrue(ATSScoreEngine.isQuantified("6000+"), "6000+ must count as quantified");
        assertTrue(ATSScoreEngine.isQuantified("200 teams"), "200 teams must count as quantified");
        assertTrue(ATSScoreEngine.isQuantified("Rank 152"), "Rank 152 must count as quantified");
        assertTrue(ATSScoreEngine.isQuantified("30%"), "30% must count as quantified");

        // Glued numbers do not count
        assertFalse(ATSScoreEngine.isQuantified("v5"), "v5 must not count as quantified");
        assertFalse(ATSScoreEngine.isQuantified("ESP32"), "ESP32 must not count as quantified");
        assertFalse(ATSScoreEngine.isQuantified("S3"), "S3 must not count as quantified");
        assertFalse(ATSScoreEngine.isQuantified("2nd"), "2nd must not count as quantified");

        // Verify impact on minus 5 risk in ATS Compatibility
        String resumeQuantified = "EDUCATION\nBachelor\nEXPERIENCE\n" +
                "Rank 152 in coding challenge with 6000+ participants and 200 teams\n" +
                "SKILLS\njava python";
        ATSScoreEngine.ScoreResult rQuantified = engine.score(resumeQuantified, TestData.JD_TWO_TERMS, false, false);
        assertEquals(100.0, rQuantified.atsScore(), 0.01,
                "Resume with standalone numbers (Rank 152, 6000+, 200 teams) must not incur -5 deduction");
        assertTrue(rQuantified.risks().stream().noneMatch(rk -> "NO_MEASURABLE_ACHIEVEMENTS".equals(rk.title()) || rk.title().contains("Measurable")),
                "No measurable achievements risk must not fire when standalone numbers exist");

        String resumeGlued = "EDUCATION\nBachelor\nEXPERIENCE\n" +
                "Firmware development for ESP32 and S3 architecture on v5 release in 2nd phase\n" +
                "SKILLS\njava python";
        ATSScoreEngine.ScoreResult rGlued = engine.score(resumeGlued, TestData.JD_TWO_TERMS, false, false);
        assertEquals(95.0, rGlued.atsScore(), 0.01,
                "Resume with only glued numbers (ESP32, S3, v5, 2nd) must incur -5 deduction");
        assertTrue(rGlued.risks().stream().anyMatch(rk -> rk.title().contains("Measurable")),
                "No measurable achievements risk must fire when only glued numbers are present");
    }

    @Test
    void should_excludeSkillsAndGenericWordsFromRepetition_andFlagPlainWordManaged() {
        // Resume where react and tailwind repeat 8 times: skill dictionary terms excluded -> no risk
        String resumeSkills = "EDUCATION\nBachelor of Science\nEXPERIENCE\n" +
                "Software engineer who achieved 40% improvement in product delivery.\n" +
                "react react react react react react react react\n" +
                "tailwind tailwind tailwind tailwind tailwind tailwind tailwind tailwind\n" +
                "SKILLS\nreact tailwind";
        ATSScoreEngine.ScoreResult rSkills = engine.score(resumeSkills, "react tailwind", false, false);
        assertEquals(100.0, rSkills.writingScore(), 0.01,
                "Repeating technical skills (react, tailwind) 8 times must incur 0 repetition deduction");
        assertTrue(rSkills.risks().stream().noneMatch(rk -> rk.title().contains("Repeated")),
                "No repeated word risk should fire for skill dictionary terms");

        // Resume where plain word 'managed' repeats 8 times: length 7, non-skill, non-generic -> fires risk
        String resumeManaged = "EDUCATION\nBachelor of Science\nEXPERIENCE\n" +
                "Software engineer who achieved 40% improvement in product delivery.\n" +
                "managed managed managed managed managed managed managed managed\n" +
                "SKILLS\nreact tailwind";
        ATSScoreEngine.ScoreResult rManaged = engine.score(resumeManaged, "react tailwind", false, false);
        assertEquals(95.0, rManaged.writingScore(), 0.01,
                "Repeating plain word 'managed' 8 times must incur -5 repetition deduction (100 - 5 = 95)");
        assertTrue(rManaged.risks().stream().anyMatch(rk -> rk.title().contains("Repeated")),
                "Repeated word risk must fire when plain word 'managed' repeats 8 times");
        var repRisk = rManaged.risks().stream().filter(rk -> rk.title().contains("Repeated")).findFirst().orElseThrow();
        assertEquals("LOW", repRisk.severity(), "5 point deduction must produce LOW severity");
    }

    @Test
    void should_handleBulletConsistency_singleDashLineConsistent_multipleDashLinesInconsistent() {
        // Resume with bullets using only • plus one line starting with a dash: consistent bullets
        String resumeSingleDash = "EDUCATION\nBachelor\nEXPERIENCE\n" +
                "• Led software engineering team achieving 40% improvement\n" +
                "• Designed distributed systems architecture\n" +
                "• Deployed automated cloud infrastructure\n" +
                "- single wrapped line starting with a dash\n" +
                "SKILLS\njava python";
        ATSScoreEngine.ScoreResult rSingleDash = engine.score(resumeSingleDash, TestData.JD_TWO_TERMS, false, false);
        assertEquals(100.0, rSingleDash.professionalismScore(), 0.01,
                "Bullets using only • plus one dash line must have consistent bullets (professionalismScore 100)");
        assertTrue(rSingleDash.risks().stream().noneMatch(rk -> rk.title().contains("Bullet")),
                "Inconsistent bullets risk must not fire when only one line starts with dash");

        // Resume using • on 3 lines and - on 3 lines: inconsistent bullets
        String resumeMultiDash = "EDUCATION\nBachelor\nEXPERIENCE\n" +
                "• Led software engineering team achieving 40% improvement\n" +
                "• Designed distributed systems architecture\n" +
                "• Deployed automated cloud infrastructure\n" +
                "- Built reliable backend APIs with high throughput\n" +
                "- Streamlined integration pipelines across environments\n" +
                "- Coordinated cross functional release readiness\n" +
                "SKILLS\njava python";
        ATSScoreEngine.ScoreResult rMultiDash = engine.score(resumeMultiDash, TestData.JD_TWO_TERMS, false, false);
        assertEquals(95.0, rMultiDash.professionalismScore(), 0.01,
                "Resume with • on 3 lines and - on 3 lines must incur -5 deduction (professionalismScore 95)");
        assertTrue(rMultiDash.risks().stream().anyMatch(rk -> rk.title().contains("Bullet")),
                "Inconsistent bullets risk must fire when both • and - have multiple lines");
    }

    @Test
    void should_formatRepeatedWordsDescriptionWithHandComputedCounts() {
        // Hand-computed counts:
        // 'managed' appears 8 times (> 5)
        // 'custom' appears 6 times (> 5)
        String resume = "EDUCATION\nBachelor of Science\nEXPERIENCE\n" +
                "Software developer who achieved 40% improvement in product delivery.\n" +
                "managed managed managed managed managed managed managed managed\n" +
                "custom custom custom custom custom custom\n" +
                "SKILLS\njava python";

        ATSScoreEngine.ScoreResult result = engine.score(resume, "java python", false, false);
        var repRisk = result.risks().stream()
                .filter(rk -> rk.title().contains("Repeated"))
                .findFirst()
                .orElseThrow();

        // Hand-computed expected literal ending
        String expectedEnding = "Words used more than 5 times: managed (8), custom (6).";
        assertTrue(repRisk.description().endsWith(expectedEnding),
                "Repeated words description must end with hand-computed literal: " + expectedEnding);
    }

    @Test
    void should_haveNoMissingHeadingDeduction_whenUsingAliasesWorkExperienceTechnicalSkillsAcademicProjectsEducation() {
        String resume = "Work Experience:\n" +
                "Software Engineer who built cloud pipelines with 40% speedup\n" +
                "Technical Skills:\n" +
                "java python docker kubernetes\n" +
                "Academic Projects:\n" +
                "Developed distributed data analysis system\n" +
                "Education:\n" +
                "Bachelor of Computer Science 2020";

        ATSScoreEngine.ScoreResult r = engine.score(resume, TestData.JD_TWO_TERMS, false, false);
        assertEquals(100.0, r.atsScore(), 0.01,
                "Resume with Work Experience, Technical Skills, Academic Projects, and Education must have 0 missing heading deduction (atsScore 100)");
        assertTrue(r.risks().stream().noneMatch(rk -> rk.title().contains("Missing Section Headings")),
                "No MISSING_SECTIONS risk should fire when all required section aliases are present");
    }

    @Test
    void should_deductForMissingExperience_whenExperienceSentenceIsNotAHeading() {
        // Resume has Education and Skills, but only a normal sentence mentioning experience
        String resume = "EDUCATION\nBachelor of Computer Science\n" +
                "SKILLS\njava python\n" +
                "Experience with React and Node for 3 years achieving 40% improvement";
        ATSScoreEngine.ScoreResult r = engine.score(resume, TestData.JD_TWO_TERMS, false, false);
        // Experience is missing because the sentence is not a heading -> 1 missing section -> -8 deduction
        assertEquals(92.0, r.atsScore(), 0.01,
                "Sentence 'Experience with React and Node' must not count as an Experience section heading; -8 deduction must apply");
        assertTrue(r.risks().stream().anyMatch(rk -> rk.title().contains("Missing Section Headings")),
                "MISSING_SECTIONS risk must fire when Experience section heading is absent");
    }
}
