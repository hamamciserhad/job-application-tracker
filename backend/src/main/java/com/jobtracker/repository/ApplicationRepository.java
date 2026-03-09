package com.jobtracker.repository;

import com.jobtracker.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long>,
        JpaSpecificationExecutor<Application> {

    Optional<Application> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT a.status, COUNT(a) FROM Application a WHERE a.user.id = :userId GROUP BY a.status")
    List<Object[]> countByStatusForUser(@Param("userId") Long userId);

    @Query(value = "SELECT TO_CHAR(applied_date, 'YYYY-MM') AS month, COUNT(*) AS cnt " +
                   "FROM applications WHERE user_id = :userId AND applied_date IS NOT NULL " +
                   "GROUP BY month ORDER BY month ASC", nativeQuery = true)
    List<Object[]> countByMonthForUser(@Param("userId") Long userId);
}
