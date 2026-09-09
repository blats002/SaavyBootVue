package org.saavy.config;

import org.saavy.component.SecurityCustomizerProvider;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Attendance Plugin Security Customizer
 * Dynamically registers attendance endpoints that do not require authentication (e.g. Bundy Clock Kiosk).
 */
@Component
public class AttendanceSecurityProvider implements SecurityCustomizerProvider {

    @Override
    public List<String> getPublicEndpoints() {
        return List.of(
                "/api/attendance/**"
        );
    }
}
