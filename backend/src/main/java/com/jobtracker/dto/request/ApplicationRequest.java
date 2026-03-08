package com.jobtracker.dto.request;

import com.jobtracker.entity.ApplicationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ApplicationRequest(
        @NotBlank @Size(max = 255) String companyName,
        @NotBlank @Size(max = 255) String position,
        ApplicationStatus status,
        BigDecimal salary,
        @Size(max = 255) String location,
        String jobUrl,
        String notes,
        LocalDate appliedDate
) {}
