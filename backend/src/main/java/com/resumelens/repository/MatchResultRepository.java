package com.resumelens.repository;

import com.resumelens.entity.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MatchResultRepository extends JpaRepository<MatchResult, Long> {
    List<MatchResult> findByResumeUserIdOrderByAnalyzedAtDesc(Long userId);
}