package com.resumelens.service;

import com.resumelens.entity.MatchResult;
import com.resumelens.entity.Resume;
import com.resumelens.entity.SectionScore;
import com.resumelens.repository.MatchResultRepository;
import com.resumelens.repository.ResumeRepository;
import com.resumelens.repository.SectionScoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RetentionService {

    private static final Logger log = LoggerFactory.getLogger(RetentionService.class);

    private final ResumeRepository resumeRepository;
    private final MatchResultRepository matchResultRepository;
    private final SectionScoreRepository sectionScoreRepository;
    private final int retentionDays;

    public RetentionService(ResumeRepository resumeRepository,
                            MatchResultRepository matchResultRepository,
                            SectionScoreRepository sectionScoreRepository,
                            @Value("${app.retention-days:${RETENTION_DAYS:7}}") int retentionDays) {
        this.resumeRepository = resumeRepository;
        this.matchResultRepository = matchResultRepository;
        this.sectionScoreRepository = sectionScoreRepository;
        this.retentionDays = retentionDays;
    }

    public int getRetentionDays() {
        return retentionDays;
    }

    @Scheduled(cron = "${app.retention.cron:0 0 2 * * *}")
    @Transactional
    public int runScheduledRetentionCleanup() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
        return cleanupExpiredGuestData(cutoff);
    }

    @Transactional
    public int cleanupExpiredGuestData(LocalDateTime cutoff) {
        List<Resume> expiredResumes = resumeRepository.findByUserIsNullAndUploadedAtBefore(cutoff);
        if (expiredResumes.isEmpty()) {
            log.info("Guest data cleanup finished: deleted 0 rows.");
            return 0;
        }

        List<MatchResult> matchResults = matchResultRepository.findByResumeIn(expiredResumes);
        int deletedScores = 0;
        if (!matchResults.isEmpty()) {
            List<SectionScore> sectionScores = sectionScoreRepository.findByMatchResultIn(matchResults);
            deletedScores = sectionScores.size();
            sectionScoreRepository.deleteAll(sectionScores);
        }

        int deletedResults = matchResults.size();
        matchResultRepository.deleteAll(matchResults);

        int deletedResumes = expiredResumes.size();
        resumeRepository.deleteAll(expiredResumes);

        int totalDeleted = deletedScores + deletedResults + deletedResumes;
        log.info("Guest data cleanup finished: deleted {} rows.", totalDeleted);
        return totalDeleted;
    }
}

