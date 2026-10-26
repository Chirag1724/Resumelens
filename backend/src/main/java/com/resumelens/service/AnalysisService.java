package com.resumelens.service;

import com.resumelens.dto.response.AnalysisResponse;
import com.resumelens.dto.response.RiskResponse;
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
import java.util.UUID;
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
    public AnalysisResponse runAnalysis(UUID resumeId, Long jobDescriptionId,
                                        String rawJdText, String jobTitle) {
        Resume resume = resumeRepository.findByPublicId(resumeId)
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

        boolean hasFormattingRisk = resume.hasFormattingRisk() || resumeParser.detectFormattingRisks(resumeText);
        boolean hasGraphicsRisk = resume.hasGraphicsRisk();

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
        String prompt = promptBuilder.buildFixPrompt(matchResult.getPublicId(), "Simple Single-Column");
        matchResult.setImprovementPrompt(prompt);
        matchResultRepository.save(matchResult);

        // Map section scores to response DTOs
        List<SectionScoreResponse> sectionResponses = sectionScoreRepository
            .findByMatchResultId(matchResult.getId()).stream()
            .map(s -> new SectionScoreResponse(
                s.getSectionName(), s.getScore(), s.getStatus(),
                s.getFeedback(), s.getMatchedKeywords(), s.getMissingKeywords()))
            .collect(Collectors.toList());

        return new AnalysisResponse(
            matchResult.getPublicId(),
            scores.overallScore(), scores.statusLabel(),
            scores.atsScore(), scores.skillsScore(),
            scores.impactScore(), scores.projectScore(),
            scores.writingScore(), scores.professionalismScore(),
            scores.keywordMatchPercent(),
            sectionResponses, suggestions, scores.risks(), prompt);
    }

    @Transactional(readOnly = true)
    public AnalysisResponse getResult(UUID matchResultPublicId) {
        MatchResult mr = matchResultRepository.findByPublicId(matchResultPublicId)
            .orElseThrow(() -> new ResourceNotFoundException("MatchResult", matchResultPublicId));
        List<SectionScoreResponse> sections = sectionScoreRepository.findByMatchResultId(mr.getId())
            .stream()
            .map(s -> new SectionScoreResponse(
                s.getSectionName(), s.getScore(), s.getStatus(),
                s.getFeedback(), s.getMatchedKeywords(), s.getMissingKeywords()))
            .collect(Collectors.toList());

        String resumeText = mr.getResume().getRawText();
        List<String> missingKeywords = extractMissingKeywords(sections);
        List<String> weakVerbs = suggestionEngine.detectWeakVerbs(resumeText);
        List<String> unquantified = suggestionEngine.checkQuantification(resumeText);
        List<SuggestionResponse> suggestions = suggestionEngine.generateTopSuggestions(
            missingKeywords, weakVerbs, unquantified, mr.getOverallScore());

        String jdText = mr.getJobDescription().getDescription();
        boolean hasFormattingRisk = mr.getResume().hasFormattingRisk() || resumeParser.detectFormattingRisks(resumeText);
        boolean hasGraphicsRisk = mr.getResume().hasGraphicsRisk();
        List<RiskResponse> risks = scoreEngine.score(resumeText, jdText, hasFormattingRisk, hasGraphicsRisk).risks();

        return new AnalysisResponse(
            mr.getPublicId(), mr.getOverallScore(), mr.getStatusLabel(),
            mr.getAtsScore(), mr.getSkillsScore(),
            mr.getImpactScore(), mr.getProjectScore(),
            mr.getWritingScore(), mr.getProfessionalismScore(),
            mr.getKeywordMatchPercent(), sections, suggestions,
            risks, mr.getImprovementPrompt());
    }

    private List<String> extractMissingKeywords(List<SectionScoreResponse> sections) {
        return sections.stream()
            .filter(s -> SectionScore.SKILLS_VALIDATION.equals(s.sectionName()))
            .findFirst()
            .map(s -> fromJson(s.missingKeywords()))
            .orElse(List.of());
    }

    private List<String> fromJson(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return List.of();
        }
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