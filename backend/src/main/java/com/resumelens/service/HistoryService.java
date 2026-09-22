package com.resumelens.service;

import com.resumelens.dto.response.HistoryItemResponse;
import com.resumelens.exception.ResourceNotFoundException;
import com.resumelens.repository.MatchResultRepository;
import com.resumelens.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HistoryService {

    private final MatchResultRepository matchResultRepository;
    private final UserRepository userRepository;

    public HistoryService(MatchResultRepository matchResultRepository,
                          UserRepository userRepository) {
        this.matchResultRepository = matchResultRepository;
        this.userRepository = userRepository;
    }

    public List<HistoryItemResponse> getHistoryForUser(String email) {
        var user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User", email));

        return matchResultRepository.findByResumeUserIdOrderByAnalyzedAtDesc(user.getId())
            .stream()
            .map(mr -> new HistoryItemResponse(
                mr.getId(),
                mr.getResume().getId(),
                mr.getResume().getFileName(),
                mr.getJobDescription().getTitle(),
                mr.getOverallScore(),
                mr.getStatusLabel(),
                mr.getAnalyzedAt()))
            .collect(Collectors.toList());
    }
}