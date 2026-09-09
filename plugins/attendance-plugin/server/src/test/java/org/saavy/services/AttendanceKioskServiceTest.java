package org.saavy.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.saavy.entity.*;
import org.saavy.reference.AttendanceLogType;
import org.saavy.reference.AttendanceStatus;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceKioskServiceTest {

    @Mock
    private EmployeeBadgeRepository employeeBadgeRepository;

    @Mock
    private AttendanceLogRepository attendanceLogRepository;

    @InjectMocks
    private AttendanceKioskService attendanceKioskService;

    private EmployeeBadge sampleBadge;

    @BeforeEach
    void setUp() {
        sampleBadge = new EmployeeBadge();
        sampleBadge.setId(1L);
        sampleBadge.setEmployeeId("EMP-001");
        sampleBadge.setEmployeeName("Jane Doe");
        sampleBadge.setDepartment("Engineering");
        sampleBadge.setJobTitle("Software Engineer");
        sampleBadge.setQrToken("QR-EMP001-TOKEN");
        sampleBadge.setPinCode("1234");
        sampleBadge.setIsActive(true);
    }

    @Test
    @DisplayName("Should successfully clock in when valid QR token is scanned")
    void testProcessScan_ValidQrClockIn() {
        AttendanceScanRequest request = new AttendanceScanRequest();
        request.setQrToken("QR-EMP001-TOKEN");
        request.setKioskDeviceId("Terminal-01");

        when(employeeBadgeRepository.findByQrTokenAndIsActiveTrue("QR-EMP001-TOKEN"))
                .thenReturn(Optional.of(sampleBadge));
        when(attendanceLogRepository.findTopByEmployeeIdOrderByTimestampDesc("EMP-001"))
                .thenReturn(Optional.empty());
        when(attendanceLogRepository.save(any(AttendanceLog.class)))
                .thenAnswer(i -> i.getArgument(0));

        AttendanceScanResponse response = attendanceKioskService.processScan(request);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("EMP-001", response.getEmployeeId());
        assertEquals("Jane Doe", response.getEmployeeName());
        assertEquals(AttendanceLogType.CLOCK_IN, response.getLogType());
        verify(attendanceLogRepository, times(1)).save(any(AttendanceLog.class));
    }

    @Test
    @DisplayName("Should auto-toggle to CLOCK_OUT when already clocked in")
    void testProcessScan_AutoClockOut() {
        AttendanceScanRequest request = new AttendanceScanRequest();
        request.setQrToken("QR-EMP001-TOKEN");
        request.setKioskDeviceId("Terminal-01");

        AttendanceLog previousLog = new AttendanceLog();
        previousLog.setEmployeeId("EMP-001");
        previousLog.setLogType(AttendanceLogType.CLOCK_IN);
        previousLog.setTimestamp(LocalDateTime.now().minusHours(4));

        when(employeeBadgeRepository.findByQrTokenAndIsActiveTrue("QR-EMP001-TOKEN"))
                .thenReturn(Optional.of(sampleBadge));
        when(attendanceLogRepository.findTopByEmployeeIdOrderByTimestampDesc("EMP-001"))
                .thenReturn(Optional.of(previousLog));
        when(attendanceLogRepository.save(any(AttendanceLog.class)))
                .thenAnswer(i -> i.getArgument(0));

        AttendanceScanResponse response = attendanceKioskService.processScan(request);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(AttendanceLogType.CLOCK_OUT, response.getLogType());
        verify(attendanceLogRepository, times(1)).save(any(AttendanceLog.class));
    }

    @Test
    @DisplayName("Anti-passback debounce: should prevent duplicate punch within 15 seconds")
    void testProcessScan_AntiPassbackDebounce() {
        AttendanceScanRequest request = new AttendanceScanRequest();
        request.setQrToken("QR-EMP001-TOKEN");

        AttendanceLog recentLog = new AttendanceLog();
        recentLog.setEmployeeId("EMP-001");
        recentLog.setLogType(AttendanceLogType.CLOCK_IN);
        recentLog.setStatus(AttendanceStatus.ON_TIME);
        recentLog.setTimestamp(LocalDateTime.now().minusSeconds(5));

        when(employeeBadgeRepository.findByQrTokenAndIsActiveTrue("QR-EMP001-TOKEN"))
                .thenReturn(Optional.of(sampleBadge));
        when(attendanceLogRepository.findTopByEmployeeIdOrderByTimestampDesc("EMP-001"))
                .thenReturn(Optional.of(recentLog));

        AttendanceScanResponse response = attendanceKioskService.processScan(request);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertTrue(response.getMessage().contains("already registered"));
        verify(attendanceLogRepository, never()).save(any(AttendanceLog.class));
    }

    @Test
    @DisplayName("Should validate PIN fallback when PIN code is provided")
    void testProcessScan_ValidPinFallback() {
        AttendanceScanRequest request = new AttendanceScanRequest();
        request.setPinCode("1234");
        request.setKioskDeviceId("Terminal-01");

        when(employeeBadgeRepository.findByPinCodeAndIsActiveTrue("1234"))
                .thenReturn(Optional.of(sampleBadge));
        when(attendanceLogRepository.findTopByEmployeeIdOrderByTimestampDesc("EMP-001"))
                .thenReturn(Optional.empty());
        when(attendanceLogRepository.save(any(AttendanceLog.class)))
                .thenAnswer(i -> i.getArgument(0));

        AttendanceScanResponse response = attendanceKioskService.processScan(request);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("EMP-001", response.getEmployeeId());
    }

    @Test
    @DisplayName("Should reject scan when token or PIN is invalid")
    void testProcessScan_InvalidToken() {
        AttendanceScanRequest request = new AttendanceScanRequest();
        request.setQrToken("INVALID-QR");

        when(employeeBadgeRepository.findByQrTokenAndIsActiveTrue("INVALID-QR"))
                .thenReturn(Optional.empty());

        AttendanceScanResponse response = attendanceKioskService.processScan(request);

        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("Invalid or inactive employee badge.", response.getMessage());
        verify(attendanceLogRepository, never()).save(any(AttendanceLog.class));
    }

    @Test
    @DisplayName("Should aggregate today's summary statistics correctly")
    void testGetTodaySummary() {
        when(employeeBadgeRepository.countByIsActiveTrue()).thenReturn(10L);
        when(attendanceLogRepository.countDistinctClockedInToday(any(LocalDateTime.class))).thenReturn(8L);
        when(attendanceLogRepository.countDistinctLateToday(any(LocalDateTime.class))).thenReturn(2L);

        AttendanceSummaryDTO summary = attendanceKioskService.getTodaySummary();

        assertNotNull(summary);
        assertEquals(10L, summary.getTotalActiveEmployees());
        assertEquals(8L, summary.getPresentToday());
        assertEquals(2L, summary.getLateToday());
        assertEquals(2L, summary.getAbsentToday());
    }
}
