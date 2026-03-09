package com.jobtracker.controller;

import com.jobtracker.dto.response.ApplicationResponse;
import com.jobtracker.entity.Application;
import com.jobtracker.entity.ApplicationStatus;
import com.jobtracker.entity.User;
import com.jobtracker.exception.ResourceNotFoundException;
import com.jobtracker.mapper.ApplicationMapper;
import com.jobtracker.security.CustomUserDetailsService;
import com.jobtracker.security.JwtTokenProvider;
import com.jobtracker.service.ApplicationService;
import com.jobtracker.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApplicationController.class)
class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApplicationService applicationService;

    @MockBean
    private ApplicationMapper applicationMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private User testUser;
    private Application testApp;
    private ApplicationResponse testResponse;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).email("user").name("Test User").build();
        testApp = Application.builder()
                .id(1L)
                .user(testUser)
                .companyName("Google")
                .position("SWE")
                .status(ApplicationStatus.APPLIED)
                .build();
        testResponse = new ApplicationResponse(
                1L, "Google", "SWE", ApplicationStatus.APPLIED,
                null, null, null, null,
                LocalDate.now(), LocalDateTime.now(), LocalDateTime.now()
        );

        when(userService.findByEmail("user")).thenReturn(Optional.of(testUser));
    }

    @Test
    @WithMockUser
    void list_returnsPageResponse() throws Exception {
        when(applicationService.findAll(anyLong(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(testApp)));
        when(applicationMapper.toResponse(testApp)).thenReturn(testResponse);

        mockMvc.perform(get("/api/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].companyName").value("Google"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser
    void getById_found_returns200WithData() throws Exception {
        when(applicationService.findById(1L, 1L)).thenReturn(testApp);
        when(applicationMapper.toResponse(testApp)).thenReturn(testResponse);

        mockMvc.perform(get("/api/applications/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.companyName").value("Google"));
    }

    @Test
    @WithMockUser
    void getById_notFound_returns404() throws Exception {
        when(applicationService.findById(99L, 1L))
                .thenThrow(new ResourceNotFoundException("Application not found"));

        mockMvc.perform(get("/api/applications/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void create_validRequest_returns201() throws Exception {
        String body = """
                {"companyName": "Google", "position": "SWE"}
                """;
        when(applicationService.create(any(), eq(testUser))).thenReturn(testApp);
        when(applicationMapper.toResponse(testApp)).thenReturn(testResponse);

        mockMvc.perform(post("/api/applications")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.companyName").value("Google"));
    }

    @Test
    @WithMockUser
    void create_missingRequiredFields_returns400() throws Exception {
        String body = """
                {"position": "SWE"}
                """;

        mockMvc.perform(post("/api/applications")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void update_validRequest_returns200() throws Exception {
        String body = """
                {"companyName": "Google", "position": "Staff SWE"}
                """;
        when(applicationService.update(eq(1L), any(), eq(1L))).thenReturn(testApp);
        when(applicationMapper.toResponse(testApp)).thenReturn(testResponse);

        mockMvc.perform(put("/api/applications/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @WithMockUser
    void updateStatus_validRequest_returns200() throws Exception {
        String body = """
                {"status": "INTERVIEW"}
                """;
        when(applicationService.updateStatus(1L, ApplicationStatus.INTERVIEW, 1L)).thenReturn(testApp);
        when(applicationMapper.toResponse(testApp)).thenReturn(testResponse);

        mockMvc.perform(patch("/api/applications/1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @WithMockUser
    void updateStatus_nullStatus_returns400() throws Exception {
        String body = """
                {}
                """;

        mockMvc.perform(patch("/api/applications/1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void delete_returns204() throws Exception {
        doNothing().when(applicationService).delete(1L, 1L);

        mockMvc.perform(delete("/api/applications/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
