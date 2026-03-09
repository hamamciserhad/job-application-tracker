package com.jobtracker.service;

import com.jobtracker.dto.response.StatusHistoryResponse;
import com.jobtracker.entity.Application;
import com.jobtracker.entity.ApplicationStatus;
import com.jobtracker.entity.StatusHistory;
import com.jobtracker.repository.StatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StatusHistoryService {

    private final StatusHistoryRepository statusHistoryRepository;

    public void record(Application application, ApplicationStatus oldStatus, ApplicationStatus newStatus) {
        StatusHistory entry = StatusHistory.builder()
                .application(application)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .build();
        statusHistoryRepository.save(entry);
    }

    @Transactional(readOnly = true)
    public List<StatusHistoryResponse> getHistory(Long applicationId) {
        return statusHistoryRepository.findAllByApplicationIdOrderByChangedAtAsc(applicationId)
                .stream()
                .map(h -> new StatusHistoryResponse(
                        h.getId(),
                        h.getOldStatus(),
                        h.getNewStatus(),
                        h.getChangedAt(),
                        h.getNotes()))
                .toList();
    }
}
