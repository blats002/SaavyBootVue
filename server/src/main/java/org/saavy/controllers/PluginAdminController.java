package org.saavy.controllers;

import org.saavy.entity.PluginConfig;
import org.saavy.entity.PluginConfigDTO;
import org.saavy.services.JPAService;
import org.saavy.services.PluginConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/plugins")
public class PluginAdminController extends BaseController<PluginConfig, PluginConfigDTO, Long> {

    @Autowired
    private PluginConfigService pluginConfigService;

    @Override
    protected JPAService<PluginConfig, PluginConfigDTO, Long> getService() {
        return pluginConfigService;
    }

    /**
     * Endpoint to fetch active/enabled state map of all registered plugins.
     * Accessible to all authenticated users for frontend menu & routing checks.
     */
    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Boolean>> getActivePlugins() {
        return ResponseEntity.ok(pluginConfigService.getActivePluginMap());
    }

    /**
     * Toggle or set plugin enabled state (Restricted strictly to ROLE_ADMIN).
     */
    @PutMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PluginConfigDTO> togglePlugin(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Boolean> payload) {
        Boolean enabled = (payload != null && payload.containsKey("enabled")) ? payload.get("enabled") : null;
        PluginConfigDTO updated = pluginConfigService.togglePlugin(id, enabled);
        return ResponseEntity.ok(updated);
    }
}
