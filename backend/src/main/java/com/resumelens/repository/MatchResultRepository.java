package com.resumelens.repository;

import com.resumelens.entity.MatchResult;
import com.resumelens.entity.Resume;
import java.util.Collection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MatchResultRepository extends JpaRepository<MatchResult, Long> {
    @EntityGraph(attributePaths = {"resume", "jobDescription"})
    Optional<MatchResult> findByPublicId(UUID publicId);

    @EntityGraph(attributePaths = {"resume", "jobDescription"})
    List<MatchResult> findByResumeUserIdOrderByAnalyzedAtDesc(Long userId, Pageable pageable);

    List<MatchResult> findByResumeIn(Collection<Resume> resumes);

    long countByResumeId(Long resumeId);
}