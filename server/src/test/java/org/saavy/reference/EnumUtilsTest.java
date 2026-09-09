package org.saavy.reference;

import org.junit.jupiter.api.Test;
import org.saavy.component.UiField;
import org.saavy.component.UiFieldMetadata;
import org.saavy.services.UiFieldMetadataService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EnumUtilsTest {

    enum StatusWithLabel {
        ON_TIME("On Time"),
        LATE("Late"),
        EARLY_DEPARTURE("Early Departure");

        private final String label;

        StatusWithLabel(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    enum SimpleStatus {
        ACTIVE,
        IN_PROGRESS,
        COMPLETED
    }

    static class SampleDTO {
        @UiField(label = "Status", order = 1)
        private StatusWithLabel status;

        @UiField(label = "Simple Status", order = 2)
        private SimpleStatus simpleStatus;
    }

    @Test
    void testToOptionsWithLabelGetter() {
        List<Map<String, String>> options = EnumUtils.toOptions(StatusWithLabel.class);
        assertEquals(3, options.size());

        assertEquals("On Time", options.get(0).get("label"));
        assertEquals("ON_TIME", options.get(0).get("value"));

        assertEquals("Late", options.get(1).get("label"));
        assertEquals("LATE", options.get(1).get("value"));

        assertEquals("Early Departure", options.get(2).get("label"));
        assertEquals("EARLY_DEPARTURE", options.get(2).get("value"));
    }

    @Test
    void testToOptionsFallbackHumanize() {
        List<Map<String, String>> options = EnumUtils.toOptions(SimpleStatus.class);
        assertEquals(3, options.size());

        assertEquals("Active", options.get(0).get("label"));
        assertEquals("ACTIVE", options.get(0).get("value"));

        assertEquals("In Progress", options.get(1).get("label"));
        assertEquals("IN_PROGRESS", options.get(1).get("value"));

        assertEquals("Completed", options.get(2).get("label"));
        assertEquals("COMPLETED", options.get(2).get("value"));
    }

    @Test
    void testToJson() {
        String json = EnumUtils.toJson(StatusWithLabel.class);
        assertNotNull(json);
        assertTrue(json.contains("{\"label\":\"On Time\",\"value\":\"ON_TIME\"}"));
        assertTrue(json.contains("{\"label\":\"Late\",\"value\":\"LATE\"}"));
        assertTrue(json.contains("{\"label\":\"Early Departure\",\"value\":\"EARLY_DEPARTURE\"}"));
    }

    @Test
    void testUiFieldMetadataServiceAutoPopulatesEnumOptions() {
        UiFieldMetadataService service = new UiFieldMetadataService(Collections.emptyList());
        List<UiFieldMetadata> fields = service.getUiFields(SampleDTO.class);

        assertEquals(2, fields.size());

        UiFieldMetadata statusField = fields.get(0);
        assertEquals("status", statusField.name());
        assertEquals("enum", statusField.type());
        assertNotNull(statusField.enumOptionsJSON());
        assertTrue(statusField.enumOptionsJSON().contains("On Time"));
        assertTrue(statusField.enumOptionsJSON().contains("ON_TIME"));

        UiFieldMetadata simpleField = fields.get(1);
        assertEquals("simpleStatus", simpleField.name());
        assertEquals("enum", simpleField.type());
        assertNotNull(simpleField.enumOptionsJSON());
        assertTrue(simpleField.enumOptionsJSON().contains("In Progress"));
        assertTrue(simpleField.enumOptionsJSON().contains("IN_PROGRESS"));
    }
}
