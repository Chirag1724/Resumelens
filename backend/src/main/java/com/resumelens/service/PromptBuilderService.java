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
     * Supported options:
     * - "Jake's Resume (Overleaf Single-Column)"
     * - "Rezume (Developer Clean & Clickable)"
     * - "Audric Minimalist (Tech Stack Header)"
     * - "Simple Single-Column"
     * - "Awesome-CV"
     * - "Deedy Resume"
     *
     * @param matchResultPublicId the persisted analysis result public UUID
     * @param templateStyle chosen template identifier
     * @return the assembled prompt string
     */
    @Transactional(readOnly = true)
    public String buildFixPrompt(UUID matchResultPublicId, String templateStyle) {
        MatchResult result = matchResultRepository.findByPublicId(matchResultPublicId)
            .orElseThrow(() -> new ResourceNotFoundException("MatchResult", matchResultPublicId));

        List<SectionScore> sections = sectionScoreRepository.findByMatchResultPublicId(matchResultPublicId);

        List<SectionScore> weakSections = sections.stream()
            .sorted(Comparator.comparingDouble(s -> s.getScore()))
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

    private String buildPromptString(String resumeText, String jdText, String jobTitle,
                                     double atsScore, String missingKeywords,
                                     List<SectionScore> weakSections, String templateStyle) {
        String templateGuideline = resolveTemplateGuideline(templateStyle);

        return """
                You are an expert resume writer and LaTeX developer with deep knowledge of ATS systems
                and hiring practices across the technology industry. You will rewrite the resume below
                to maximise its ATS compatibility for the target job description provided.
                Do not ask any clarifying questions. Make reasonable professional assumptions wherever
                information is ambiguous and proceed directly to producing the improved resume.

                ==========================================
                TARGET JOB TITLE
                ==========================================
                """ + jobTitle + """


                ==========================================
                CURRENT ATS COMPATIBILITY SCORE
                ==========================================
                """ + atsScore + " / 100" + """


                ==========================================
                ORIGINAL RESUME
                ==========================================
                """ + resumeText + """


                ==========================================
                TARGET JOB DESCRIPTION
                ==========================================
                """ + jdText + """


                ==========================================
                MISSING KEYWORDS (from JD, absent in resume)
                ==========================================
                """ + missingKeywords + """


                ==========================================
                WEAKEST SCORING SECTIONS
                ==========================================
                """ + formatWeakSections(weakSections) + """


                ==========================================
                TEMPLATE SPECIFICATION & PREAMBLE
                ==========================================
                Template: """ + templateStyle + """

                """ + templateGuideline + """


                ==========================================
                STRICT RULES - YOU MUST FOLLOW THESE
                ==========================================
                1. Do NOT fabricate any experience, project, skill, or achievement not present in the original resume. Only rephrase and restructure truthful content.
                2. You MAY incorporate missing keywords listed above, but only in places where they genuinely and accurately describe something already in the resume.
                3. You MAY quantify achievements, but only using numbers that appear somewhere in the original resume text. Never invent statistics.
                4. Improve action verbs and remove first-person pronouns throughout.
                5. Ensure standard section headings are present: EXPERIENCE, EDUCATION, SKILLS, PROJECTS (if applicable), CERTIFICATIONS (if applicable).
                6. Ensure contact information is clearly present at the top.
                7. Target a single-page resume unless the candidate has more than 8 years of demonstrated experience.
                8. MANDATORY CLICKABLE HYPERLINKS:
                   - You MUST include \\usepackage[hidelinks]{hyperref} and \\pdfgentounicode=1 in the preamble for ATS machine-readability.
                   - Every contact detail (Email, LinkedIn, GitHub, LeetCode, Portfolio, personal website) MUST be wrapped in \\href{URL}{Anchor Text}.
                   - Every project repository URL, live deployment link, or publication link MUST be wrapped in \\href{URL}{Anchor Text}.
                   - When compiled on Overleaf or pdflatex, all links in the generated PDF document must be fully interactive and clickable.

                ==========================================
                OUTPUT FORMAT
                ==========================================
                Produce ONLY complete, compilable LaTeX code.
                Start from \\documentclass and end at \\end{document}.
                Do not include any explanation, commentary, or markdown before or after the code.
                """;
    }

    private String resolveTemplateGuideline(String templateStyle) {
        String lower = (templateStyle != null) ? templateStyle.toLowerCase() : "";

        if (lower.contains("rezume") || lower.contains("nanu")) {
            return """
                   Use the Rezume (by Nanu Panchamurthy) developer template structure:
                   - Preamble packages: \\documentclass[a4paper,11pt]{article}, titlesec, tabularx, enumitem, [hidelinks]{hyperref}, [normalem]{ulem}, [default]{sourcesanspro}, input glyphtounicode, \\pdfgentounicode=1.
                   - Redefine sections: \\titleformat{\\section}{\\scshape\\large}{}{0em}{\\color{blue}}[\\color{black}\\titlerule\\vspace{0pt}]
                   - Custom macros to define and use:
                     \\newcommand{\\resumeItem}[1]{\\item\\small{#1}}
                     \\newcommand{\\resumeItemListStart}{\\begin{itemize}[rightmargin=0.11in]}
                     \\newcommand{\\resumeItemListEnd}{\\end{itemize}}
                     \\newcommand{\\resumeSectionType}[3]{\\item\\begin{tabular*}{0.96\\textwidth}[t]{p{0.15\\linewidth}p{0.02\\linewidth}p{0.81\\linewidth}}\\textbf{#1} & #2 & #3\\end{tabular*}\\vspace{-2pt}}
                     \\newcommand{\\resumeTrioHeading}[3]{\\item\\small{\\begin{tabular*}{0.96\\textwidth}[t]{l@{\\extracolsep{\\fill}}c@{\\extracolsep{\\fill}}r}\\textbf{#1} & \\textit{#2} & #3\\end{tabular*}}}
                     \\newcommand{\\resumeQuadHeading}[4]{\\item\\begin{tabular*}{0.96\\textwidth}[t]{l@{\\extracolsep{\\fill}}r}\\textbf{#1} & #2 \\\\\\textit{\\small#3} & \\textit{\\small #4} \\\\\\end{tabular*}}
                     \\newcommand{\\resumeHeadingListStart}{\\begin{itemize}[leftmargin=0.15in, label={}]}
                     \\newcommand{\\resumeHeadingListEnd}{\\end{itemize}}
                   - Header format: Two-column tabular with Name on left and Location/Phone on right, with clickable links wrapped in \\href{...}{\\uline{...}}.
                   """;
        } else if (lower.contains("audric") || lower.contains("minimalist")) {
            return """
                   Use the Audric Serador Minimalist clean single-column format:
                   - Preamble packages: \\documentclass[letterpaper,11pt]{article}, latexsym, [empty]{fullpage}, titlesec, enumitem, [hidelinks]{hyperref}, fancyhdr, tabularx, input glyphtounicode, \\pdfgentounicode=1.
                   - Margins: 0.5in on all sides with \\raggedbottom and \\raggedright.
                   - Sections: \\titleformat{\\section}{\\vspace{-4pt}\\scshape\\raggedright\\large}{}{0em}{}[\\color{black}\\titlerule\\vspace{-5pt}].
                   - Header: Centered or clean top block with \\href{...}{...} clickable links for Email, GitHub, LinkedIn, and Portfolio.
                   """;
        } else if (lower.contains("awesome")) {
            return """
                   Use the Awesome-CV style single-page developer structure:
                   - Standard single-column layout with clean sans-serif typography.
                   - Sections: Experience, Education, Projects, Skills, Certifications.
                   - Include \\usepackage[hidelinks]{hyperref} and make every link clickable using \\href{URL}{Anchor Text}.
                   """;
        } else if (lower.contains("deedy")) {
            return """
                   Use the Deedy Resume two-column technical resume format:
                   - Left column for Education, Skills, and Links; Right column for Experience and Projects.
                   - Include \\usepackage[hidelinks]{hyperref} and ensure every link is clickable using \\href{URL}{Anchor Text}.
                   """;
        } else {
            // Default: Jake's Resume (Overleaf Standard)
            return """
                   Use Jake Gutierrez's classic Overleaf single-column software engineer resume structure:
                   - Preamble: \\documentclass[letterpaper,11pt]{article}, latexsym, [empty]{fullpage}, titlesec, marvosym, color, verbatim, enumitem, [hidelinks]{hyperref}, fancyhdr, tabularx, input glyphtounicode, \\pdfgentounicode=1.
                   - Margin settings: 0.5in left/right/top/bottom, \\raggedbottom, \\raggedright, \\setlength{\\tabcolsep}{0in}.
                   - Section formatting: \\titleformat{\\section}{\\vspace{-4pt}\\scshape\\raggedright\\large}{}{0em}{}[\\color{black}\\titlerule \\vspace{-5pt}].
                   - Custom macros to define and use:
                     \\newcommand{\\resumeItem}[1]{\\item\\small{{#1 \\vspace{-2pt}}}}
                     \\newcommand{\\resumeSubheading}[4]{\\vspace{-2pt}\\item\\begin{tabular*}{0.97\\textwidth}[t]{l@{\\extracolsep{\\fill}}r}\\textbf{#1} & #2 \\\\\\textit{\\small#3} & \\textit{\\small #4} \\\\\\end{tabular*}\\vspace{-7pt}}
                     \\newcommand{\\resumeSubSubheading}[2]{\\item\\begin{tabular*}{0.97\\textwidth}{l@{\\extracolsep{\\fill}}r}\\textit{\\small#1} & \\textit{\\small #2} \\\\\\end{tabular*}\\vspace{-7pt}}
                     \\newcommand{\\resumeProjectHeading}[2]{\\item\\begin{tabular*}{0.97\\textwidth}{l@{\\extracolsep{\\fill}}r}\\small#1 & #2 \\\\\\end{tabular*}\\vspace{-7pt}}
                     \\newcommand{\\resumeSubHeadingListStart}{\\begin{itemize}[leftmargin=0.15in, label={}]}
                     \\newcommand{\\resumeSubHeadingListEnd}{\\end{itemize}}
                     \\newcommand{\\resumeItemListStart}{\\begin{itemize}}
                     \\newcommand{\\resumeItemListEnd}{\\end{itemize}\\vspace{-5pt}}
                   - Header format: Centered name in \\Huge \\scshape, followed by phone, \\href{mailto:...}{\\underline{Email}}, \\href{https://linkedin.com/in/...}{\\underline{LinkedIn}}, \\href{https://github.com/...}{\\underline{GitHub}}, and Portfolio link.
                   - Projects format: \\resumeProjectHeading{\\textbf{Project Name} $|$ \\emph{Tech Stack} $|$ \\href{URL}{\\underline{Live Demo}}}{Dates}
                   """;
        }
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
            .map(s -> s.getMissingKeywords())
            .findFirst()
            .orElse("(none detected)");
    }
}
