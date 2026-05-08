package org.saavy.component;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "parent-fields")
public class ParentFieldRegistry {

    private Map<String, String> fields = new HashMap<>();

    public String resolve(String field) {
        String resolved = fields.get(field.toLowerCase());
        if (resolved == null) {
            throw new IllegalArgumentException("Unsupported parent field");
        }
        return resolved;
    }

    public void setFields(Map<String, String> fields) {
        this.fields = fields;
    }
}


