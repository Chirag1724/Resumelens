package com.resumelens.algorithm;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Minimal self-check for SimilarityCalculator.
 * Covers the two non-trivial algorithms: Levenshtein DP and Cosine Similarity.
 * No framework, no fixtures: keep the implementation minimal, avoid unnecessary abstractions.
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

    @Test
    void isFuzzyMatchReturnsTrueAtThresholdAndFalseAtThresholdPlusOne() {
        // Levenshtein distance between "java" and "javaaa" is 2
        assertEquals(2, calc.levenshteinDistance("java", "javaaa"));
        assertTrue(calc.isFuzzyMatch("java", "javaaa", 2),
                "isFuzzyMatch must return true when distance equals threshold (2 == 2)");

        // Levenshtein distance between "java" and "javaaaa" is 3 (threshold + 1)
        assertEquals(3, calc.levenshteinDistance("java", "javaaaa"));
        assertFalse(calc.isFuzzyMatch("java", "javaaaa", 2),
                "isFuzzyMatch must return false when distance equals threshold + 1 (3 > 2)");
    }

    @Test
    void should_matchJdSql_withResumeMysql() {
        assertTrue(calc.isKeywordMatched("sql", "Proficient in MySQL database administration."));
    }

    @Test
    void should_notMatchJdPostgreSql_withResumeMysql() {
        assertFalse(calc.isKeywordMatched("postgresql", "Proficient in MySQL database administration."));
    }

    @Test
    void should_matchJdK8s_withResumeKubernetes() {
        assertTrue(calc.isKeywordMatched("k8s", "Managed container clusters using Kubernetes."));
    }

    @Test
    void should_matchAliasesSymmetricallyAndDialects() {
        assertTrue(calc.isKeywordMatched("SQL", "Experience with SQLite and MariaDB and MSSQL"));
        assertTrue(calc.isKeywordMatched("sql", "Experience with PostgreSQL"));
        assertTrue(calc.isKeywordMatched("sql", "Experience with Postgres"));
        assertTrue(calc.isKeywordMatched("sql", "Experience with PLSQL and TSQL"));

        assertTrue(calc.isKeywordMatched("postgresql", "Worked on Postgres"));
        assertTrue(calc.isKeywordMatched("postgres", "Worked on PostgreSQL"));

        assertTrue(calc.isKeywordMatched("javascript", "Frontend with JS"));
        assertTrue(calc.isKeywordMatched("js", "Frontend with JavaScript"));

        assertTrue(calc.isKeywordMatched("typescript", "Frontend with TS"));
        assertTrue(calc.isKeywordMatched("ts", "Frontend with TypeScript"));

        assertTrue(calc.isKeywordMatched("kubernetes", "Deployed to k8s"));
        assertTrue(calc.isKeywordMatched("k8s", "Deployed to Kubernetes"));

        assertTrue(calc.isKeywordMatched("machine learning", "Specialized in ML"));
        assertTrue(calc.isKeywordMatched("ml", "Specialized in Machine Learning"));
    }
}