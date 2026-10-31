package com.resumelens.repository;

import com.resumelens.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    Optional<Resume> findByPublicId(UUID publicId);
    List<Resume> findByUserIdOrderByUploadedAtDesc(Long userId);
    List<Resume> findByUserIsNullAndUploadedAtBefore(LocalDateTime cutoff);
}