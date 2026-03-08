package com.jobtracker.dto.response;

import com.jobtracker.entity.ApplicationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ApplicationResponse(
        Long id,
        String companyName,
        String position,
        ApplicationStatus status,
        BigDecimal salary,
        String location,
        String jobUrl,
        String notes,
        LocalDate appliedDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
