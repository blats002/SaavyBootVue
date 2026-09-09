package org.saavy.config;

import org.saavy.component.EntityRegistryProvider;
import org.saavy.entity.AttendanceLogDTO;
import org.saavy.entity.EmployeeBadgeDTO;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AttendanceEntityRegistryProvider implements EntityRegistryProvider {

    @Override
    public Map<String, Class<?>> getEntities() {
        return Map.ofEntries(
                Map.entry("employee-badges", EmployeeBadgeDTO.class),
                Map.entry("attendance-logs", AttendanceLogDTO.class)
        );
    }
}
