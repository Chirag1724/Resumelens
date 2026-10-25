package com.resumelens.service;

import com.resumelens.entity.MatchResult;
import com.resumelens.entity.SectionScore;
import com.resumelens.exception.ResourceNotFoundException;
import com.resumelens.repository.MatchResultRepository;
import com.resumelens.repository.SectionScoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import java.util.UUID;

/**
 * Builds the copyable AI-improvement prompt.
 * The system never calls an AI API - per PROJECT_CONTEXT.md & Architectural Principles.
 * This method performs pure string assembly only.
 */
@Service
public class PromptBuilderService {

    private final MatchResultRepository matchResultRepository;
    private final SectionScoreRepository sectionScoreRepository;

    public PromptBuilderService(MatchResultRepository matchResultRepository,
                                SectionScoreRepository sectionScoreRepository) {
        this.matchResultRepository = matchResultRepository;
        this.sectionScoreRepository = sectionScoreRepository;
    }

    /**
     * Assembles a complete, self-contained prompt the user can paste into an
     * external AI assistant. templateStyle controls the LaTeX template preference.
     * @param matchResultPublicId the persisted analysis result public UUID
     * @param templateStyle one of: "Awesome-CV", "Deedy Resume", "Simple Single-Column"
     * @return the assembled prompt string
     */
    @Transactional(readOnly = true)
    public String buildFixPrompt(UUID matchResultPublicId, String templateStyle) {
        MatchResult result = matchResultRepository.findByPublicId(matchResultPublicId)
            .orElseThrow(() -> new ResourceNotFoundException("MatchResult", matchResultPublicId));

        List<SectionScore> sections = sectionScoreRepository.findByMatchResultPublicId(matchResultPublicId);

        List<SectionScore> weakSections = sections.stream()
            .sorted(Comparator.comparingDouble(SectionScore::getScore))
            .limit(3)
            .collect(Collectors.toList());

        String style = (templateStyle != null && !templateStyle.isBlank())
            ? templateStyle : "Simple Single-Column";

        String resumeText = result.getResume().getRawText();
        String jdText     = result.getJobDescription().getDescription();
        String jobTitle   = result.getJobDescription().getTitle();
        String missingKws = extractMissingFromSections(sections);

        return buildPromptString(resumeText, jdText, jobTitle,
                                 result.getOverallScore(), missingKws,
                                 weakSections, style);
    }

    // Private assembly
    private String buildPromptString(String resumeText, String jdText, String jobTitle,
                                     double atsScore, String missingKeywords,
                                     List<SectionScore> weakSections, String templateStyle) {
        return """
                You are an expert resume writer and LaTeX developer with deep knowledge of ATS systems
                and hiring practices across the technology industry. You will rewrite the resume below
                to maximise its ATS compatibility for the target job description provided.
                Do not ask any clarifying questions. Make reasonable professional assumptions wherever
                information is ambiguous and proceed directly to producing the improved resume.

                ══════════════════════════════════════════
                TARGET JOB TITLE
                ══════════════════════════════════════════
                """ + jobTitle + """


                ══════════════════════════════════════════
                CURRENT ATS COMPATIBILITY SCORE
                ══════════════════════════════════════════
                """ + atsScore + " / 100" + """


                ══════════════════════════════════════════
                ORIGINAL RESUME
                ══════════════════════════════════════════
                """ + resumeText + """


                ══════════════════════════════════════════
                TARGET JOB DESCRIPTION
                ══════════════════════════════════════════
                """ + jdText + """


                ══════════════════════════════════════════
                MISSING KEYWORDS (from JD, absent in resume)
                ══════════════════════════════════════════
                """ + missingKeywords + """


                ══════════════════════════════════════════
                WEAKEST SCORING SECTIONS
                ══════════════════════════════════════════
                """ + formatWeakSections(weakSections) + """


                ══════════════════════════════════════════
                TEMPLATE PREFERENCE
                ══════════════════════════════════════════
                Please produce the rewritten resume using the following LaTeX template style: \
                """ + templateStyle + """
                (Options: Awesome-CV, Deedy Resume, Simple Single-Column)

                ══════════════════════════════════════════
                STRICT RULES - YOU MUST FOLLOW THESE
                ══════════════════════════════════════════
                1. Do NOT fabricate any experience, project, skill, or achievement not present in the original resume. Only rephrase and restructure truthful content.
                2. You MAY incorporate missing keywords listed above, but only in places where they genuinely and accurately describe something already in the resume.
                3. You MAY quantify achievements, but only using numbers that appear somewhere in the original resume text. Never invent statistics.
                4. Improve action verbs and remove first-person pronouns throughout.
                5. Ensure standard section headings are present: EXPERIENCE, EDUCATION, SKILLS, PROJECTS (if applicable), CERTIFICATIONS (if applicable).
                6. Ensure contact information is clearly present at the top.
                7. Target a single-page resume unless the candidate has more than 8 years of demonstrated experience.

                ══════════════════════════════════════════
                OUTPUT FORMAT
                ══════════════════════════════════════════
                Produce ONLY complete, compilable LaTeX code.
                Start from \\documentclass and end at \\end{document}.
                Do not include any explanation, commentary, or markdown before or after the code.
                """;
    }

    private String formatWeakSections(List<SectionScore> sections) {
        if (sections.isEmpty()) return "None";
        StringBuilder sb = new StringBuilder();
        sections.forEach(s -> sb.append("- ")
            .append(s.getSectionName())
            .append(" (score: ").append(s.getScore()).append(")")
            .append(s.getFeedback() != null ? " - " + s.getFeedback() : "")
            .append('\n'));
        return sb.toString();
    }

    private String extractMissingFromSections(List<SectionScore> sections) {
        return sections.stream()
            .filter(s -> s.getMissingKeywords() != null && !s.getMissingKeywords().isBlank())
            .map(SectionScore::getMissingKeywords)
            .findFirst()
            .orElse("(none detected)");
    }
}