package com.jobtracker.service;

import com.jobtracker.dto.response.ConversionEntry;
import com.jobtracker.dto.response.OverviewResponse;
import com.jobtracker.dto.response.TimelineResponse;
import com.jobtracker.entity.ApplicationStatus;
import com.jobtracker.repository.ApplicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private StatsService statsService;

    @Test
    void getOverview_returnsCorrectTotalsAndStatusCounts() {
        when(applicationRepository.countByStatusForUser(1L)).thenReturn(List.of(
                new Object[]{ApplicationStatus.APPLIED, 5L},
                new Object[]{ApplicationStatus.INTERVIEW, 3L},
                new Object[]{ApplicationStatus.REJECTED, 2L}
        ));

        OverviewResponse response = statsService.getOverview(1L);

        assertThat(response.total()).isEqualTo(10L);
        assertThat(response.byStatus()).containsEntry("APPLIED", 5L);
        assertThat(response.byStatus()).containsEntry("INTERVIEW", 3L);
        assertThat(response.byStatus()).containsEntry("REJECTED", 2L);
        assertThat(response.byStatus()).containsEntry("OFFER", 0L);
    }

    @Test
    void getOverview_noApplications_returnsZeroTotalAndAllZeroCounts() {
        when(applicationRepository.countByStatusForUser(1L)).thenReturn(List.of());

        OverviewResponse response = statsService.getOverview(1L);

        assertThat(response.total()).isEqualTo(0L);
        assertThat(response.byStatus().values()).allMatch(v -> v == 0L);
        assertThat(response.byStatus()).hasSize(ApplicationStatus.values().length);
    }

    @Test
    void getTimeline_returnsLabelsAndValues() {
        when(applicationRepository.countByMonthForUser(1L)).thenReturn(List.of(
                new Object[]{"2026-01", 3L},
                new Object[]{"2026-02", 7L},
                new Object[]{"2026-03", 2L}
        ));

        TimelineResponse response = statsService.getTimeline(1L);

        assertThat(response.labels()).containsExactly("2026-01", "2026-02", "2026-03");
        assertThat(response.values()).containsExactly(3L, 7L, 2L);
    }

    @Test
    void getTimeline_noData_returnsEmptyLists() {
        when(applicationRepository.countByMonthForUser(1L)).thenReturn(List.of());

        TimelineResponse response = statsService.getTimeline(1L);

        assertThat(response.labels()).isEmpty();
        assertThat(response.values()).isEmpty();
    }

    @Test
    void getConversion_returnsFunnelStagesInOrder() {
        when(applicationRepository.countByStatusForUser(1L)).thenReturn(List.of(
                new Object[]{ApplicationStatus.APPLIED, 10L},
                new Object[]{ApplicationStatus.INTERVIEW, 4L},
                new Object[]{ApplicationStatus.OFFER, 1L}
        ));

        List<ConversionEntry> entries = statsService.getConversion(1L);

        assertThat(entries).extracting(ConversionEntry::stage)
                .containsExactly("APPLIED", "PHONE_SCREEN", "INTERVIEW", "TECHNICAL_TEST", "OFFER", "ACCEPTED");
        assertThat(entries.get(0).count()).isEqualTo(10L);
        assertThat(entries.get(1).count()).isEqualTo(0L);
        assertThat(entries.get(2).count()).isEqualTo(4L);
        assertThat(entries.get(4).count()).isEqualTo(1L);
    }

    @Test
    void getConversion_noApplications_returnsAllZeroCounts() {
        when(applicationRepository.countByStatusForUser(1L)).thenReturn(List.of());

        List<ConversionEntry> entries = statsService.getConversion(1L);

        assertThat(entries).hasSize(6);
        assertThat(entries).extracting(ConversionEntry::count).allMatch(c -> c == 0L);
    }
}
