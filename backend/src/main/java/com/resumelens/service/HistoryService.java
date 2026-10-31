package com.resumelens.service;

import com.resumelens.dto.response.HistoryItemResponse;
import com.resumelens.exception.ResourceNotFoundException;
import com.resumelens.repository.MatchResultRepository;
import com.resumelens.repository.ResumeRepository;
import com.resumelens.repository.SectionScoreRepository;
import com.resumelens.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class HistoryService {

    private final MatchResultRepository matchResultRepository;
    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;
    private final SectionScoreRepository sectionScoreRepository;

    public HistoryService(MatchResultRepository matchResultRepository,
                          UserRepository userRepository,
                          ResumeRepository resumeRepository,
                          SectionScoreRepository sectionScoreRepository) {
        this.matchResultRepository = matchResultRepository;
        this.userRepository = userRepository;
        this.resumeRepository = resumeRepository;
        this.sectionScoreRepository = sectionScoreRepository;
    }

    @Transactional(readOnly = true)
    public List<HistoryItemResponse> getHistoryForUser(String email) {
        var user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User", email));

        return matchResultRepository.findByResumeUserIdOrderByAnalyzedAtDesc(user.getId(), PageRequest.of(0, 50))
            .stream()
            .map(mr -> new HistoryItemResponse(
                mr.getPublicId(),
                mr.getResume().getPublicId(),
                mr.getResume().getFileName(),
                mr.getJobDescription().getTitle(),
                mr.getOverallScore(),
                mr.getStatusLabel(),
                mr.getAnalyzedAt()))
            .collect(Collectors.toList());
    }

    @Transactional
    public void deleteHistoryItem(String email, UUID matchResultPublicId) {
        var user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User", email));

        var matchResult = matchResultRepository.findByPublicId(matchResultPublicId)
            .orElseThrow(() -> new ResourceNotFoundException("MatchResult", matchResultPublicId));

        var resume = matchResult.getResume();
        if (resume.getUser() == null || !resume.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("MatchResult", matchResultPublicId);
        }

        var sectionScores = sectionScoreRepository.findByMatchResultId(matchResult.getId());
        if (!sectionScores.isEmpty()) {
            sectionScoreRepository.deleteAll(sectionScores);
        }

        matchResultRepository.delete(matchResult);

        if (matchResultRepository.countByResumeId(resume.getId()) == 0) {
            resumeRepository.delete(resume);
        }
    }
}
