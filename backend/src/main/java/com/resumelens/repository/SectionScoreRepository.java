package com.resumelens.repository;

import com.resumelens.entity.SectionScore;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SectionScoreRepository extends JpaRepository<SectionScore, Long> {
    List<SectionScore> findByMatchResultId(Long matchResultId);
    List<SectionScore> findByMatchResultPublicId(UUID publicId);
}