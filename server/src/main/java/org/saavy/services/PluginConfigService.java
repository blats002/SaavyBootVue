package org.saavy.services;

import jakarta.annotation.PostConstruct;
import org.modelmapper.ModelMapper;
import org.saavy.entity.PluginConfig;
import org.saavy.entity.PluginConfigDTO;
import org.saavy.entity.PluginConfigRepository;
import org.saavy.reference.BaseJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

@Service
public class PluginConfigService extends JPAService<PluginConfig, PluginConfigDTO, Long> {

    private static final Logger log = LoggerFactory.getLogger(PluginConfigService.class);

    @Autowired
    private PluginConfigRepository pluginConfigRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    protected BaseJpaRepository<PluginConfig, Long> getJpaRepository() {
        return pluginConfigRepository;
    }

    @Override
    public PluginConfigDTO toDTO(PluginConfig entity) {
        if (entity == null) return null;
        return modelMapper.map(entity, PluginConfigDTO.class);
    }

    @Override
    public PluginConfig toEntity(PluginConfigDTO dto, Long id) {
        if (dto == null) return null;
        PluginConfig entity = modelMapper.map(dto, PluginConfig.class);
        if (id != null) {
            entity.setId(id);
        }
        return entity;
    }

    @PostConstruct
    @Transactional
    public void initKnownPlugins() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath*:plugin.properties");

            for (Resource resource : resources) {
                try (InputStream is = resource.getInputStream()) {
                    Properties props = new Properties();
                    props.load(is);

                    String name = props.getProperty("plugin.name");
                    String displayName = props.getProperty("plugin.displayName", name);
                    String dashboardTitle = props.getProperty("plugin.dashboardTitle", displayName != null ? displayName : name);
                    String description = props.getProperty("plugin.description", "");
                    String version = props.getProperty("plugin.version", "1.0.0");

                    if (name != null && !name.isBlank()) {
                        registerDefaultPluginIfMissing(
                                name.trim(),
                                displayName != null ? displayName.trim() : null,
                                dashboardTitle != null ? dashboardTitle.trim() : null,
                                description != null ? description.trim() : "",
                                version != null ? version.trim() : "1.0.0"
                        );
                    }
                } catch (Exception e) {
                    log.warn("Failed to load plugin.properties from resource: {}", resource.getDescription(), e);
                }
            }
        } catch (Exception e) {
            log.error("Failed to scan classpath for plugin.properties", e);
        }
    }

    private void registerDefaultPluginIfMissing(String name, String displayName, String dashboardTitle, String description, String version) {
        try {
            Optional<PluginConfig> existing = pluginConfigRepository.findByName(name);
            if (existing.isEmpty()) {
                PluginConfig config = new PluginConfig();
                config.setName(name);
                config.setDisplayName(displayName != null ? displayName : name);
                config.setDashboardTitle(dashboardTitle != null ? dashboardTitle : displayName);
                config.setDescription(description);
                config.setVersion(version);
                config.setEnabled(true);
                config.setUpdatedAt(LocalDateTime.now());
                pluginConfigRepository.save(config);
                log.info("Registered plugin from properties: {}", name);
            } else {
                PluginConfig config = existing.get();
                boolean updated = false;

                if (displayName != null && !Objects.equals(displayName, config.getDisplayName())) {
                    config.setDisplayName(displayName);
                    updated = true;
                }
                if (dashboardTitle != null && !Objects.equals(dashboardTitle, config.getDashboardTitle())) {
                    config.setDashboardTitle(dashboardTitle);
                    updated = true;
                }
                if (description != null && !Objects.equals(description, config.getDescription())) {
                    config.setDescription(description);
                    updated = true;
                }
                if (version != null && !Objects.equals(version, config.getVersion())) {
                    config.setVersion(version);
                    updated = true;
                }

                if (updated) {
                    config.setUpdatedAt(LocalDateTime.now());
                    pluginConfigRepository.save(config);
                    log.info("Updated plugin '{}' metadata from properties: displayName={}, dashboardTitle={}, version={}",
                            name, displayName, dashboardTitle, version);
                }
            }
        } catch (Exception e) {
            // In case database table is not migrated yet on very first boot cycle
            log.debug("Could not register or update default plugin '{}': {}", name, e.getMessage());
        }
    }

    @Transactional
    public PluginConfigDTO togglePlugin(Long id, Boolean enabled) {
        PluginConfig config = pluginConfigRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Plugin not found with id: " + id));
        config.setEnabled(enabled != null ? enabled : !Boolean.TRUE.equals(config.getEnabled()));
        config.setUpdatedAt(LocalDateTime.now());
        PluginConfig saved = pluginConfigRepository.save(config);
        return toDTO(saved);
    }

    public boolean isPluginEnabled(String name) {
        if (name == null || name.isBlank()) return true;
        return pluginConfigRepository.findByName(name)
                .map(PluginConfig::getEnabled)
                .orElse(true);
    }

    public Map<String, Boolean> getActivePluginMap() {
        Map<String, Boolean> activeMap = new HashMap<>();
        List<PluginConfig> all = pluginConfigRepository.findAll();
        for (PluginConfig p : all) {
            activeMap.put(p.getName(), Boolean.TRUE.equals(p.getEnabled()));
        }
        return activeMap;
    }

    public Map<String, String> getDashboardTitleMap() {
        Map<String, String> titleMap = new HashMap<>();
        List<PluginConfig> all = pluginConfigRepository.findAll();
        for (PluginConfig p : all) {
            if (p.getDashboardTitle() != null && !p.getDashboardTitle().isBlank()) {
                titleMap.put(p.getName(), p.getDashboardTitle());
            } else if (p.getDisplayName() != null && !p.getDisplayName().isBlank()) {
                titleMap.put(p.getName(), p.getDisplayName());
            }
        }
        return titleMap;
    }
}
