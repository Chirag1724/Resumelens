package com.resumelens.algorithm;
import org.springframework.stereotype.Component;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/**
 * Self-implemented Cosine Similarity and Levenshtein Distance.
 * No external NLP library.
 */
@Component
public class SimilarityCalculator {

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
     * O(n) space optimisation possible with two-row DP.
     * Upgrade path: replace 2D array with two alternating rows if memory matters.
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
}