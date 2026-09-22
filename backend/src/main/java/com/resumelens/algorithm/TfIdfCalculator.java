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
      Return top N terms by TF-IDF weight from a single document, using the document itself as the IDF corpus (single-doc baseline).
      Single-doc IDF is a simplification; for multi-doc corpora pass the shared idf map. Upgrade path: pass corpus idf externally.
     */
    public List<String> topKeywords(String text, int topN) {
        Map<String, Double> tf = termFrequency(text);
        return tf.entrySet().stream()
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