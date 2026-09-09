package org.saavy.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.saavy.component.UiField;
import org.saavy.component.UiMaster;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@UiMaster(
        title = "Plugin Management",
        dialogHeader = "Plugin Details",
        optionLabel = "displayName",
        masterEndPoint = "plugins",
        deletable = false,
        messages = "{\"created\":\"Plugin Configuration Created\"" +
                ",\"updated\":\"Plugin Configuration Updated\"" +
                ",\"deleted\":\"Plugin Configuration Deleted\"" +
                ",\"deletedMany\":\"Plugin Configurations Deleted\"" +
                "}"
)
public class PluginConfigDTO implements Serializable {

    @UiField(label = "ID", type = "number", hidden = true, editable = false, order = 1)
    private Long id;

    @UiField(label = "Plugin Identifier", type = "text", required = true, sortable = true, editable = false, order = 2)
    private String name;

    @UiField(label = "Display Name", type = "text", required = true, sortable = true, order = 3)
    private String displayName;

    @UiField(label = "Dashboard Title", type = "text", sortable = true, order = 4)
    private String dashboardTitle;

    @UiField(label = "Description", type = "text", sortable = true, order = 5)
    private String description;

    @UiField(label = "Version", type = "text", sortable = true, order = 6)
    private String version;

    @UiField(label = "Enabled", type = "checkbox", sortable = true, order = 7)
    private Boolean enabled;

    private LocalDateTime updatedAt;
}
