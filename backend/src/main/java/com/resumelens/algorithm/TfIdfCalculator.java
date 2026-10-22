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

    // Generic JD filler terms that add noise to keyword ranking even with TF-IDF
    private static final Set<String> JD_FILLER = Set.of(
        "experience","team","work","ability","strong","required",
        "excellent","knowledge","skills","good","must","role",
        "position","candidate","year","years","including","within"
    );

    /** TF: raw count of term in document normalised by document length. */
    public Map<String, Double> termFrequency(String text) {
        String[] tokens = tokenize(text);
        Map<String, Double> tf = new HashMap<>();
        for (String token : tokens) {
            if (!STOPWORDS.contains(token)) {
                tf.merge(token, 1.0, Double::sum);
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
            terms.forEach(t -> df.merge(t, 1, Integer::sum));
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
     * Returns top N terms ranked by TF-IDF weight.
     * IDF is computed using sentences/lines of the input as a mini-corpus so that
     * terms ubiquitous across lines (generic words) receive a low IDF and are
     * down-weighted relative to distinctive, line-specific terms.
     * Generic JD filler terms are removed after ranking via JD_FILLER.
     */
    public List<String> topKeywords(String text, int topN) {
        if (text == null || text.isBlank()) return List.of();
        // Each non-blank sentence/line is one document for IDF
        // Split on: newline, period, !, ?, semicolon, bullet, en-dash, hyphen
        List<String> lines = Arrays.stream(text.split("[\\n.!?;•–-]+"))
                .map(String::trim)
                .filter(l -> !l.isBlank())
                .collect(Collectors.toList());
        if (lines.isEmpty()) lines = List.of(text);

        Map<String, Double> idfMap   = idf(lines);
        Map<String, Double> tfidfVec = tfidfVector(text, idfMap);

        return tfidfVec.entrySet().stream()
                .filter(e -> !JD_FILLER.contains(e.getKey()))
                .filter(e -> e.getValue() > 0.0)
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public static String[] tokenize(String text) {
        return text.toLowerCase()
                   .replaceAll("[^a-z0-9\\s]", " ")
                   .trim()
                   .split("\\s+");
    }
}