package org.saavy.component;

public record UiDetailMetadata(
        String key,
        String title,
        String dialogHeader,
        String parentField,
        String parentValueJSON,
        String messagesJSON,
        String detailEndpoint,
        boolean deleteWithPayload
) {
}
