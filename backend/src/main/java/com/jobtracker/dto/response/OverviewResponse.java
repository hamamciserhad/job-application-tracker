package com.jobtracker.dto.response;

import java.util.Map;

public record OverviewResponse(long total, Map<String, Long> byStatus) {}
