package org.saavy.entity;

import org.saavy.reference.BaseJpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceLogRepository extends BaseJpaRepository<AttendanceLog, Long> {

    Optional<AttendanceLog> findTopByEmployeeIdOrderByTimestampDesc(String employeeId);

    List<AttendanceLog> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime start, LocalDateTime end);

    List<AttendanceLog> findTop10ByOrderByTimestampDesc();

    @Query("SELECT COUNT(DISTINCT a.employeeId) FROM AttendanceLog a WHERE a.timestamp >= :startOfDay AND a.logType = 'CLOCK_IN'")
    long countDistinctClockedInToday(@Param("startOfDay") LocalDateTime startOfDay);

    @Query("SELECT COUNT(DISTINCT a.employeeId) FROM AttendanceLog a WHERE a.timestamp >= :startOfDay AND a.status = 'LATE'")
    long countDistinctLateToday(@Param("startOfDay") LocalDateTime startOfDay);
}
