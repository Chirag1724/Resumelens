package com.resumelens.algorithm;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Minimal self-check for SimilarityCalculator.
 * Covers the two non-trivial algorithms: Levenshtein DP and Cosine Similarity.
 * No framework, no fixtures — per Ponytail rules.
 */
class SimilarityCalculatorTest {

    private final SimilarityCalculator calc = new SimilarityCalculator();

    // Levenshtein

    @Test
    void levenshteinIdenticalStrings() {
        assertEquals(0, calc.levenshteinDistance("java", "java"));
    }

    @Test
    void levenshteinOneInsertion() {
        assertEquals(1, calc.levenshteinDistance("java", "javaa"));
    }

    @Test
    void levenshteinOneDeletion() {
        assertEquals(1, calc.levenshteinDistance("javascript", "javascrip"));
    }

    @Test
    void levenshteinTypicalSkillVariant() {
        // "javascript" vs "Javascript" — after lowercasing, distance = 0
        assertEquals(0, calc.levenshteinDistance("javascript", "javascript"));
    }

    @Test
    void levenshteinCaseDifferenceViaFuzzyMatch() {
        // isFuzzyMatch lowercases both sides
        assertTrue(calc.isFuzzyMatch("JavaScript", "javascript", 2));
    }

    @Test
    void levenshteinCompletelyDifferent() {
        // Distance between "java" and "python" should be > 2
        assertTrue(calc.levenshteinDistance("java", "python") > 2);
    }

    // Cosine Similarity

    @Test
    void cosineIdenticalVectors() {
        var v = java.util.Map.of("java", 0.5, "spring", 0.3);
        assertEquals(1.0, calc.cosineSimilarity(v, v), 1e-9);
    }

    @Test
    void cosineOrthogonalVectors() {
        var vA = java.util.Map.of("java", 1.0);
        var vB = java.util.Map.of("python", 1.0);
        assertEquals(0.0, calc.cosineSimilarity(vA, vB), 1e-9);
    }

    @Test
    void cosinePartialOverlap() {
        var vA = java.util.Map.of("java", 1.0, "spring", 1.0);
        var vB = java.util.Map.of("java", 1.0, "django", 1.0);
        double result = calc.cosineSimilarity(vA, vB);
        assertTrue(result > 0.0 && result < 1.0, "Expected partial overlap score between 0 and 1");
    }

    @Test
    void cosineEmptyVectorReturnsZero() {
        assertEquals(0.0, calc.cosineSimilarity(java.util.Map.of(), java.util.Map.of("java", 1.0)));
    }
}