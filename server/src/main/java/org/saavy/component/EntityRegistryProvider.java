package org.saavy.component;

import java.util.Map;

/**
 * Extension SPI for downstream projects/modules to contribute entity DTO classes
 * to the dynamic UiFieldMetadataService without modifying the core framework.
 */
public interface EntityRegistryProvider {
    Map<String, Class<?>> getEntities();
}
