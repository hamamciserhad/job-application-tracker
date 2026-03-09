package com.jobtracker.repository;

import com.jobtracker.entity.Application;
import com.jobtracker.entity.ApplicationStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class ApplicationSpecification {

    public static Specification<Application> byUserId(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Application> byStatus(ApplicationStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Application> companyNameContains(String companyName) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("companyName")), "%" + companyName.toLowerCase() + "%");
    }

    public static Specification<Application> appliedDateFrom(LocalDate from) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("appliedDate"), from);
    }

    public static Specification<Application> appliedDateTo(LocalDate to) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("appliedDate"), to);
    }
}
