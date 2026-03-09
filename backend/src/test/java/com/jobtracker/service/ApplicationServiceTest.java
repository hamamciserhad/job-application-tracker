package com.jobtracker.service;

import com.jobtracker.dto.request.ApplicationRequest;
import com.jobtracker.entity.Application;
import com.jobtracker.entity.ApplicationStatus;
import com.jobtracker.entity.User;
import com.jobtracker.exception.ResourceNotFoundException;
import com.jobtracker.repository.ApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private StatusHistoryService statusHistoryService;

    @InjectMocks
    private ApplicationService applicationService;

    private User testUser;
    private Application testApp;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).email("test@test.com").name("Test User").build();
        testApp = Application.builder()
                .id(1L)
                .user(testUser)
                .companyName("Google")
                .position("SWE")
                .status(ApplicationStatus.APPLIED)
                .build();
    }

    @Test
    void findAll_returnsPageOfApplications() {
        Page<Application> page = new PageImpl<>(List.of(testApp));
        when(applicationRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);

        Page<Application> result = applicationService.findAll(1L, null, null, null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCompanyName()).isEqualTo("Google");
    }

    @Test
    void findAll_withStatusFilter_passesSpecification() {
        Page<Application> page = new PageImpl<>(List.of(testApp));
        when(applicationRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);

        Page<Application> result = applicationService.findAll(1L, ApplicationStatus.APPLIED, null, null, null, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(applicationRepository).findAll(any(Specification.class), any(PageRequest.class));
    }

    @Test
    void findById_found_returnsApplication() {
        when(applicationRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testApp));

        Application result = applicationService.findById(1L, 1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCompanyName()).isEqualTo("Google");
    }

    @Test
    void findById_notFound_throwsResourceNotFoundException() {
        when(applicationRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicationService.findById(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Application not found");
    }

    @Test
    void create_withNullStatus_defaultsToApplied() {
        ApplicationRequest request = new ApplicationRequest("Meta", "Backend", null, null, null, null, null, null);
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

        Application result = applicationService.create(request, testUser);

        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.APPLIED);
        assertThat(result.getCompanyName()).isEqualTo("Meta");
        assertThat(result.getUser()).isEqualTo(testUser);
    }

    @Test
    void create_withSpecifiedStatus_usesProvidedStatus() {
        ApplicationRequest request = new ApplicationRequest("Netflix", "SRE", ApplicationStatus.INTERVIEW, null, null, null, null, null);
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

        Application result = applicationService.create(request, testUser);

        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.INTERVIEW);
    }

    @Test
    void update_withSameStatus_doesNotRecordHistory() {
        ApplicationRequest request = new ApplicationRequest("Google", "Staff SWE", ApplicationStatus.APPLIED, null, null, null, null, null);
        when(applicationRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testApp));
        when(applicationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        applicationService.update(1L, request, 1L);

        verify(statusHistoryService, never()).record(any(), any(), any());
        assertThat(testApp.getPosition()).isEqualTo("Staff SWE");
    }

    @Test
    void update_withDifferentStatus_recordsHistory() {
        ApplicationRequest request = new ApplicationRequest("Google", "SWE", ApplicationStatus.INTERVIEW, null, null, null, null, null);
        when(applicationRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testApp));
        when(applicationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        applicationService.update(1L, request, 1L);

        verify(statusHistoryService).record(testApp, ApplicationStatus.APPLIED, ApplicationStatus.INTERVIEW);
        assertThat(testApp.getStatus()).isEqualTo(ApplicationStatus.INTERVIEW);
    }

    @Test
    void updateStatus_changesStatusAndRecordsHistory() {
        when(applicationRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testApp));
        when(applicationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Application result = applicationService.updateStatus(1L, ApplicationStatus.OFFER, 1L);

        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.OFFER);
        verify(statusHistoryService).record(testApp, ApplicationStatus.APPLIED, ApplicationStatus.OFFER);
    }

    @Test
    void delete_success_deletesApplication() {
        when(applicationRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testApp));

        applicationService.delete(1L, 1L);

        verify(applicationRepository).delete(testApp);
    }

    @Test
    void delete_notFound_throwsResourceNotFoundException() {
        when(applicationRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicationService.delete(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
