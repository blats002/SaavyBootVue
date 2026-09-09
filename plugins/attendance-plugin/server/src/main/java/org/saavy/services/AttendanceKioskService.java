package org.saavy.services;

import org.saavy.entity.*;
import org.saavy.reference.AttendanceLogType;
import org.saavy.reference.AttendanceStatus;
import org.saavy.reference.VerificationMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class AttendanceKioskService {

    @Autowired
    private EmployeeBadgeRepository employeeBadgeRepository;

    @Autowired
    private AttendanceLogRepository attendanceLogRepository;

    private static final LocalTime STANDARD_SHIFT_START = LocalTime.of(9, 15); // After 9:15 AM considered late

    @Transactional
    public AttendanceScanResponse processScan(AttendanceScanRequest request) {
        if ((request.getQrToken() == null || request.getQrToken().isBlank()) &&
            (request.getPinCode() == null || request.getPinCode().isBlank())) {
            return AttendanceScanResponse.builder()
                    .success(false)
                    .message("No QR token or PIN provided.")
                    .build();
        }

        Optional<EmployeeBadge> badgeOpt;
        VerificationMethod method;

        if (request.getQrToken() != null && !request.getQrToken().isBlank()) {
            badgeOpt = employeeBadgeRepository.findByQrTokenAndIsActiveTrue(request.getQrToken().trim());
            method = VerificationMethod.QR_CODE;
        } else {
            badgeOpt = employeeBadgeRepository.findByPinCodeAndIsActiveTrue(request.getPinCode().trim());
            method = VerificationMethod.PIN;
        }

        if (badgeOpt.isEmpty()) {
            return AttendanceScanResponse.builder()
                    .success(false)
                    .message("Invalid or inactive employee badge.")
                    .build();
        }

        EmployeeBadge badge = badgeOpt.get();
        LocalDateTime now = LocalDateTime.now();

        // Check recent punch within 15 seconds to prevent accidental double punch
        Optional<AttendanceLog> lastLogOpt = attendanceLogRepository.findTopByEmployeeIdOrderByTimestampDesc(badge.getEmployeeId());
        if (lastLogOpt.isPresent()) {
            AttendanceLog lastLog = lastLogOpt.get();
            if (lastLog.getTimestamp().isAfter(now.minusSeconds(15))) {
                return AttendanceScanResponse.builder()
                        .success(true)
                        .message("Punch already registered just now (" + lastLog.getLogType() + ").")
                        .employeeId(badge.getEmployeeId())
                        .employeeName(badge.getEmployeeName())
                        .department(badge.getDepartment())
                        .jobTitle(badge.getJobTitle())
                        .avatar(badge.getAvatarFile() != null ? badge.getAvatarFile().getContent() : null)
                        .logType(lastLog.getLogType())
                        .status(lastLog.getStatus())
                        .timestamp(lastLog.getTimestamp())
                        .build();
            }
        }

        // Determine log type: use preferredAction if specified, or auto-detect
        AttendanceLogType action;
        if (request.getPreferredAction() != null) {
            action = request.getPreferredAction();
        } else if (lastLogOpt.isEmpty()) {
            action = AttendanceLogType.CLOCK_IN;
        } else {
            AttendanceLogType lastType = lastLogOpt.get().getLogType();
            if (lastType == AttendanceLogType.CLOCK_IN || lastType == AttendanceLogType.BREAK_IN) {
                action = AttendanceLogType.CLOCK_OUT;
            } else {
                action = AttendanceLogType.CLOCK_IN;
            }
        }

        // Determine status
        AttendanceStatus status = AttendanceStatus.ON_TIME;
        if (action == AttendanceLogType.CLOCK_IN) {
            if (now.toLocalTime().isAfter(STANDARD_SHIFT_START)) {
                status = AttendanceStatus.LATE;
            }
        }

        // Save log
        AttendanceLog log = new AttendanceLog();
        log.setEmployeeId(badge.getEmployeeId());
        log.setEmployeeName(badge.getEmployeeName());
        log.setDepartment(badge.getDepartment());
        log.setTimestamp(now);
        log.setLogType(action);
        log.setVerificationMethod(method);
        log.setStatus(status);
        log.setKioskDeviceId(request.getKioskDeviceId());
        attendanceLogRepository.save(log);

        String actionLabel = action == AttendanceLogType.CLOCK_IN ? "Clocked In" :
                action == AttendanceLogType.CLOCK_OUT ? "Clocked Out" :
                action == AttendanceLogType.BREAK_OUT ? "On Break" : "Back from Break";

        return AttendanceScanResponse.builder()
                .success(true)
                .message("Successfully " + actionLabel + " at " + now.toLocalTime().withNano(0))
                .employeeId(badge.getEmployeeId())
                .employeeName(badge.getEmployeeName())
                .department(badge.getDepartment())
                .jobTitle(badge.getJobTitle())
                .avatar(badge.getAvatarFile() != null ? badge.getAvatarFile().getContent() : null)
                .logType(action)
                .status(status)
                .timestamp(now)
                .build();
    }

    public AttendanceSummaryDTO getTodaySummary() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long totalEmployees = employeeBadgeRepository.countByIsActiveTrue();
        long present = attendanceLogRepository.countDistinctClockedInToday(startOfDay);
        long late = attendanceLogRepository.countDistinctLateToday(startOfDay);
        long absent = Math.max(0, totalEmployees - present);

        return AttendanceSummaryDTO.builder()
                .totalActiveEmployees(totalEmployees)
                .presentToday(present)
                .lateToday(late)
                .absentToday(absent)
                .onBreakToday(0)
                .build();
    }

    public List<AttendanceLog> getLiveFeed() {
        return attendanceLogRepository.findTop10ByOrderByTimestampDesc();
    }
}
