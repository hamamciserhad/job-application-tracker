package com.jobtracker.service;

import com.jobtracker.dto.response.ConversionEntry;
import com.jobtracker.dto.response.OverviewResponse;
import com.jobtracker.dto.response.TimelineResponse;
import com.jobtracker.entity.ApplicationStatus;
import com.jobtracker.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final ApplicationRepository applicationRepository;

    private static final List<ApplicationStatus> FUNNEL_STAGES = List.of(
            ApplicationStatus.APPLIED,
            ApplicationStatus.PHONE_SCREEN,
            ApplicationStatus.INTERVIEW,
            ApplicationStatus.TECHNICAL_TEST,
            ApplicationStatus.OFFER,
            ApplicationStatus.ACCEPTED
    );

    @Transactional(readOnly = true)
    public OverviewResponse getOverview(Long userId) {
        List<Object[]> rows = applicationRepository.countByStatusForUser(userId);

        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (ApplicationStatus s : ApplicationStatus.values()) {
            byStatus.put(s.name(), 0L);
        }

        long total = 0;
        for (Object[] row : rows) {
            ApplicationStatus status = (ApplicationStatus) row[0];
            long count = (Long) row[1];
            byStatus.put(status.name(), count);
            total += count;
        }

        return new OverviewResponse(total, byStatus);
    }

    @Transactional(readOnly = true)
    public TimelineResponse getTimeline(Long userId) {
        List<Object[]> rows = applicationRepository.countByMonthForUser(userId);

        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();

        for (Object[] row : rows) {
            labels.add((String) row[0]);
            values.add(((Number) row[1]).longValue());
        }

        return new TimelineResponse(labels, values);
    }

    @Transactional(readOnly = true)
    public List<ConversionEntry> getConversion(Long userId) {
        List<Object[]> rows = applicationRepository.countByStatusForUser(userId);

        Map<ApplicationStatus, Long> countMap = new HashMap<>();
        for (Object[] row : rows) {
            countMap.put((ApplicationStatus) row[0], (Long) row[1]);
        }

        return FUNNEL_STAGES.stream()
                .map(stage -> new ConversionEntry(stage.name(), countMap.getOrDefault(stage, 0L)))
                .collect(Collectors.toList());
    }
}
