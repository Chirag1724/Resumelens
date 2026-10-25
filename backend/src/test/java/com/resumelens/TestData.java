package com.resumelens;

/**
 * Shared test constants.
 * All test classes read from here; never duplicate literals in individual
 * tests.
 */
public final class TestData {

    private TestData() {
    }

    // Auth
    public static final String USER_EMAIL = "alice@example.com";
    public static final String USER_PASSWORD = "Secure123!";
    public static final String USER_NAME = "Alice";

    public static final String USER2_EMAIL = "bob@example.com";
    public static final String USER2_PASSWORD = "Secure456!";
    public static final String USER2_NAME = "Bob";

    // JD texts used across algorithm tests
    /** Two unique technical keywords; easy to count. */
    public static final String JD_TWO_TERMS = "java python";
    /** Three unique technical keywords; used for 30-50% density tests. */
    public static final String JD_THREE_TERMS = "java rust golang";
    /**
     * Unrelated keywords not present in any GOOD_RESUME; used for <30% density
     * tests.
     */
    public static final String JD_FOREIGN_TERMS = "rust golang swift";

    // Resume texts used across algorithm tests
    /**
     * "Good" resume that passes every ATS, Writing, and Professionalism rule.
     * Contains: standard section headings, a measurable achievement, both
     * JD_TWO_TERMS keywords.
     */
    public static final String GOOD_RESUME = "EDUCATION\n" +
            "Bachelor of Computer Science\n" +
            "EXPERIENCE\n" +
            "Software Engineer who achieved 40% improvement using java and python tools\n" +
            "SKILLS\n" +
            "java python programming algorithms";

    /**
     * A minimal job description paragraph (no newlines) where semicolons separate
     * sentences.
     * Used to test that topKeywords splits on semicolons.
     */
    public static final String SINGLE_PARA_JD = "alpha beta beta beta foo; alpha baz qux quux; alpha grault garply waldo; "
            +
            "alpha plugh thud grunt; alpha zap zing zoom";

    // SuggestionEngine test data
    public static final String SUGGESTION_RESUME = "EXPERIENCE\n" +
            "responsible for managing the team\n" +
            "helped with deployment pipelines\n" +
            "SKILLS\n" +
            "java spring boot";

    public static final String SUGGESTION_JD = "java kubernetes docker react";

    public static final String BULLETS_WITH_MIXED_METRICS = "- managed the team and delivered projects\n" +
            "- increased revenue by 40%\n" +
            "- built the product from scratch";

    // PromptBuilderService test data
    public static final String PROMPT_RESUME_TEXT = "java developer experienced in java and python";
    public static final String PROMPT_JD_TEXT = "We need java python docker developers";
    public static final String PROMPT_JOB_TITLE = "Software Engineer";
    public static final String PROMPT_TEMPLATE = "Awesome-CV";
    public static final String PROMPT_MISSING_KW = "[\"docker\"]";

    // Integration test helpers
    /** Minimal valid JSON body for POST /api/v1/auth/register */
    public static String registerJson(String email, String password, String name) {
        return String.format(
                "{\"email\":\"%s\",\"password\":\"%s\",\"name\":\"%s\"}", email, password, name);
    }

    /** Minimal valid JSON body for POST /api/v1/auth/login */
    public static String loginJson(String email, String password) {
        return String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);
    }

    /** JSON body for POST /api/v1/analysis/match (raw JD variant) */
    public static String matchJson(Long resumeId, String rawJd, String jobTitle) {
        return String.format(
                "{\"resumeId\":%d,\"rawJobDescription\":\"%s\",\"jobTitle\":\"%s\"}",
                resumeId, rawJd, jobTitle);
    }

    public static String matchJson(java.util.UUID resumeId, String rawJd, String jobTitle) {
        return String.format(
                "{\"resumeId\":\"%s\",\"rawJobDescription\":\"%s\",\"jobTitle\":\"%s\"}",
                resumeId, rawJd, jobTitle);
    }


    /**
     * Generates a string of N unique tokens so that no single token appears more
     * than once.
     * Used to construct long sentences without triggering the word-repetition
     * penalty.
     */
    public static String uniqueTokens(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (i > 0)
                sb.append(' ');
            sb.append("tok").append(i);
        }
        return sb.toString();
    }
}
