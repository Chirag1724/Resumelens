package com.resumelens.service;

import com.resumelens.algorithm.SimilarityCalculator;
import com.resumelens.algorithm.TfIdfCalculator;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Implements every scoring rule from docs/SCORING_RULES.md exactly.
 * Do not change any constant here without a corresponding update to SCORING_RULES.md
 * and an entry in DECISIONS_LOG.md.
 */
@Service
public class ATSScoreEngine {

    // Weights (SCORING_RULES.md & Score Architecture)
    static final double WEIGHT_ATS           = 0.25;
    static final double WEIGHT_SKILLS        = 0.25;
    static final double WEIGHT_IMPACT        = 0.20;
    static final double WEIGHT_PROJECT       = 0.15;
    static final double WEIGHT_WRITING       = 0.10;
    static final double WEIGHT_PROFESSIONAL  = 0.05;

    // Levenshtein threshold
    static final int LEVENSHTEIN_THRESHOLD   = 2;

    // Keyword cap
    static final int MAX_JD_KEYWORDS         = 30;

    // Standard section headings
    private static final List<String> REQUIRED_SECTIONS =
        List.of("education", "experience", "skills");

    // Action verb list (SCORING_RULES.md & Impact & Achievements)
    private static final Set<String> ACTION_VERBS = Set.of(
        "led", "built", "owned", "architected", "drove", "designed", "developed",
        "implemented", "delivered", "launched", "managed", "created", "established",
        "reduced", "increased", "improved", "achieved", "deployed", "automated",
        "optimised", "optimized", "engineered", "scaled", "migrated", "integrated",
        "spearheaded", "streamlined", "transformed", "initiated", "directed"
    );

    // Filler / weak phrases
    private static final List<String> FILLER_PHRASES = List.of(
        "responsible for", "helped with", "assisted in", "involved in", "participated in",
        "worked on", "was part of"
    );

    // First-person pronouns
    private static final Pattern FIRST_PERSON =
        Pattern.compile("\\b(i|me|my|we|our)\\b", Pattern.CASE_INSENSITIVE);

    // Tech keyword list for project quality check
    private static final Set<String> TECH_KEYWORDS = Set.of(
        "java","python","javascript","typescript","react","angular","vue","spring",
        "django","flask","node","express","mysql","postgresql","mongodb","redis",
        "docker","kubernetes","aws","azure","gcp","git","kafka","rabbitmq","rest",
        "graphql","microservices","ci","cd","linux","html","css","android","ios",
        "swift","kotlin","c++","rust","go","terraform","jenkins"
    );

    // Result-oriented keywords for project outcome check
    private static final Set<String> OUTCOME_WORDS = Set.of(
        "deployed","reduced","achieved","improved","increased","delivered",
        "launched","scaled","optimised","optimized","automated","saved"
    );

    // Stopwords for repetition check
    private static final Set<String> STOPWORDS = Set.of(
        "a","an","the","and","or","but","in","on","at","to","for","of","with",
        "by","from","is","are","was","were","be","been","being","have","has",
        "had","do","does","did","will","would","could","should","may","might"
    );

    // Passive voice markers
    private static final Pattern PASSIVE_PATTERN =
        Pattern.compile("\\b(was|were|been|is|are|be)\\s+\\w+ed\\b", Pattern.CASE_INSENSITIVE);

    private final TfIdfCalculator tfidf;
    private final SimilarityCalculator similarity;
    private final ResumeParserService resumeParser;

    public ATSScoreEngine(TfIdfCalculator tfidf,
                          SimilarityCalculator similarity,
                          ResumeParserService resumeParser) {
        this.tfidf = tfidf;
        this.similarity = similarity;
        this.resumeParser = resumeParser;
    }

    // Public interface
    public ScoreResult score(String resumeText, String jdText,
                             boolean hasFormattingRisk, boolean hasGraphicsRisk) {

        List<String> jdKeywords = tfidf.topKeywords(jdText, MAX_JD_KEYWORDS);

        double ats     = round(calcAts(resumeText, jdText, jdKeywords, hasFormattingRisk, hasGraphicsRisk));
        double skills  = round(calcSkills(resumeText, jdText, jdKeywords));
        double impact  = round(calcImpact(resumeText));
        double project = round(calcProject(resumeText));
        double writing = round(calcWriting(resumeText));
        double professionalism = round(calcProfessionalism(resumeText));

        double overall = round(
            ats    * WEIGHT_ATS    +
            skills * WEIGHT_SKILLS +
            impact * WEIGHT_IMPACT +
            project * WEIGHT_PROJECT +
            writing * WEIGHT_WRITING +
            professionalism * WEIGHT_PROFESSIONAL
        );

        double keywordMatchPercent = round(calcKeywordMatchPercent(resumeText, jdKeywords));

        List<String> matched = jdKeywords.stream()
            .filter(kw -> isMatchedInResume(kw, resumeText))
            .collect(Collectors.toList());
        List<String> missing = jdKeywords.stream()
            .filter(kw -> !isMatchedInResume(kw, resumeText))
            .collect(Collectors.toList());

        return new ScoreResult(overall, ats, skills, impact, project, writing,
                               professionalism, statusLabel(overall),
                               keywordMatchPercent, matched, missing);
    }


    // 1. ATS Compatibility (SCORING_RULES.md)
    private double calcAts(String resumeText, String jdText, List<String> jdKeywords,
                           boolean hasFormattingRisk, boolean hasGraphicsRisk) {
        double score = 100.0;

        // Missing standard section headings (–8 per, max –24)
        String lower = resumeText.toLowerCase();
        long missingSections = REQUIRED_SECTIONS.stream()
            .filter(s -> !lower.contains(s))
            .count();
        score -= Math.min(missingSections * 8.0, 24.0);

        // Contact info absent (–10)
        if (!resumeParser.hasContactInfo(resumeText)) score -= 10;

        // Multi-column / table layout (–10)
        if (hasFormattingRisk) score -= 10;

        // Graphics / non-standard fonts heuristic (–8)
        if (hasGraphicsRisk) score -= 8;

        // Keyword density deduction (–15 or –8, not both)
        double matchPct = calcKeywordMatchPercent(resumeText, jdKeywords);
        if (matchPct < 0.30) {
            score -= 15;
        } else if (matchPct < 0.50) {
            score -= 8;
        }

        // No measurable achievements (–5)
        if (!hasMeasurableAchievements(resumeText)) score -= 5;

        return Math.max(0.0, score);
    }

    // 2. Skills Validation (SCORING_RULES.md)
    private double calcSkills(String resumeText, String jdText, List<String> jdKeywords) {
        if (jdKeywords.isEmpty()) return 0.0;

        long matched = jdKeywords.stream()
            .filter(kw -> isMatchedInResume(kw, resumeText))
            .count();
        double base = ((double) matched / jdKeywords.size()) * 100.0;

        // Cosine similarity bonus (up to +10, capped at 100)
        List<String> docs = List.of(resumeText, jdText);
        Map<String, Double> idfMap = tfidf.idf(docs);
        Map<String, Double> resumeVec = tfidf.tfidfVector(resumeText, idfMap);
        Map<String, Double> jdVec     = tfidf.tfidfVector(jdText, idfMap);
        double cosBonus = similarity.cosineSimilarity(resumeVec, jdVec) * 10.0;

        return Math.min(100.0, base + cosBonus);
    }

    // 3. Impact & Achievements (SCORING_RULES.md)
    private double calcImpact(String resumeText) {
        String[] lines = resumeText.split("\n");
        long totalBullets = 0, impactBullets = 0;
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("-") || trimmed.startsWith("–")
                    || trimmed.startsWith("•") || trimmed.startsWith("*")) {
                totalBullets++;
                if (hasNumericValue(trimmed) || hasActionVerb(trimmed)) {
                    impactBullets++;
                }
            }
        }
        if (totalBullets == 0) return 0.0;
        double pct = ((double) impactBullets / totalBullets) * 100.0;
        // Percentage-to-score mapping (SCORING_RULES.md table)
        if (pct >= 70) return 100;
        if (pct >= 50) return 80;
        if (pct >= 30) return 60;
        if (pct >= 10) return 40;
        if (pct >=  1) return 20;
        return 0;
    }

    // 4. Project Quality (SCORING_RULES.md)
    private double calcProject(String resumeText) {
        Map<String, String> sections = resumeParser.extractSections(resumeText);
        String projectsSection = sections.entrySet().stream()
            .filter(e -> e.getKey().contains("PROJECT"))
            .map(Map.Entry::getValue)
            .findFirst().orElse(null);

        if (projectsSection == null || projectsSection.isBlank()) {
            return 50.0;  // neutral, not penalised — SCORING_RULES.md §4
        }

        // Split into individual project blocks (separated by blank lines)
        String[] blocks = projectsSection.split("\n{2,}");
        if (blocks.length == 0) return 50.0;

        double totalSubScore = 0.0;
        for (String block : blocks) {
            totalSubScore += projectSubScore(block);
        }
        return (totalSubScore / blocks.length) * 100.0;
    }

    private double projectSubScore(String block) {
        int met = 0;
        String lower = block.toLowerCase();

        // Has a title: first line is non-empty and short enough to be a heading
        String[] lines = block.trim().split("\n");
        if (lines.length > 0 && !lines[0].isBlank() && lines[0].trim().length() < 80) met++;

        // Has a tech stack mention
        if (TECH_KEYWORDS.stream().anyMatch(lower::contains)) met++;

        // Has a description (at least 10 words in body)
        long wordCount = Arrays.stream(block.split("\\s+")).filter(w -> !w.isBlank()).count();
        if (wordCount >= 10) met++;

        // Has a measurable outcome
        boolean hasNumber = hasNumericValue(lower);
        boolean hasOutcome = OUTCOME_WORDS.stream().anyMatch(lower::contains);
        if (hasNumber || hasOutcome) met++;

        return (double) met / 4.0;
    }

    // 5. Writing Quality (SCORING_RULES.md)
    private double calcWriting(String resumeText) {
        double score = 100.0;
        String[] sentences = resumeText.split("[.!?]+");

        // Average sentence length > 35 words (–10)
        double avgLen = Arrays.stream(sentences)
            .mapToLong(s -> Arrays.stream(s.split("\\s+")).filter(w -> !w.isBlank()).count())
            .average().orElse(0);
        if (avgLen > 35) score -= 10;

        // First-person pronouns (–15)
        if (FIRST_PERSON.matcher(resumeText).find()) score -= 15;

        // Filler words (–3 per occurrence, max –15)
        String lower = resumeText.toLowerCase();
        long fillerCount = FILLER_PHRASES.stream()
            .mapToLong(f -> countOccurrences(lower, f))
            .sum();
        score -= Math.min(fillerCount * 3.0, 15.0);

        // Repetition (–5 per over-repeated word, max –15)
        Map<String, Long> freq = Arrays.stream(TfIdfCalculator.tokenize(resumeText))
            .filter(w -> w.length() > 2 && !STOPWORDS.contains(w))
            .collect(Collectors.groupingBy(w -> w, Collectors.counting()));
        long repeatedWordCount = freq.values().stream().filter(v -> v > 5).count();
        score -= Math.min(repeatedWordCount * 5.0, 15.0);

        // Passive voice ratio > 40% (–10)
        long passiveCount = Arrays.stream(sentences)
            .filter(s -> PASSIVE_PATTERN.matcher(s).find())
            .count();
        if (sentences.length > 0 && (double) passiveCount / sentences.length > 0.40) {
            score -= 10;
        }

        return Math.max(0.0, score);
    }

    // 6. Professionalism (SCORING_RULES.md)
    private static final Pattern DATE_ALPHA   = Pattern.compile("(?i)(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z]*\\s+\\d{4}");
    private static final Pattern DATE_NUMERIC = Pattern.compile("\\b(0?[1-9]|1[0-2])/\\d{4}\\b");
    private static final Pattern CASUAL_EMAIL = Pattern.compile("[a-z0-9._%+\\-]*(?:cool|swag|sexy|lol|xd|420|69|1337)[a-z0-9._%+\\-]*@", Pattern.CASE_INSENSITIVE);

    private double calcProfessionalism(String resumeText) {
        double score = 100.0;

        // Spelling: simple check - words with double-consonant errors or common typos
        // A full dictionary check would need a bundled word list.
        // Using a small targeted list of the most common resume misspellings.
        // Upgrade path: bundle a curated word list and use a trie lookup.
        long spellingErrors = countSpellingErrors(resumeText);
        score -= Math.min(spellingErrors * 5.0, 20.0);

        // Inconsistent date formatting (–10)
        boolean hasAlpha   = DATE_ALPHA.matcher(resumeText).find();
        boolean hasNumeric = DATE_NUMERIC.matcher(resumeText).find();
        if (hasAlpha && hasNumeric) score -= 10;

        // Inconsistent bullet style (–5)
        boolean hasDash  = resumeText.contains("- ");
        boolean hasBullet = resumeText.contains("• ");
        boolean hasStar   = resumeText.contains("* ");
        if ((hasDash ? 1 : 0) + (hasBullet ? 1 : 0) + (hasStar ? 1 : 0) > 1) score -= 5;

        // Casual email address (–10)
        if (CASUAL_EMAIL.matcher(resumeText).find()) score -= 10;

        // Resume length > 700 words estimated as > 2 pages (–5)
        long wordCount = Arrays.stream(resumeText.split("\\s+")).filter(w -> !w.isBlank()).count();
        if (wordCount > 700) score -= 5;

        return Math.max(0.0, score);
    }

    // Status label (SCORING_RULES.md & Status Label Thresholds)
    public static String statusLabel(double overallScore) {
        if (overallScore >= 85) return "Excellent Match";
        if (overallScore >= 70) return "Strong Match";
        if (overallScore >= 55) return "Moderate Match";
        if (overallScore >= 40) return "Weak Match";
        return "Poor Match";
    }

    // Helpers
    private boolean isMatchedInResume(String keyword, String resumeText) {
        String lower = resumeText.toLowerCase();
        if (lower.contains(keyword.toLowerCase())) return true;
        // Fuzzy match: split resume into tokens and check each
        return Arrays.stream(TfIdfCalculator.tokenize(resumeText))
            .anyMatch(token -> similarity.isFuzzyMatch(token, keyword, LEVENSHTEIN_THRESHOLD));
    }

    private double calcKeywordMatchPercent(String resumeText, List<String> jdKeywords) {
        if (jdKeywords.isEmpty()) return 0.0;
        long matched = jdKeywords.stream().filter(kw -> isMatchedInResume(kw, resumeText)).count();
        return (double) matched / jdKeywords.size();
    }

    private boolean hasMeasurableAchievements(String text) {
        return Pattern.compile("[0-9]+%|[0-9]+x|\\$[0-9]+|[0-9]+\\s*(million|thousand|billion|k\\b)",
            Pattern.CASE_INSENSITIVE).matcher(text).find();
    }

    private boolean hasNumericValue(String text) {
        return Pattern.compile("[0-9]").matcher(text).find();
    }

    private boolean hasActionVerb(String text) {
        String lower = text.toLowerCase();
        return ACTION_VERBS.stream().anyMatch(v -> lower.startsWith(v) || lower.contains(" " + v + " "));
    }

    private long countOccurrences(String text, String phrase) {
        int count = 0, idx = 0;
        while ((idx = text.indexOf(phrase, idx)) != -1) { count++; idx += phrase.length(); }
        return count;
    }

    private static final Set<String> COMMON_MISSPELLINGS = Set.of(
        "recieve","achievment","managment","developement","experiance",
        "techincal","occured","enviroment","seperate","accomodate"
    );

    private long countSpellingErrors(String text) {
        String lower = text.toLowerCase();
        return COMMON_MISSPELLINGS.stream().filter(lower::contains).count();
    }

    private static double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    // Result record - passed back to Analysis Service for persistence
    public record ScoreResult(
        double overallScore,
        double atsScore,
        double skillsScore,
        double impactScore,
        double projectScore,
        double writingScore,
        double professionalismScore,
        String statusLabel,
        double keywordMatchPercent,
        List<String> matchedKeywords,
        List<String> missingKeywords
    ) {}
}