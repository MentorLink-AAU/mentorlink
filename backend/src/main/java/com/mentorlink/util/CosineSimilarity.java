package com.mentorlink.util;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Cosine similarity for skill-based student grouping and faculty-student matching.
 * Groups are formed by comparing skills – students with similar skills/department/year are grouped together.
 */
public final class CosineSimilarity {

    private CosineSimilarity() {}

    /**
     * Compute cosine similarity between two skill vectors (0.0 to 1.0).
     */
    public static double similarity(Set<String> skills1, Set<String> skills2) {
        if (skills1 == null || skills2 == null) return 0.0;
        if (skills1.isEmpty() && skills2.isEmpty()) return 1.0;  // both empty = max similarity
        if (skills1.isEmpty() || skills2.isEmpty()) return 0.0;
        Set<String> allTerms = new HashSet<>(skills1);
        allTerms.addAll(skills2);

        double[] v1 = toVector(skills1, allTerms);
        double[] v2 = toVector(skills2, allTerms);

        double dot = 0, norm1 = 0, norm2 = 0;
        for (int i = 0; i < v1.length; i++) {
            dot += v1[i] * v2[i];
            norm1 += v1[i] * v1[i];
            norm2 += v2[i] * v2[i];
        }
        double denom = Math.sqrt(norm1) * Math.sqrt(norm2);
        return denom == 0 ? 0 : dot / denom;
    }

    private static double[] toVector(Set<String> skills, Set<String> vocabulary) {
        List<String> sorted = new ArrayList<>(vocabulary);
        Collections.sort(sorted);
        double[] v = new double[sorted.size()];
        int i = 0;
        for (String term : sorted) {
            v[i++] = skills.contains(term) ? 1.0 : 0.0;
        }
        return v;
    }

    /**
     * Group students by SKILL SIMILARITY using cosine similarity.
     * Starts with the most similar pair (not serial order), then greedily adds the most similar student.
     * When all profiles are empty, shuffles to avoid deterministic serial grouping.
     */
    public static List<Set<Long>> clusterBySimilarity(
            Map<Long, Set<String>> studentSkills,
            int minGroupSize,
            int maxGroupSize
    ) {
        List<Long> students = new ArrayList<>(studentSkills.keySet());
        if (students.isEmpty()) return List.of();

        // Shuffle to break serial order when similarities are tied (e.g. all empty profiles)
        Collections.shuffle(students, ThreadLocalRandom.current());

        Set<Long> assigned = new HashSet<>();
        List<Set<Long>> groups = new ArrayList<>();

        while (true) {
            // Find the best seed: the unassigned pair with HIGHEST similarity (or first if all tied)
            Long seedA = null;
            Long seedB = null;
            double bestPairSim = -1;

            for (int i = 0; i < students.size(); i++) {
                Long a = students.get(i);
                if (assigned.contains(a)) continue;
                for (int j = i + 1; j < students.size(); j++) {
                    Long b = students.get(j);
                    if (assigned.contains(b)) continue;
                    double sim = similarity(
                            studentSkills.getOrDefault(a, Set.of()),
                            studentSkills.getOrDefault(b, Set.of()));
                    if (sim > bestPairSim) {
                        bestPairSim = sim;
                        seedA = a;
                        seedB = b;
                    }
                }
            }

            if (seedA == null) break;  // no unassigned left

            Set<Long> group = new HashSet<>();
            group.add(seedA);
            assigned.add(seedA);
            if (seedB != null) {
                group.add(seedB);
                assigned.add(seedB);
            }

            // Greedily add the most similar unassigned student to the group (by cosine similarity)
            while (group.size() < maxGroupSize) {
                Long best = null;
                double bestAvgSim = -1;

                for (Long other : students) {
                    if (assigned.contains(other)) continue;
                    double avgSim = avgSimilarityToGroup(other, group, studentSkills);
                    if (avgSim > bestAvgSim) {
                        bestAvgSim = avgSim;
                        best = other;
                    }
                }
                if (best == null) break;
                group.add(best);
                assigned.add(best);
            }

            if (group.size() >= minGroupSize) {
                groups.add(group);
            }
        }

        // Handle remaining unassigned (singletons or small groups)
        for (Long s : students) {
            if (!assigned.contains(s)) {
                groups.add(Set.of(s));
            }
        }

        return groups;
    }

    private static double avgSimilarityToGroup(Long student, Set<Long> group, Map<Long, Set<String>> skills) {
        Set<String> sSkills = skills.getOrDefault(student, Set.of());
        double sum = 0;
        int count = 0;
        for (Long g : group) {
            sum += similarity(sSkills, skills.getOrDefault(g, Set.of()));
            count++;
        }
        return count == 0 ? 0 : sum / count;
    }
}
