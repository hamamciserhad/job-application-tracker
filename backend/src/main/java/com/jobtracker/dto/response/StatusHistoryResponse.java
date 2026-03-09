package com.jobtracker.dto.response;

import com.jobtracker.entity.ApplicationStatus;

import java.time.LocalDateTime;

public record StatusHistoryResponse(
        Long id,
        ApplicationStatus oldStatus,
        ApplicationStatus newStatus,
        LocalDateTime changedAt,
        String notes) {}
