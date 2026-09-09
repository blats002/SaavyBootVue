package org.saavy.config;

import org.saavy.component.EntityRegistryProvider;
import org.saavy.entity.PluginConfigDTO;
import org.saavy.entity.RoleDTO;
import org.saavy.entity.UserDTO;
import org.saavy.entity.UserRoleDTO;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CoreEntityRegistryProvider implements EntityRegistryProvider {

    @Override
    public Map<String, Class<?>> getEntities() {
        return Map.ofEntries(
                // 1. Register core built-in entities
                Map.entry("users", UserDTO.class),
                Map.entry("roles", RoleDTO.class),
                Map.entry("userrole", UserRoleDTO.class),
                Map.entry("plugins", PluginConfigDTO.class)
        );
    }
}
