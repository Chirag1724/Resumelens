package com.resumelens.service;

import com.resumelens.TestData;
import com.resumelens.algorithm.SimilarityCalculator;
import com.resumelens.algorithm.TfIdfCalculator;
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
        when(resumeParser.extractSections(anyString())).thenReturn(Map.of());
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
        // 3 distinct words each repeated >5 times → 3×5=15, capped at 15
        String resume = "EDUCATION\nEXPERIENCE\nSKILLS\n" +
                "java java java java java java " + // 6 → 1 over-repeated word
                "docker docker docker docker docker docker " + // 6 → 2
                "spring spring spring spring spring spring"; // 6 → 3 → total 15, capped
        ATSScoreEngine.ScoreResult r = engine.score(resume, TestData.JD_TWO_TERMS, false, false);
        assertEquals(85.0, r.writingScore(), 0.01,
                "3 over-repeated words × 5 = 15, capped → –15 → writingScore 85");
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
        // Mix of dash and bullet styles
        String resume = "EDUCATION\nEXPERIENCE\n" +
                "- java developer\n" +
                "• python expert\n" +
                "SKILLS\njava python 40%";
        ATSScoreEngine.ScoreResult r = engine.score(resume, TestData.JD_TWO_TERMS, false, false);
        assertEquals(95.0, r.professionalismScore(), 0.01,
                "Mixed bullet styles (- and •) → –5 → professionalismScore 95");
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
                "Resume missing all sections + 0% keywords + no achievements should score ≤ 60 on ATS");
        assertEquals(0.0, r.impactScore(), 0.01,
                "Resume with no bullet points should have impactScore 0");
    }
}
