package com.mentorlink.recommender.algorithm;

import java.util.*;

/**
 * NLP text preprocessing: lowercasing, tokenization, stopword removal.
 */
public final class TextPreprocessor {

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "a", "an", "the", "is", "in", "on", "of", "for", "to", "and", "or",
            "with", "by", "at", "from", "as", "into", "through", "during",
            "be", "been", "being", "have", "has", "had", "do", "does", "did",
            "will", "would", "could", "should", "may", "might", "can", "must",
            "this", "that", "these", "those", "it", "its", "they", "them"
    ));

    private TextPreprocessor() {}

    /**
     * Preprocess text: lowercase, tokenize, remove stopwords and short tokens.
     */
    public static List<String> preprocess(String text) {
        if (text == null || text.isBlank()) return List.of();

        String lower = text.toLowerCase().trim();
        String[] tokens = lower.split("\\W+");

        List<String> filtered = new ArrayList<>();
        for (String token : tokens) {
            if (token.length() > 2 && !STOP_WORDS.contains(token)) {
                filtered.add(token);
            }
        }
        return filtered;
    }

    /**
     * Build vocabulary from multiple documents.
     */
    public static Set<String> buildVocabulary(List<List<String>> documents) {
        Set<String> vocab = new HashSet<>();
        for (List<String> doc : documents) {
            vocab.addAll(doc);
        }
        return vocab;
    }
}
