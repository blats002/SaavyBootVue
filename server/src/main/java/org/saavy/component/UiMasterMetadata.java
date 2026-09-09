package org.saavy.component;

public record UiMasterMetadata(
        String title,
        String dialogHeader,
        String optionLabel,
        String messagesJSON,
        String masterEndPoint,
        boolean deletable,
        String deletableField
) {
}
