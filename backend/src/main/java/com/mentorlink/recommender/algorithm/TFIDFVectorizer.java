package com.mentorlink.recommender.algorithm;

import java.util.*;

/**
 * TF-IDF vectorization for content-based recommendation.
 * TF = term_count / total_words
 * IDF = log(total_documents / documents_with_term)
 * TF-IDF = TF × IDF
 */
public final class TFIDFVectorizer {

    private TFIDFVectorizer() {}

    /**
     * Compute term frequency for a document.
     * TF(t) = count(t) / total_terms
     */
    public static Map<String, Double> computeTF(List<String> tokens) {
        Map<String, Double> tf = new HashMap<>();
        if (tokens == null || tokens.isEmpty()) return tf;

        for (String token : tokens) {
            tf.merge(token, 1.0, Double::sum);
        }
        int size = tokens.size();
        for (String token : tf.keySet()) {
            tf.put(token, tf.get(token) / size);
        }
        return tf;
    }

    /**
     * Compute IDF for each term across documents.
     * IDF(t) = log(1 + (N / df(t))) where N = total docs, df = doc frequency
     */
    public static Map<String, Double> computeIDF(List<List<String>> documents) {
        Map<String, Double> idf = new HashMap<>();
        if (documents == null || documents.isEmpty()) return idf;

        int N = documents.size();
        Map<String, Integer> docFreq = new HashMap<>();

        for (List<String> doc : documents) {
            Set<String> uniqueTerms = new HashSet<>(doc);
            for (String term : uniqueTerms) {
                docFreq.merge(term, 1, Integer::sum);
            }
        }

        for (Map.Entry<String, Integer> e : docFreq.entrySet()) {
            double idfVal = Math.log(1.0 + (double) N / e.getValue());
            idf.put(e.getKey(), idfVal);
        }
        return idf;
    }

    /**
     * Compute TF-IDF vector for a document using given IDF map.
     */
    public static Map<String, Double> computeTFIDF(List<String> tokens, Map<String, Double> idf) {
        Map<String, Double> tf = computeTF(tokens);
        Map<String, Double> tfidf = new HashMap<>();
        for (Map.Entry<String, Double> e : tf.entrySet()) {
            double idfVal = idf.getOrDefault(e.getKey(), 0.0);
            tfidf.put(e.getKey(), e.getValue() * idfVal);
        }
        return tfidf;
    }

    /**
     * Simplified: compute TF for a single document (for pairwise comparison).
     * Used when IDF is not critical (e.g. comparing two short texts).
     */
    public static Map<String, Double> computeTFOnly(List<String> tokens) {
        return computeTF(tokens);
    }
}
