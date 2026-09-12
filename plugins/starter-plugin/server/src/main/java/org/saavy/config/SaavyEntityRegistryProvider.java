package org.saavy.config;

import org.saavy.component.EntityRegistryProvider;
import org.saavy.entity.SampleItemDTO;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SaavyEntityRegistryProvider implements EntityRegistryProvider {

    @Override
    public Map<String, Class<?>> getEntities() {
        return Map.ofEntries(
                Map.entry("sample-items", SampleItemDTO.class)
        );
    }
}
