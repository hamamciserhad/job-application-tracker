package com.jobtracker.dto.request;

import com.jobtracker.entity.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public record StatusRequest(@NotNull ApplicationStatus status) {}
