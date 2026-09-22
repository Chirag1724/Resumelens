package com.resumelens.service;

import com.resumelens.algorithm.TfIdfCalculator;
import com.resumelens.algorithm.SimilarityCalculator;
import com.resumelens.dto.response.SuggestionResponse;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SuggestionEngine {

    private static final int LEVENSHTEIN_THRESHOLD = 2;

    private static final Map<String, String> WEAK_VERB_REPLACEMENTS = Map.of(
        "worked on",        "led / built / delivered",
        "helped with",      "co-led / contributed directly to",
        "responsible for",  "owned / managed / drove",
        "involved in",      "architected / implemented",
        "participated in",  "contributed to / spearheaded"
    );

    // Regex that finds a bullet point lacking any digit
    private static final Pattern BULLET_NO_NUMBER =
        Pattern.compile("^[-–•*]\\s+(?!.*\\d).+$", Pattern.MULTILINE);

    private final SimilarityCalculator similarity;

    public SuggestionEngine(SimilarityCalculator similarity) {
        this.similarity = similarity;
    }

    /** Keywords present in JD but absent from resume (exact or fuzzy). */
    public List<String> findMissingKeywords(String resumeText, String jdText) {
        List<String> jdTokens = List.of(TfIdfCalculator.tokenize(jdText));
        Set<String> resumeTokens = Set.of(TfIdfCalculator.tokenize(resumeText));
        return jdTokens.stream()
            .distinct()
            .filter(kw -> kw.length() > 2)
            .filter(kw -> resumeTokens.stream()
                .noneMatch(rt -> rt.equals(kw) || similarity.isFuzzyMatch(rt, kw, LEVENSHTEIN_THRESHOLD)))
            .collect(Collectors.toList());
    }

    /**
     * Returns lines containing weak verbs, paired with a suggested replacement.
     * Format: "Line text -> suggested replacement"
     */
    public List<String> detectWeakVerbs(String resumeText) {
        String lower = resumeText.toLowerCase();
        List<String> findings = new ArrayList<>();
        WEAK_VERB_REPLACEMENTS.forEach((weak, strong) -> {
            int idx = 0;
            while ((idx = lower.indexOf(weak, idx)) != -1) {
                // Extract surrounding context (up to 80 chars)
                int start = Math.max(0, idx - 10);
                int end   = Math.min(resumeText.length(), idx + weak.length() + 50);
                String context = resumeText.substring(start, end).trim().replaceAll("\\s+", " ");
                findings.add("\"..." + context + "...\" → replace \"" + weak + "\" with: " + strong);
                idx += weak.length();
            }
        });
        return findings;
    }

    /** Returns bullet-point lines that contain no numeric metric. */
    public List<String> checkQuantification(String resumeText) {
        return BULLET_NO_NUMBER.matcher(resumeText).results()
            .map(mr -> mr.group().trim())
            .collect(Collectors.toList());
    }

    /**
     * Assembles prioritised suggestions from all analysis signals.
     * HIGH   = missing keywords + weak verbs (most impactful on ATS score)
     * MEDIUM = unquantified bullets
     * LOW    = style / professionalism notes
     */
    public List<SuggestionResponse> generateTopSuggestions(
            List<String> missingKeywords,
            List<String> weakVerbLines,
            List<String> unquantifiedBullets,
            double overallScore) {

        List<SuggestionResponse> suggestions = new ArrayList<>();

        if (!missingKeywords.isEmpty()) {
            String topMissing = missingKeywords.stream().limit(5).collect(Collectors.joining(", "));
            suggestions.add(new SuggestionResponse(
                "HIGH",
                "Add missing JD keywords",
                "Your resume is missing " + missingKeywords.size() + " keywords from the job description. "
                    + "Incorporate them naturally where they genuinely apply.",
                "Consider adding: " + topMissing
            ));
        }

        if (!weakVerbLines.isEmpty()) {
            suggestions.add(new SuggestionResponse(
                "HIGH",
                "Replace weak action verbs",
                "Found " + weakVerbLines.size() + " instance(s) of weak or passive phrasing. "
                    + "Strong action verbs improve both readability and ATS scoring.",
                weakVerbLines.get(0)
            ));
        }

        if (!unquantifiedBullets.isEmpty()) {
            suggestions.add(new SuggestionResponse(
                "MEDIUM",
                "Quantify your achievements",
                unquantifiedBullets.size() + " bullet point(s) lack measurable outcomes. "
                    + "Adding numbers, percentages, or scale significantly increases impact score.",
                "E.g.: \"Reduced build time by 40%\" or \"Managed a team of 8 engineers\""
            ));
        }

        if (overallScore < 55) {
            suggestions.add(new SuggestionResponse(
                "LOW",
                "Review overall resume structure",
                "Your overall ATS score is below the Moderate Match threshold. "
                    + "Ensure standard section headings (Education, Experience, Skills) are present "
                    + "and contact information is clearly visible.",
                "Standard headings: EXPERIENCE, EDUCATION, SKILLS, PROJECTS"
            ));
        }

        return suggestions;
    }
}