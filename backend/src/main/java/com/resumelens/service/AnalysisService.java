package com.resumelens.service;

import com.resumelens.dto.response.AnalysisResponse;
import com.resumelens.dto.response.SectionScoreResponse;
import com.resumelens.dto.response.SuggestionResponse;
import com.resumelens.entity.*;
import com.resumelens.exception.InvalidInputException;
import com.resumelens.exception.ResourceNotFoundException;
import com.resumelens.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnalysisService {

    private final ResumeRepository resumeRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final MatchResultRepository matchResultRepository;
    private final SectionScoreRepository sectionScoreRepository;
    private final ATSScoreEngine scoreEngine;
    private final SuggestionEngine suggestionEngine;
    private final PromptBuilderService promptBuilder;
    private final ResumeParserService resumeParser;
    private final ObjectMapper objectMapper;

    public AnalysisService(ResumeRepository resumeRepository,
                           JobDescriptionRepository jobDescriptionRepository,
                           MatchResultRepository matchResultRepository,
                           SectionScoreRepository sectionScoreRepository,
                           ATSScoreEngine scoreEngine,
                           SuggestionEngine suggestionEngine,
                           PromptBuilderService promptBuilder,
                           ResumeParserService resumeParser,
                           ObjectMapper objectMapper) {
        this.resumeRepository = resumeRepository;
        this.jobDescriptionRepository = jobDescriptionRepository;
        this.matchResultRepository = matchResultRepository;
        this.sectionScoreRepository = sectionScoreRepository;
        this.scoreEngine = scoreEngine;
        this.suggestionEngine = suggestionEngine;
        this.promptBuilder = promptBuilder;
        this.resumeParser = resumeParser;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public AnalysisResponse runAnalysis(Long resumeId, Long jobDescriptionId,
                                        String rawJdText, String jobTitle) {
        Resume resume = resumeRepository.findById(resumeId)
            .orElseThrow(() -> new ResourceNotFoundException("Resume", resumeId));

        // Resolve the JD: prefer persisted entity, fall back to raw text
        JobDescription jd;
        if (jobDescriptionId != null) {
            jd = jobDescriptionRepository.findById(jobDescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("JobDescription", jobDescriptionId));
        } else if (rawJdText != null && !rawJdText.isBlank()) {
            jd = jobDescriptionRepository.save(
                new JobDescription(jobTitle != null ? jobTitle : "Untitled", rawJdText, ""));
        } else {
            throw new InvalidInputException("Provide either jobDescriptionId or rawJobDescription.");
        }

        String resumeText = resume.getRawText();
        String jdText     = jd.getDescription();

        boolean hasFormattingRisk = resumeParser.detectFormattingRisks(resumeText);
        // Graphics detection is file-level; at analysis time we treat as false if not flagged at upload
        boolean hasGraphicsRisk = false;

        ATSScoreEngine.ScoreResult scores = scoreEngine.score(
            resumeText, jdText, hasFormattingRisk, hasGraphicsRisk);

        // Persist MatchResult
        MatchResult matchResult = new MatchResult(resume, jd);
        matchResult.setOverallScore(scores.overallScore());
        matchResult.setAtsScore(scores.atsScore());
        matchResult.setSkillsScore(scores.skillsScore());
        matchResult.setImpactScore(scores.impactScore());
        matchResult.setProjectScore(scores.projectScore());
        matchResult.setWritingScore(scores.writingScore());
        matchResult.setProfessionalismScore(scores.professionalismScore());
        matchResult.setStatusLabel(scores.statusLabel());
        matchResult.setKeywordMatchPercent(scores.keywordMatchPercent());
        matchResult = matchResultRepository.save(matchResult);

        // Persist SectionScores
        persistSectionScore(matchResult, SectionScore.ATS_COMPATIBILITY, scores.atsScore(),
            "ATS formatting and keyword compliance", List.of(), List.of());
        persistSectionScore(matchResult, SectionScore.SKILLS_VALIDATION, scores.skillsScore(),
            "Keyword and skill match against job description", scores.matchedKeywords(), scores.missingKeywords());
        persistSectionScore(matchResult, SectionScore.IMPACT_ACHIEVEMENTS, scores.impactScore(),
            "Quantified achievements and action verb usage", List.of(), List.of());
        persistSectionScore(matchResult, SectionScore.PROJECT_QUALITY, scores.projectScore(),
            "Project section completeness and measurable outcomes", List.of(), List.of());
        persistSectionScore(matchResult, SectionScore.WRITING_QUALITY, scores.writingScore(),
            "Sentence length, first-person usage, filler words, passive voice", List.of(), List.of());
        persistSectionScore(matchResult, SectionScore.PROFESSIONALISM, scores.professionalismScore(),
            "Date consistency, bullet style, email quality, resume length", List.of(), List.of());

        // Suggestions
        List<String> weakVerbs      = suggestionEngine.detectWeakVerbs(resumeText);
        List<String> unquantified   = suggestionEngine.checkQuantification(resumeText);
        List<SuggestionResponse> suggestions = suggestionEngine.generateTopSuggestions(
            scores.missingKeywords(), weakVerbs, unquantified, scores.overallScore());

        // Build and persist improvement prompt
        final Long savedMatchResultId = matchResult.getId();
        String prompt = promptBuilder.buildFixPrompt(savedMatchResultId, "Simple Single-Column");
        matchResult.setImprovementPrompt(prompt);
        matchResultRepository.save(matchResult);

        // Map section scores to response DTOs
        List<SectionScoreResponse> sectionResponses = sectionScoreRepository
            .findByMatchResultId(savedMatchResultId).stream()
            .map(s -> new SectionScoreResponse(
                s.getSectionName(), s.getScore(), s.getStatus(),
                s.getFeedback(), s.getMatchedKeywords(), s.getMissingKeywords()))
            .collect(Collectors.toList());

        return new AnalysisResponse(
            savedMatchResultId,
            scores.overallScore(), scores.statusLabel(),
            scores.atsScore(), scores.skillsScore(),
            scores.impactScore(), scores.projectScore(),
            scores.writingScore(), scores.professionalismScore(),
            scores.keywordMatchPercent(),
            sectionResponses, suggestions, prompt);
    }

    public AnalysisResponse getResult(Long matchResultId) {
        MatchResult mr = matchResultRepository.findById(matchResultId)
            .orElseThrow(() -> new ResourceNotFoundException("MatchResult", matchResultId));
        List<SectionScoreResponse> sections = sectionScoreRepository.findByMatchResultId(matchResultId)
            .stream()
            .map(s -> new SectionScoreResponse(
                s.getSectionName(), s.getScore(), s.getStatus(),
                s.getFeedback(), s.getMatchedKeywords(), s.getMissingKeywords()))
            .collect(Collectors.toList());

        return new AnalysisResponse(
            mr.getId(), mr.getOverallScore(), mr.getStatusLabel(),
            mr.getAtsScore(), mr.getSkillsScore(),
            mr.getImpactScore(), mr.getProjectScore(),
            mr.getWritingScore(), mr.getProfessionalismScore(),
            mr.getKeywordMatchPercent(), sections, List.of(),
            mr.getImprovementPrompt());
    }

    // Helpers
    private void persistSectionScore(MatchResult mr, String name, double score,
                                     String feedback, List<String> matched, List<String> missing) {
        sectionScoreRepository.save(new SectionScore(
            mr, name, score, feedback,
            toJson(matched), toJson(missing)));
    }

    private String toJson(List<String> list) {
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }
}