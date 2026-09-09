package org.saavy.component;

import java.util.List;

/**
 * Extension SPI for plugins/modules to contribute public request matchers
 * to Spring Security without hardcoding plugin endpoints in the core SecurityConfig.
 */
public interface SecurityCustomizerProvider {

    /**
     * URL patterns to permit all (public access without authentication).
     * e.g., List.of("/api/attendance/**")
     */
    default List<String> getPublicEndpoints() {
        return List.of();
    }
}
