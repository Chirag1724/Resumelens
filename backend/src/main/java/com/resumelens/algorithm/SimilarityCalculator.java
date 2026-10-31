package com.resumelens.algorithm;

import org.springframework.stereotype.Component;
import java.util.*;

/**
 * Self-implemented Cosine Similarity, Levenshtein Distance, and canonical keyword matching.
 * No external NLP library.
 */
@Component
public class SimilarityCalculator {

    public static final Map<String, Set<String>> SKILL_ALIASES = Map.ofEntries(
        Map.entry("sql", Set.of("mysql", "postgresql", "postgres", "sqlite", "mariadb", "mssql", "plsql", "tsql")),
        Map.entry("postgresql", Set.of("postgres")),
        Map.entry("postgres", Set.of("postgresql")),
        Map.entry("javascript", Set.of("js")),
        Map.entry("js", Set.of("javascript")),
        Map.entry("typescript", Set.of("ts")),
        Map.entry("ts", Set.of("typescript")),
        Map.entry("kubernetes", Set.of("k8s")),
        Map.entry("k8s", Set.of("kubernetes")),
        Map.entry("machinelearning", Set.of("ml")),
        Map.entry("ml", Set.of("machinelearning")),
        Map.entry("rest", Set.of("restful")),
        Map.entry("restful", Set.of("rest"))
    );

    /**
     * Cosine similarity between two TF-IDF vectors.
     * Aligns on the union of all terms from both vectors.
     * Returns a value in [0.0, 1.0].
     */
    public double cosineSimilarity(Map<String, Double> vectorA, Map<String, Double> vectorB) {
        if (vectorA.isEmpty() || vectorB.isEmpty()) return 0.0;

        Set<String> terms = new HashSet<>(vectorA.keySet());
        terms.addAll(vectorB.keySet());

        double dot = 0.0, magA = 0.0, magB = 0.0;
        for (String term : terms) {
            double a = vectorA.getOrDefault(term, 0.0);
            double b = vectorB.getOrDefault(term, 0.0);
            dot  += a * b;
            magA += a * a;
            magB += b * b;
        }
        if (magA == 0.0 || magB == 0.0) return 0.0;
        return dot / (Math.sqrt(magA) * Math.sqrt(magB));
    }

    /**
     * Levenshtein distance - dynamic programming, O(m*n) time and space.
     */
    public int levenshteinDistance(String a, String b) {
        int m = a.length(), n = b.length();
        int[][] dp = new int[m + 1][n + 1];
        for (int i = 0; i <= m; i++) dp[i][0] = i;
        for (int j = 0; j <= n; j++) dp[0][j] = j;
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j - 1],
                                   Math.min(dp[i - 1][j], dp[i][j - 1]));
                }
            }
        }
        return dp[m][n];
    }

    /** Returns true if the Levenshtein distance between a and b is <= threshold. */
    public boolean isFuzzyMatch(String a, String b, int threshold) {
        return levenshteinDistance(a.toLowerCase(Locale.ROOT), b.toLowerCase(Locale.ROOT)) <= threshold;
    }

    /**
     * Canonical form per SCORING_RULES.md:
     * Lowercase with Locale.ROOT.
     * Special cases for c++, c#, and .net before canonicalization so they stay distinguishable.
     * Remove every character that is not a letter or digit.
     * Strip trailing "js" when remaining length is more than 3.
     */
    public String canonicalize(String term) {
        if (term == null || term.isBlank()) return "";
        String s = term.toLowerCase(Locale.ROOT).trim();
        s = s.replaceAll("c\\+\\+", "cpp");
        s = s.replaceAll("c#", "csharp");
        s = s.replaceAll("(^|[^a-z0-9])\\.net($|[^a-z0-9])", "$1dotnet$2");
        s = s.replaceAll("[^a-z0-9]", "");
        if (s.endsWith("js") && s.length() > 3) {
            s = s.substring(0, s.length() - 2);
        }
        return s;
    }

    /**
     * Splits resume text into tokens preserving characters used in technical terms (+, #, ., -).
     * For every token that contains a hyphen, adds its parts as extra tokens in addition to the whole token.
     */
    public List<String> extractResumeTokens(String resumeText) {
        if (resumeText == null || resumeText.isBlank()) return List.of();
        String cleaned = resumeText.replaceAll("[,;:|/\\n\\r\\t(){}\\[\\]\"'`*•\\u2013\\u2014]", " ");
        List<String> result = new ArrayList<>();
        for (String t : cleaned.trim().split("\\s+")) {
            if (t.isBlank()) continue;
            result.add(t);
            if (t.contains("-")) {
                for (String part : t.split("-+")) {
                    if (!part.isBlank()) {
                        result.add(part);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Keyword matching per SCORING_RULES.md:
     * A JD keyword is matched when its canonical form equals the canonical form of a resume
     * token, or of two adjacent resume tokens joined together.
     * Fuzzy matching is allowed only when the canonical keyword has at least 6 characters
     * and the first two characters are equal: allowed edit distance is 1 for 6 to 8 characters
     * and 2 for 9 or more. Never fuzzy match shorter keywords (< 6 characters).
     */
    public boolean isKeywordMatched(String keyword, String resumeText) {
        if (keyword == null || keyword.isBlank() || resumeText == null || resumeText.isBlank()) {
            return false;
        }
        String canonicalKw = canonicalize(keyword);
        if (canonicalKw.isEmpty()) return false;

        List<String> rawTokens = extractResumeTokens(resumeText);
        if (rawTokens.isEmpty()) return false;

        int n = rawTokens.size();
        List<String> canonicalTokens = new ArrayList<>(n);
        for (String t : rawTokens) {
            canonicalTokens.add(canonicalize(t));
        }

        List<String> adjacentTokens = new ArrayList<>(Math.max(0, n - 1));
        for (int i = 0; i < n - 1; i++) {
            adjacentTokens.add(canonicalize(rawTokens.get(i) + " " + rawTokens.get(i + 1)));
        }

        // 1. Exact canonical match: single token or two adjacent tokens joined
        for (String ct : canonicalTokens) {
            if (canonicalKw.equals(ct)) return true;
        }
        for (String at : adjacentTokens) {
            if (canonicalKw.equals(at)) return true;
        }

        // 1b. Exact alias match: canonical equality only, no fuzzy matching
        Set<String> aliases = SKILL_ALIASES.get(canonicalKw);
        if (aliases != null) {
            for (String ct : canonicalTokens) {
                if (aliases.contains(ct)) return true;
            }
            for (String at : adjacentTokens) {
                if (aliases.contains(at)) return true;
            }
        }

        // 1c. Version suffix match: resume token with trailing digits matches keyword equal to token without digits
        for (String ct : canonicalTokens) {
            if (matchesVersionSuffix(canonicalKw, ct)) return true;
        }
        for (String at : adjacentTokens) {
            if (matchesVersionSuffix(canonicalKw, at)) return true;
        }

        // 2. Fuzzy match: allowed only for keywords with at least 6 characters
        int kwLen = canonicalKw.length();
        if (kwLen < 6) {
            return false;
        }

        int maxAllowedDistance = (kwLen <= 8) ? 1 : 2;

        // Check against single tokens
        for (String ct : canonicalTokens) {
            if (ct.length() >= 2 &&
                canonicalKw.charAt(0) == ct.charAt(0) &&
                canonicalKw.charAt(1) == ct.charAt(1)) {
                if (levenshteinDistance(canonicalKw, ct) <= maxAllowedDistance) {
                    return true;
                }
            }
        }

        // Check against adjacent tokens
        for (String at : adjacentTokens) {
            if (at.length() >= 2 &&
                canonicalKw.charAt(0) == at.charAt(0) &&
                canonicalKw.charAt(1) == at.charAt(1)) {
                if (levenshteinDistance(canonicalKw, at) <= maxAllowedDistance) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Resume token with trailing digits (html5, css3, es6, python3, vue3, angular2) matches
     * a keyword equal to the token without the digits, only when at least 3 letters remain.
     * Tokens such as s3, ec2 and mp3 are never stripped.
     * One way only: resume token to keyword.
     */
    private boolean matchesVersionSuffix(String canonicalKw, String resumeToken) {
        if (resumeToken == null || resumeToken.length() < 4) return false;
        int i = resumeToken.length() - 1;
        while (i >= 0 && Character.isDigit(resumeToken.charAt(i))) {
            i--;
        }
        if (i == resumeToken.length() - 1) {
            return false;
        }
        String stripped = resumeToken.substring(0, i + 1);
        if (stripped.length() >= 3 && stripped.chars().allMatch(Character::isLetter)) {
            return canonicalKw.equals(stripped);
        }
        return false;
    }
}