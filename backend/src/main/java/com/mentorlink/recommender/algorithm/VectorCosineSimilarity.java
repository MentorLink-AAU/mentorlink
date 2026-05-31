package com.mentorlink.recommender.algorithm;

import java.util.*;

/**
 * Cosine similarity between TF-IDF (or TF) vectors represented as Map&lt;String, Double&gt;.
 * cosine_similarity = (A · B) / (||A|| × ||B||)
 * Range: 0 (no similarity) to 1 (identical).
 */
public final class VectorCosineSimilarity {

    private VectorCosineSimilarity() {}

    /**
     * Compute cosine similarity between two term-weight vectors.
     */
    public static double cosineSimilarity(Map<String, Double> vec1, Map<String, Double> vec2) {
        if (vec1 == null || vec2 == null || vec1.isEmpty() || vec2.isEmpty()) {
            return 0.0;
        }

        Set<String> words = new HashSet<>();
        words.addAll(vec1.keySet());
        words.addAll(vec2.keySet());

        double dot = 0;
        double normA = 0;
        double normB = 0;

        for (String word : words) {
            double a = vec1.getOrDefault(word, 0.0);
            double b = vec2.getOrDefault(word, 0.0);
            dot += a * b;
            normA += a * a;
            normB += b * b;
        }

        double denom = Math.sqrt(normA) * Math.sqrt(normB);
        return denom == 0 ? 0 : Math.min(1.0, dot / denom);
    }
}
