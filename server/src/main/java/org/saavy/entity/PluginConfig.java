package org.saavy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "plugin_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PluginConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "display_name", nullable = false, length = 150)
    private String displayName;

    @Column(name = "dashboard_title", length = 150)
    private String dashboardTitle;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "version", length = 50)
    private String version = "1.0.0";

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    @PreUpdate
    public void onSave() {
        this.updatedAt = LocalDateTime.now();
    }
}
