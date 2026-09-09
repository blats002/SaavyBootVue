package org.saavy.reference;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Method;
import java.util.*;

public final class EnumUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private EnumUtils() {
        // Utility class
    }

    /**
     * Converts an Enum class into a JSON array string of label/value objects:
     * [
     *   { "label": "On Time", "value": "ON_TIME" },
     *   ...
     * ]
     *
     * @param enumClass the enum class to convert
     * @return JSON array string representation
     */
    public static String toJson(Class<? extends Enum<?>> enumClass) {
        if (enumClass == null || !enumClass.isEnum()) {
            return "[]";
        }

        try {
            return OBJECT_MAPPER.writeValueAsString(toOptions(enumClass));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize enum options to JSON for " + enumClass.getName(), e);
        }
    }

    /**
     * Extracts a list of maps containing "label" and "value" keys from the enum constants.
     *
     * @param enumClass the enum class to convert
     * @return list of options maps
     */
    public static List<Map<String, String>> toOptions(Class<? extends Enum<?>> enumClass) {
        if (enumClass == null || !enumClass.isEnum()) {
            return Collections.emptyList();
        }

        List<Map<String, String>> options = new ArrayList<>();
        Enum<?>[] constants = enumClass.getEnumConstants();
        if (constants == null) {
            return options;
        }

        Method labelMethod = findLabelMethod(enumClass);

        for (Enum<?> constant : constants) {
            String value = constant.name();
            String label = null;

            if (labelMethod != null) {
                try {
                    Object result = labelMethod.invoke(constant);
                    if (result != null) {
                        label = result.toString();
                    }
                } catch (Exception ignored) {
                }
            }

            if (label == null || label.isBlank()) {
                label = humanize(value);
            }

            Map<String, String> option = new LinkedHashMap<>();
            option.put("label", label);
            option.put("value", value);
            options.add(option);
        }

        return options;
    }

    private static Method findLabelMethod(Class<?> enumClass) {
        try {
            return enumClass.getMethod("getLabel");
        } catch (NoSuchMethodException e1) {
            try {
                return enumClass.getMethod("label");
            } catch (NoSuchMethodException e2) {
                return null;
            }
        }
    }

    /**
     * Converts SNAKE_CASE or UPPER_CASE identifier to Title Case words.
     * Example: "EARLY_DEPARTURE" -> "Early Departure"
     */
    public static String humanize(String name) {
        if (name == null || name.isBlank()) {
            return "";
        }

        String[] parts = name.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                if (!sb.isEmpty()) {
                    sb.append(" ");
                }
                sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
            }
        }
        return sb.toString();
    }
}
