package org.saavy.controllers;

import org.saavy.entity.AttendanceLog;
import org.saavy.entity.AttendanceScanRequest;
import org.saavy.entity.AttendanceScanResponse;
import org.saavy.entity.AttendanceSummaryDTO;
import org.saavy.services.AttendanceKioskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceKioskController {

    @Autowired
    private AttendanceKioskService attendanceKioskService;

    @PostMapping("/scan-qr")
    public ResponseEntity<AttendanceScanResponse> processScan(@RequestBody AttendanceScanRequest request) {
        AttendanceScanResponse response = attendanceKioskService.processScan(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/today-summary")
    public ResponseEntity<AttendanceSummaryDTO> getTodaySummary() {
        return ResponseEntity.ok(attendanceKioskService.getTodaySummary());
    }

    @GetMapping("/live-feed")
    public ResponseEntity<List<AttendanceLog>> getLiveFeed() {
        return ResponseEntity.ok(attendanceKioskService.getLiveFeed());
    }
}
