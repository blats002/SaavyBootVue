package org.saavy.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Base controller for Dashboard operations.
 */
@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
public abstract class BaseDashBoardController {

    @GetMapping("/cards")
    public abstract List<Map<String, Object>> getCards();

    @GetMapping("/charts")
    public abstract List<Map<String, Object>> getCharts();

    @GetMapping("/tables")
    public abstract List<Map<String, Object>> getTables();

    @GetMapping("/tabs")
    public abstract Map<String, String> getTabs();
}
