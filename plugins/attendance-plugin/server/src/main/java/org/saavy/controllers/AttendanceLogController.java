package org.saavy.controllers;

import org.saavy.entity.AttendanceLog;
import org.saavy.entity.AttendanceLogDTO;
import org.saavy.services.AttendanceLogService;
import org.saavy.services.JPAService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attendance-logs")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
public class AttendanceLogController extends BaseController<AttendanceLog, AttendanceLogDTO, Long> {

    @Autowired
    private AttendanceLogService attendanceLogService;

    @Override
    protected JPAService<AttendanceLog, AttendanceLogDTO, Long> getService() {
        return attendanceLogService;
    }
}
