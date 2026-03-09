package com.jobtracker.dto.response;

import java.util.List;

public record TimelineResponse(List<String> labels, List<Long> values) {}
