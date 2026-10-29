package com.resumelens.algorithm;

import org.springframework.stereotype.Component;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Self-implemented TF-IDF calculator.
 * No external NLP library - per ARCHITECTURE.md and PROJECT_CONTEXT.md.
 */
@Component
public class TfIdfCalculator {

    private static final Set<String> STOPWORDS = Set.of(
        "a","an","the","and","or","but","in","on","at","to","for","of","with",
        "by","from","is","are","was","were","be","been","being","have","has",
        "had","do","does","did","will","would","could","should","may","might",
        "it","its","this","that","these","those","i","me","my","we","our",
        "you","your","he","she","they","their","not","no","as","up","out"
    );

    /**
     * Generic job description and resume words that must never become keywords.
     * Contains over 150 words per SCORING_RULES.md.
     */
    public static final Set<String> GENERIC_WORDS = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
        // Explicitly mandated terms
        "responsibilities", "requirements", "requirement", "hiring", "apply", "large",
        "using", "related", "strong", "experience", "team", "work", "ability",
        "knowledge", "looking", "join", "role", "candidate", "years", "production",
        // Additional common verbs and verb forms
        "collaborate", "collaborating", "collaboration", "collaborative",
        "communicate", "communicating", "communication", "communications",
        "lead", "leading", "leads", "leader", "leadership",
        "manage", "managing", "manager", "management",
        "develop", "developing", "developed", "developer", "development",
        "create", "creating", "created", "creation",
        "build", "building", "built", "builder",
        "design", "designing", "designed", "designer",
        "deliver", "delivering", "delivered", "delivery",
        "maintain", "maintaining", "maintained", "maintenance",
        "support", "supporting", "supported",
        "ensure", "ensuring", "ensured",
        "help", "helping", "helped",
        "assist", "assisting", "assisted",
        "provide", "providing", "provided",
        "require", "requiring", "required",
        "prefer", "preferring", "preferred",
        "include", "including", "included", "includes",
        "perform", "performing", "performed",
        "participate", "participating", "participated",
        "contribute", "contributing", "contributed",
        "drive", "driving", "driven",
        "improve", "improving", "improved", "improvement",
        "increase", "increasing", "increased",
        "reduce", "reducing", "reduced",
        "achieve", "achieving", "achieved",
        "learn", "learning", "learned",
        "train", "training", "trained",
        "grow", "growing", "growth",
        "understand", "understanding",
        "implement", "implementing", "implemented", "implementation",
        "operate", "operating", "operation", "operations",
        "execute", "executing", "execution",
        "focus", "focusing", "focused",
        "solve", "solving", "solution", "solutions",
        "coordinate", "coordinating", "coordinated",
        "track", "tracking", "tracked",
        "deploy", "deploying", "deployment",
        "test", "testing", "tested",
        "write", "writing", "written",
        // Common adjectives and adverbs
        "excellent", "good", "great", "best", "high", "highly", "well",
        "proven", "solid", "deep", "broad", "wide", "fast", "paced",
        "dynamic", "passionate", "motivated", "enthusiastic", "dedicated",
        "successful", "successfully", "effective", "effectively",
        "innovative", "creative", "exceptional", "outstanding",
        "proficient", "proficiency", "skilled", "skills", "skill", "qualify", "qualified", "qualification", "qualifications",
        "flexible", "detail", "oriented", "hands", "ideal", "critical", "key",
        "senior", "junior", "mid", "entry", "principal", "staff",
        "full", "time", "part", "contract", "remote", "hybrid", "onsite",
        // Common nouns and filler
        "position", "positions", "job", "jobs", "career", "careers",
        "company", "business", "client", "clients", "customer", "customers",
        "project", "projects", "product", "products", "program", "programs",
        "opportunity", "opportunities", "environment", "industry", "organization",
        "degree", "bachelor", "bachelors", "master", "masters", "phd",
        "education", "background", "field", "area", "discipline",
        "professional", "professionals", "individual", "individuals", "person", "people",
        "member", "members", "partner", "partners", "stakeholder", "stakeholders",
        "daily", "weekly", "monthly", "quarterly", "annual",
        "year", "month", "months", "day", "days",
        "level", "range", "scope", "scale", "aspect", "aspects",
        "need", "needs", "must", "plus", "nice", "have", "within", "across",
        "models", "model", "data",
        // Keyword noise additions
        "engineer", "engineers", "practices", "practice", "evaluate",
        "techniques", "technique", "science", "apache", "computer science"
    )));

    public static final Set<String> JD_FILLER = GENERIC_WORDS;

    /** TF: raw count of term in document normalised by document length. */
    public Map<String, Double> termFrequency(String text) {
        String[] tokens = tokenize(text);
        Map<String, Double> tf = new HashMap<>();
        for (String token : tokens) {
            if (!STOPWORDS.contains(token)) {
                tf.merge(token, 1.0, (a, b) -> a + b);
            }
        }
        // Normalise by document length
        int total = tokens.length;
        if (total > 0) tf.replaceAll((k, v) -> v / total);
        return tf;
    }

    /**
     * IDF across a list of documents.
     * IDF(t) = log((1 + N) / (1 + df(t))) + 1  (smoothed, sklearn-style)
     */
    public Map<String, Double> idf(List<String> documents) {
        int n = documents.size();
        Map<String, Integer> df = new HashMap<>();
        for (String doc : documents) {
            Set<String> terms = Arrays.stream(tokenize(doc))
                    .filter(t -> !STOPWORDS.contains(t))
                    .collect(Collectors.toSet());
            terms.forEach(t -> df.merge(t, 1, (a, b) -> a + b));
        }
        Map<String, Double> idfMap = new HashMap<>();
        df.forEach((term, count) ->
            idfMap.put(term, Math.log((1.0 + n) / (1.0 + count)) + 1.0)
        );
        return idfMap;
    }

    /**
     * TF-IDF vector for a document given a pre-computed IDF map.
     * Reuse idf() across the resume+JD pair for consistency.
     */
    public Map<String, Double> tfidfVector(String text, Map<String, Double> idfMap) {
        Map<String, Double> tf = termFrequency(text);
        Map<String, Double> result = new HashMap<>();
        tf.forEach((term, tfVal) -> {
            double idfVal = idfMap.getOrDefault(term, 0.0);
            result.put(term, tfVal * idfVal);
        });
        return result;
    }

    /**
     * Returns top N terms ranked for job description keyword extraction (max 30).
     * 1. First take every JD term that is in the skill dictionary, including multiword
     *    dictionary terms matched as a phrase, in order of occurrence.
     * 2. Fill remaining slots with the highest TF-IDF terms using the sentence-level
     *    corpus already in place, excluding the generic set and tokens shorter than 3 characters.
     * 3. Keeps multiword skills as one keyword (e.g. deep learning is not split into deep and learning).
     */
    public List<String> topKeywords(String text, int topN) {
        if (text == null || text.isBlank() || topN <= 0) return List.of();

        // 1. Extract terms in the skill dictionary, including multiword phrases
        List<String> extractedSkills = extractSkillsFromText(text);

        if (extractedSkills.size() >= topN) {
            return extractedSkills.subList(0, topN);
        }

        // 2. Sentence-level IDF corpus for remaining slots
        List<String> lines = new ArrayList<>();
        for (String segment : text.split("[\\n.!?;•\u2013-]+")) {
            String trimmed = segment.trim();
            if (!trimmed.isBlank()) {
                lines.add(trimmed);
            }
        }
        if (lines.isEmpty()) lines = List.of(text);

        Map<String, Double> idfMap = idf(lines);
        Map<String, Double> tfidfVec = tfidfVector(text, idfMap);

        // Keep multiword skill as one keyword: do not split or add component words
        Set<String> wordsInMultiwordSkills = new HashSet<>();
        for (String skill : extractedSkills) {
            if (skill.contains(" ") || skill.contains("-")) {
                for (String part : skill.split("[\\s\\-]+")) {
                    if (!part.isBlank()) {
                        wordsInMultiwordSkills.add(part.toLowerCase(Locale.ROOT));
                    }
                }
            }
        }

        Set<String> alreadyExtracted = new HashSet<>(extractedSkills);
        List<String> result = new ArrayList<>(extractedSkills);

        List<Map.Entry<String, Double>> entries = new ArrayList<>(tfidfVec.entrySet());
        entries.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        for (Map.Entry<String, Double> entry : entries) {
            String term = entry.getKey();
            if (term.length() < 3) continue;
            if (GENERIC_WORDS.contains(term)) continue;
            if (alreadyExtracted.contains(term)) continue;
            if (wordsInMultiwordSkills.contains(term)) continue;
            if (entry.getValue() <= 0.0) continue;

            if (result.size() >= topN) break;
            result.add(term);
        }

        return Collections.unmodifiableList(result);
    }

    private List<String> extractSkillsFromText(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        List<SpanMatch> spans = new ArrayList<>();

        for (String skill : SkillDictionary.getSkillsSortedByLengthDesc()) {
            if (GENERIC_WORDS.contains(skill)) continue;
            int fromIdx = 0;
            while (fromIdx < lower.length()) {
                int idx = lower.indexOf(skill, fromIdx);
                if (idx == -1) break;
                int endIdx = idx + skill.length();

                boolean validBefore = (idx == 0) || !Character.isLetterOrDigit(lower.charAt(idx - 1));
                if (validBefore && skill.equals("net") && idx > 0 && lower.charAt(idx - 1) == '.') {
                    validBefore = false;
                }
                if (validBefore && skill.equals("c") && idx > 0 && (lower.charAt(idx - 1) == '+' || lower.charAt(idx - 1) == '#')) {
                    validBefore = false;
                }

                boolean validAfter = (endIdx == lower.length()) || !Character.isLetterOrDigit(lower.charAt(endIdx));
                if (validAfter && skill.equals("c") && endIdx < lower.length() && (lower.charAt(endIdx) == '+' || lower.charAt(endIdx) == '#')) {
                    validAfter = false;
                }
                if (validAfter && skill.endsWith("+") && endIdx < lower.length() && lower.charAt(endIdx) == '+') {
                    validAfter = false;
                }

                if (validBefore && validAfter) {
                    boolean overlaps = false;
                    for (SpanMatch sm : spans) {
                        if (idx < sm.end && endIdx > sm.start) {
                            overlaps = true;
                            break;
                        }
                    }
                    if (!overlaps) {
                        spans.add(new SpanMatch(idx, endIdx, skill));
                    }
                }
                fromIdx = idx + 1;
            }
        }

        spans.sort(Comparator.comparingInt(sm -> sm.start));
        Set<String> seen = new LinkedHashSet<>();
        for (SpanMatch sm : spans) {
            seen.add(sm.skill);
        }
        return new ArrayList<>(seen);
    }

    private record SpanMatch(int start, int end, String skill) {}

    public static String[] tokenize(String text) {
        return text.toLowerCase(Locale.ROOT)
                   .replaceAll("[^a-z0-9\\s]", " ")
                   .trim()
                   .split("\\s+");
    }
}