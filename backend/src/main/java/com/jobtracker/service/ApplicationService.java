package com.jobtracker.service;

import com.jobtracker.dto.request.ApplicationRequest;
import com.jobtracker.entity.Application;
import com.jobtracker.entity.ApplicationStatus;
import com.jobtracker.entity.User;
import com.jobtracker.exception.ResourceNotFoundException;
import com.jobtracker.repository.ApplicationRepository;
import com.jobtracker.repository.ApplicationSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;

    @Transactional(readOnly = true)
    public Page<Application> findAll(Long userId, ApplicationStatus status,
                                     String companyName, LocalDate fromDate,
                                     LocalDate toDate, Pageable pageable) {
        Specification<Application> spec = Specification.where(ApplicationSpecification.byUserId(userId));

        if (status != null) {
            spec = spec.and(ApplicationSpecification.byStatus(status));
        }
        if (companyName != null && !companyName.isBlank()) {
            spec = spec.and(ApplicationSpecification.companyNameContains(companyName));
        }
        if (fromDate != null) {
            spec = spec.and(ApplicationSpecification.appliedDateFrom(fromDate));
        }
        if (toDate != null) {
            spec = spec.and(ApplicationSpecification.appliedDateTo(toDate));
        }

        return applicationRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public Application findById(Long id, Long userId) {
        return applicationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
    }

    @Transactional
    public Application create(ApplicationRequest request, User user) {
        Application app = Application.builder()
                .user(user)
                .companyName(request.companyName())
                .position(request.position())
                .status(request.status() != null ? request.status() : ApplicationStatus.APPLIED)
                .salary(request.salary())
                .location(request.location())
                .jobUrl(request.jobUrl())
                .notes(request.notes())
                .appliedDate(request.appliedDate())
                .build();
        return applicationRepository.save(app);
    }

    @Transactional
    public Application update(Long id, ApplicationRequest request, Long userId) {
        Application app = findById(id, userId);
        app.setCompanyName(request.companyName());
        app.setPosition(request.position());
        if (request.status() != null) {
            app.setStatus(request.status());
        }
        app.setSalary(request.salary());
        app.setLocation(request.location());
        app.setJobUrl(request.jobUrl());
        app.setNotes(request.notes());
        app.setAppliedDate(request.appliedDate());
        return applicationRepository.save(app);
    }

    @Transactional
    public void delete(Long id, Long userId) {
        Application app = findById(id, userId);
        applicationRepository.delete(app);
    }
}
