package com.jobtracker.controller;

import com.jobtracker.dto.response.ApiResponse;
import com.jobtracker.dto.response.ConversionEntry;
import com.jobtracker.dto.response.OverviewResponse;
import com.jobtracker.dto.response.TimelineResponse;
import com.jobtracker.entity.User;
import com.jobtracker.exception.ResourceNotFoundException;
import com.jobtracker.service.StatsService;
import com.jobtracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;
    private final UserService userService;

    private User resolveUser(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<OverviewResponse>> overview(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = resolveUser(userDetails);
        return ResponseEntity.ok(ApiResponse.of(statsService.getOverview(user.getId())));
    }

    @GetMapping("/timeline")
    public ResponseEntity<ApiResponse<TimelineResponse>> timeline(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = resolveUser(userDetails);
        return ResponseEntity.ok(ApiResponse.of(statsService.getTimeline(user.getId())));
    }

    @GetMapping("/conversion")
    public ResponseEntity<ApiResponse<List<ConversionEntry>>> conversion(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = resolveUser(userDetails);
        return ResponseEntity.ok(ApiResponse.of(statsService.getConversion(user.getId())));
    }
}
