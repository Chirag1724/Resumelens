package com.resumelens.algorithm;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Skill dictionary loaded once at startup from /skills.txt.
 * Immutable set of technical skills per SCORING_RULES.md.
 */
public final class SkillDictionary {

    private static final Set<String> SKILLS = loadSkills();
    private static final List<String> SKILLS_BY_LENGTH_DESC = sortSkillsByLengthDesc(SKILLS);

    private SkillDictionary() {
    }

    private static Set<String> loadSkills() {
        Set<String> set = new LinkedHashSet<>();
        try (InputStream is = SkillDictionary.class.getResourceAsStream("/skills.txt")) {
            if (is != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String trimmed = line.trim().toLowerCase(Locale.ROOT);
                        if (!trimmed.isBlank() && !trimmed.startsWith("#")) {
                            set.add(trimmed);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // fallback empty
        }
        return Collections.unmodifiableSet(set);
    }

    private static List<String> sortSkillsByLengthDesc(Set<String> skills) {
        List<String> list = new ArrayList<>(skills);
        list.sort((a, b) -> {
            int cmp = Integer.compare(b.length(), a.length());
            return cmp != 0 ? cmp : a.compareTo(b);
        });
        return Collections.unmodifiableList(list);
    }

    public static Set<String> getSkills() {
        return SKILLS;
    }

    public static List<String> getSkillsSortedByLengthDesc() {
        return SKILLS_BY_LENGTH_DESC;
    }

    public static boolean contains(String term) {
        if (term == null) return false;
        return SKILLS.contains(term.toLowerCase(Locale.ROOT).trim());
    }
}
