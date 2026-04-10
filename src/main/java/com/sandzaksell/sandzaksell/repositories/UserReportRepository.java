package com.sandzaksell.sandzaksell.repositories;

import com.sandzaksell.sandzaksell.models.UserReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserReportRepository extends JpaRepository<UserReport, Long> {
    boolean existsByReporterIdAndReportedUserId(Long reporterId, Long reportedUserId);
}