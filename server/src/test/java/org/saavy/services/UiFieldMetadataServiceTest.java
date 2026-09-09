package org.saavy.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.saavy.component.UiField;
import org.saavy.component.UiFieldMetadata;
import org.saavy.component.UiMaster;
import org.saavy.component.UiMasterMetadata;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UiFieldMetadataServiceTest {

    private UiFieldMetadataService uiFieldMetadataService;

    @BeforeEach
    void setUp() {
        uiFieldMetadataService = new UiFieldMetadataService(Collections.emptyList());
    }

    // Sample Test DTO
    @UiMaster(title = "Sample Management", dialogHeader = "Sample Record", optionLabel = "sampleName")
    public static class SampleTestDTO {
        @UiField(label = "ID", hidden = true)
        private Long id;

        @UiField(label = "Sample Name", required = true)
        private String sampleName;

        @UiField(label = "Amount", type = "number")
        private Double amount;

        @UiField(label = "Is Active", type = "boolean")
        private Boolean active;

        public Long getId() { return id; }
        public String getSampleName() { return sampleName; }
        public Double getAmount() { return amount; }
        public Boolean getActive() { return active; }
    }

    @Test
    @DisplayName("Should extract fields metadata accurately from annotated DTO")
    void testGetUiFields() {
        List<UiFieldMetadata> fields = uiFieldMetadataService.getUiFields(SampleTestDTO.class);

        assertNotNull(fields);
        assertEquals(4, fields.size());

        UiFieldMetadata idField = fields.stream()
                .filter(f -> "id".equals(f.name()))
                .findFirst()
                .orElse(null);
        assertNotNull(idField);
        assertEquals("ID", idField.label());
        assertTrue(idField.hidden());

        UiFieldMetadata nameField = fields.stream()
                .filter(f -> "sampleName".equals(f.name()))
                .findFirst()
                .orElse(null);
        assertNotNull(nameField);
        assertEquals("Sample Name", nameField.label());
        assertTrue(nameField.required());

        UiFieldMetadata amountField = fields.stream()
                .filter(f -> "amount".equals(f.name()))
                .findFirst()
                .orElse(null);
        assertNotNull(amountField);
        assertEquals("number", amountField.type());
    }

    @Test
    @DisplayName("Should extract master metadata (title, dialogHeader, optionLabel)")
    void testGetUiMaster() {
        UiMasterMetadata masterMeta = uiFieldMetadataService.getUiMaster(SampleTestDTO.class);

        assertNotNull(masterMeta);
        assertEquals("Sample Management", masterMeta.title());
        assertEquals("Sample Record", masterMeta.dialogHeader());
        assertEquals("sampleName", masterMeta.optionLabel());
    }
}
