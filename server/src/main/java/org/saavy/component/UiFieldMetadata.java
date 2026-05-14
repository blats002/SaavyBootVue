package org.saavy.component;

public record UiFieldMetadata(
        String name,
        String label,
        String type,
        boolean sortable,
        boolean hidden,
        boolean editable,
        boolean required,
        int order,
        String optionLabel,
        String optionValue,
        String optionsEndpoint,
        String enumOptionsJSON,
        String fileNameField,
        String contentTypeField
) {
}
