package org.saavy.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.saavy.entity.PluginConfig;
import org.saavy.entity.PluginConfigDTO;
import org.saavy.entity.PluginConfigRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PluginConfigServiceTest {

    @Mock
    private PluginConfigRepository pluginConfigRepository;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    private PluginConfigService pluginConfigService;

    @Test
    @DisplayName("Should return true when plugin is enabled")
    void testIsPluginEnabled_True() {
        PluginConfig config = new PluginConfig();
        config.setName("attendance-plugin");
        config.setEnabled(true);

        when(pluginConfigRepository.findByName("attendance-plugin")).thenReturn(Optional.of(config));

        boolean enabled = pluginConfigService.isPluginEnabled("attendance-plugin");
        assertTrue(enabled);
    }

    @Test
    @DisplayName("Should return false when plugin is disabled")
    void testIsPluginEnabled_False() {
        PluginConfig config = new PluginConfig();
        config.setName("invoice-plugin");
        config.setEnabled(false);

        when(pluginConfigRepository.findByName("invoice-plugin")).thenReturn(Optional.of(config));

        boolean enabled = pluginConfigService.isPluginEnabled("invoice-plugin");
        assertFalse(enabled);
    }

    @Test
    @DisplayName("Should toggle plugin status successfully")
    void testTogglePlugin() {
        PluginConfig config = new PluginConfig();
        config.setId(1L);
        config.setName("attendance-plugin");
        config.setEnabled(true);

        when(pluginConfigRepository.findById(1L)).thenReturn(Optional.of(config));
        when(pluginConfigRepository.save(any(PluginConfig.class))).thenAnswer(i -> i.getArgument(0));

        PluginConfigDTO toggled = pluginConfigService.togglePlugin(1L, false);

        assertNotNull(toggled);
        assertFalse(toggled.getEnabled());
        verify(pluginConfigRepository, times(1)).save(config);
    }

    @Test
    @DisplayName("Should return active plugin map")
    void testGetActivePluginMap() {
        PluginConfig p1 = new PluginConfig();
        p1.setName("attendance-plugin");
        p1.setEnabled(true);

        PluginConfig p2 = new PluginConfig();
        p2.setName("invoice-plugin");
        p2.setEnabled(false);

        when(pluginConfigRepository.findAll()).thenReturn(List.of(p1, p2));

        Map<String, Boolean> activeMap = pluginConfigService.getActivePluginMap();

        assertEquals(2, activeMap.size());
        assertTrue(activeMap.get("attendance-plugin"));
        assertFalse(activeMap.get("invoice-plugin"));
    }
}
