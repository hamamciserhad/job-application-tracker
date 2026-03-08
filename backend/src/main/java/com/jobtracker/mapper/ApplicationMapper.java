package com.jobtracker.mapper;

import com.jobtracker.dto.response.ApplicationResponse;
import com.jobtracker.entity.Application;
import org.springframework.stereotype.Component;

@Component
public class ApplicationMapper {

    public ApplicationResponse toResponse(Application app) {
        return new ApplicationResponse(
                app.getId(),
                app.getCompanyName(),
                app.getPosition(),
                app.getStatus(),
                app.getSalary(),
                app.getLocation(),
                app.getJobUrl(),
                app.getNotes(),
                app.getAppliedDate(),
                app.getCreatedAt(),
                app.getUpdatedAt()
        );
    }
}
