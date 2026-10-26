package com.resumelens.service;

import com.resumelens.algorithm.SimilarityCalculator;
import com.resumelens.algorithm.TfIdfCalculator;
import com.resumelens.dto.response.RiskResponse;
import com.resumelens.entity.SectionScore;
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

    public record RiskDefinition(String title, String description, String fix) {}

    public static final Map<String, RiskDefinition> RISK_DEFINITIONS = Map.ofEntries(
        Map.entry("MISSING_SECTIONS", new RiskDefinition(
            "Missing Section Headings",
            "Standard section headings are missing from the resume.",
            "Add standard section headings including Education, Experience, and Skills."
        )),
        Map.entry("NO_CONTACT_INFO", new RiskDefinition(
            "No Contact Info",
            "No email address or phone number was found on the resume.",
            "Include your email address and phone number at the top of your resume."
        )),
        Map.entry("MULTI_COLUMN_TABLE", new RiskDefinition(
            "Multi-Column or Table Layout",
            "Tables or multi-column layouts were detected which can disrupt ATS parsers.",
            "Use a clean, single-column layout without tables or text boxes."
        )),
        Map.entry("GRAPHICS_PRESENT", new RiskDefinition(
            "Graphics Present",
            "Embedded images, graphics, or non-standard visual elements were detected.",
            "Remove images and graphics to ensure clean text parsing."
        )),
        Map.entry("KEYWORD_DENSITY_BELOW_30", new RiskDefinition(
            "Keyword Density Below 30 Percent",
            "The resume matches a low proportion of keywords from the job description.",
            "Incorporate more target keywords and skills from the job description."
        )),
        Map.entry("KEYWORD_DENSITY_30_TO_50", new RiskDefinition(
            "Keyword Density Between 30 and 50 Percent",
            "The resume matches a moderate proportion of keywords from the job description.",
            "Incorporate additional relevant keywords and skills from the job description."
        )),
        Map.entry("NO_MEASURABLE_ACHIEVEMENTS", new RiskDefinition(
            "No Measurable Achievements",
            "No quantified metrics or measurable results were found in the resume.",
            "Include numbers, percentages, or metrics to quantify accomplishments."
        )),
        Map.entry("FIRST_PERSON", new RiskDefinition(
            "First-Person Language",
            "First-person pronouns were found in the resume text.",
            "Rewrite sentences in the third person using strong action verbs."
        )),
        Map.entry("FILLER_WORDS", new RiskDefinition(
            "Filler Words",
            "Weak filler phrases were detected in the experience descriptions.",
            "Replace filler phrases with concise, direct action verbs."
        )),
        Map.entry("REPEATED_WORDS", new RiskDefinition(
            "Repeated Words",
            "Certain words appear excessively throughout the resume text.",
            "Vary your vocabulary and use synonyms to reduce word repetition."
        )),
        Map.entry("LONG_SENTENCES", new RiskDefinition(
            "Long Sentences",
            "Average sentence length exceeds recommended limits for readability.",
            "Shorten sentences and break complex statements into concise bullet points."
        )),
        Map.entry("PASSIVE_VOICE", new RiskDefinition(
            "Passive Voice Over 40 Percent",
            "A large proportion of sentences use passive voice constructions.",
            "Rewrite passive sentences in active voice with clear subjects and strong action verbs."
        )),
        Map.entry("SPELLING_ERRORS", new RiskDefinition(
            "Spelling Errors",
            "Spelling errors or typos were detected in the text.",
            "Proofread and correct misspelled words throughout the document."
        )),
        Map.entry("INCONSISTENT_DATES", new RiskDefinition(
            "Inconsistent Dates",
            "Date formats vary inconsistently across resume sections.",
            "Adopt a single consistent date format across all sections."
        )),
        Map.entry("INCONSISTENT_BULLETS", new RiskDefinition(
            "Inconsistent Bullets",
            "Multiple different bullet styles are used throughout the resume.",
            "Standardize on a single consistent bullet style across all lists."
        )),
        Map.entry("CASUAL_EMAIL", new RiskDefinition(
            "Casual Email",
            "The email address appears informal or unprofessional.",
            "Use a professional email address containing your first and last name."
        )),
        Map.entry("OVER_700_WORDS", new RiskDefinition(
            "Over 700 Words",
            "Resume length exceeds standard single-column density.",
            "Condense content to keep your resume concise and within standard length."
        ))
    );

    public static String severityFromDeduction(double deduction) {
        if (deduction >= 15.0) {
            return "HIGH";
        } else if (deduction >= 8.0) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    private static int severityRank(String severity) {
        if ("HIGH".equalsIgnoreCase(severity)) return 1;
        if ("MEDIUM".equalsIgnoreCase(severity)) return 2;
        if ("LOW".equalsIgnoreCase(severity)) return 3;
        return 4;
    }

    private void recordRisk(List<RiskResponse> risks, String ruleKey, double deduction) {
        RiskDefinition def = RISK_DEFINITIONS.get(ruleKey);
        if (def != null) {
            risks.add(new RiskResponse(def.title(), severityFromDeduction(deduction), def.description(), def.fix()));
        }
    }

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
        List<RiskResponse> risks = new ArrayList<>();

        double ats     = round(calcAts(resumeText, jdText, jdKeywords, hasFormattingRisk, hasGraphicsRisk, risks));
        double skills  = round(calcSkills(resumeText, jdText, jdKeywords));
        double impact  = round(calcImpact(resumeText));
        double project = round(calcProject(resumeText));
        double writing = round(calcWriting(resumeText, risks));
        double professionalism = round(calcProfessionalism(resumeText, risks));

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

        risks.sort(Comparator.comparingInt(r -> severityRank(r.severity())));

        return new ScoreResult(overall, ats, skills, impact, project, writing,
                               professionalism, statusLabel(overall),
                               keywordMatchPercent, matched, missing, List.copyOf(risks));
    }

    public List<RiskResponse> computeRisks(String resumeText, String jdText,
                                           boolean hasFormattingRisk, boolean hasGraphicsRisk) {
        return score(resumeText, jdText, hasFormattingRisk, hasGraphicsRisk).risks();
    }


    // 1. ATS Compatibility (SCORING_RULES.md)
    private double calcAts(String resumeText, String jdText, List<String> jdKeywords,
                           boolean hasFormattingRisk, boolean hasGraphicsRisk,
                           List<RiskResponse> risks) {
        double score = 100.0;

        // Missing standard section headings (–8 per, max –24)
        String lower = resumeText.toLowerCase(Locale.ROOT);
        long missingSections = REQUIRED_SECTIONS.stream()
            .filter(s -> !lower.contains(s))
            .count();
        if (missingSections > 0) {
            double deduction = Math.min(missingSections * 8.0, 24.0);
            score -= deduction;
            recordRisk(risks, "MISSING_SECTIONS", deduction);
        }

        // Contact info absent (–10)
        if (!resumeParser.hasContactInfo(resumeText)) {
            score -= 10;
            recordRisk(risks, "NO_CONTACT_INFO", 10.0);
        }

        // Multi-column / table layout (–10)
        if (hasFormattingRisk) {
            score -= 10;
            recordRisk(risks, "MULTI_COLUMN_TABLE", 10.0);
        }

        // Graphics / non-standard fonts heuristic (–8)
        if (hasGraphicsRisk) {
            score -= 8;
            recordRisk(risks, "GRAPHICS_PRESENT", 8.0);
        }

        // Keyword density deduction (–15 or –8, not both)
        double matchPct = calcKeywordMatchPercent(resumeText, jdKeywords);
        if (matchPct < 0.30) {
            score -= 15;
            recordRisk(risks, "KEYWORD_DENSITY_BELOW_30", 15.0);
        } else if (matchPct < 0.50) {
            score -= 8;
            recordRisk(risks, "KEYWORD_DENSITY_30_TO_50", 8.0);
        }

        // No measurable achievements (–5)
        if (!hasMeasurableAchievements(resumeText)) {
            score -= 5;
            recordRisk(risks, "NO_MEASURABLE_ACHIEVEMENTS", 5.0);
        }

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
        String lower = block.toLowerCase(Locale.ROOT);

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
    private double calcWriting(String resumeText, List<RiskResponse> risks) {
        double score = 100.0;
        String[] sentences = resumeText.split("[.!?]+");

        // Average sentence length > 35 words (–10)
        double avgLen = Arrays.stream(sentences)
            .mapToLong(s -> Arrays.stream(s.split("\\s+")).filter(w -> !w.isBlank()).count())
            .average().orElse(0);
        if (avgLen > 35) {
            score -= 10;
            recordRisk(risks, "LONG_SENTENCES", 10.0);
        }

        // First-person pronouns (–15)
        if (FIRST_PERSON.matcher(resumeText).find()) {
            score -= 15;
            recordRisk(risks, "FIRST_PERSON", 15.0);
        }

        // Filler words (–3 per occurrence, max –15)
        String lower = resumeText.toLowerCase(Locale.ROOT);
        long fillerCount = FILLER_PHRASES.stream()
            .mapToLong(f -> countOccurrences(lower, f))
            .sum();
        if (fillerCount > 0) {
            double deduction = Math.min(fillerCount * 3.0, 15.0);
            score -= deduction;
            recordRisk(risks, "FILLER_WORDS", deduction);
        }

        // Repetition (–5 per over-repeated word, max –15)
        Map<String, Long> freq = Arrays.stream(TfIdfCalculator.tokenize(resumeText))
            .filter(w -> w.length() > 2 && !STOPWORDS.contains(w))
            .collect(Collectors.groupingBy(w -> w, Collectors.counting()));
        long repeatedWordCount = freq.values().stream().filter(v -> v > 5).count();
        if (repeatedWordCount > 0) {
            double deduction = Math.min(repeatedWordCount * 5.0, 15.0);
            score -= deduction;
            recordRisk(risks, "REPEATED_WORDS", deduction);
        }

        // Passive voice ratio > 40% (–10)
        long passiveCount = Arrays.stream(sentences)
            .filter(s -> PASSIVE_PATTERN.matcher(s).find())
            .count();
        if (sentences.length > 0 && (double) passiveCount / sentences.length > 0.40) {
            score -= 10;
            recordRisk(risks, "PASSIVE_VOICE", 10.0);
        }

        return Math.max(0.0, score);
    }

    // 6. Professionalism (SCORING_RULES.md)
    private static final Pattern DATE_ALPHA   = Pattern.compile("(?i)(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z]*\\s+\\d{4}");
    private static final Pattern DATE_NUMERIC = Pattern.compile("\\b(0?[1-9]|1[0-2])/\\d{4}\\b");
    private static final Pattern CASUAL_EMAIL = Pattern.compile("[a-z0-9._%+\\-]*(?:cool|swag|sexy|lol|xd|420|69|1337)[a-z0-9._%+\\-]*@", Pattern.CASE_INSENSITIVE);

    private double calcProfessionalism(String resumeText, List<RiskResponse> risks) {
        double score = 100.0;

        // Spelling: simple check - words with double-consonant errors or common typos
        // A full dictionary check would need a bundled word list.
        // Using a small targeted list of the most common resume misspellings.
        // Upgrade path: bundle a curated word list and use a trie lookup.
        long spellingErrors = countSpellingErrors(resumeText);
        if (spellingErrors > 0) {
            double deduction = Math.min(spellingErrors * 5.0, 20.0);
            score -= deduction;
            recordRisk(risks, "SPELLING_ERRORS", deduction);
        }

        // Inconsistent date formatting (–10)
        boolean hasAlpha   = DATE_ALPHA.matcher(resumeText).find();
        boolean hasNumeric = DATE_NUMERIC.matcher(resumeText).find();
        if (hasAlpha && hasNumeric) {
            score -= 10;
            recordRisk(risks, "INCONSISTENT_DATES", 10.0);
        }

        // Inconsistent bullet style (–5)
        boolean hasDash  = resumeText.contains("- ");
        boolean hasBullet = resumeText.contains("• ");
        boolean hasStar   = resumeText.contains("* ");
        if ((hasDash ? 1 : 0) + (hasBullet ? 1 : 0) + (hasStar ? 1 : 0) > 1) {
            score -= 5;
            recordRisk(risks, "INCONSISTENT_BULLETS", 5.0);
        }

        // Casual email address (–10)
        if (CASUAL_EMAIL.matcher(resumeText).find()) {
            score -= 10;
            recordRisk(risks, "CASUAL_EMAIL", 10.0);
        }

        // Resume length > 700 words estimated as > 2 pages (–5)
        long wordCount = Arrays.stream(resumeText.split("\\s+")).filter(w -> !w.isBlank()).count();
        if (wordCount > 700) {
            score -= 5;
            recordRisk(risks, "OVER_700_WORDS", 5.0);
        }

        return Math.max(0.0, score);
    }

    // Status label (SCORING_RULES.md & Status Label Thresholds)
    public static String statusLabel(double overallScore) {
        return switch (SectionScore.deriveStatus(overallScore)) {
            case SectionScore.EXCELLENT -> "Excellent Match";
            case SectionScore.STRONG    -> "Strong Match";
            case SectionScore.MODERATE  -> "Moderate Match";
            case SectionScore.WEAK      -> "Weak Match";
            default                     -> "Poor Match";
        };
    }

    // Helpers
    private boolean isMatchedInResume(String keyword, String resumeText) {
        String lower = resumeText.toLowerCase(Locale.ROOT);
        if (lower.contains(keyword.toLowerCase(Locale.ROOT))) return true;
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
        String lower = text.toLowerCase(Locale.ROOT);
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
        String lower = text.toLowerCase(Locale.ROOT);
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
        List<String> missingKeywords,
        List<RiskResponse> risks
    ) {
        public ScoreResult(
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
        ) {
            this(overallScore, atsScore, skillsScore, impactScore, projectScore, writingScore,
                 professionalismScore, statusLabel, keywordMatchPercent, matchedKeywords, missingKeywords, List.of());
        }
    }
}