package org.saavy.services;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import org.saavy.component.*;
import org.saavy.entity.*;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class UiFieldMetadataService {

    private final Map<String, Class<?>> entityRegistry = Map.of(
            "supplier", Supplier.class,
            "product" , Product.class,
            "stock" , Stock.class
    );

    public Map<String, Class<?>> getEntityRegistry() {
        return entityRegistry;
    }

    public List<UiFieldMetadata> getUiFields(Class<?> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(UiField.class))
                .map(this::toMetadata)
                .sorted(Comparator.comparingInt(UiFieldMetadata::order))
                .toList();

    }

    public List<UiDetailMetadata> getUiDetail(Class<?> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(UiDetail.class))
                .map(this::toUiDetailMetadata)
                .toList();
    }

    private UiDetailMetadata toUiDetailMetadata(Field field) {
        UiDetail detail = field.getAnnotation(UiDetail.class);
        Column column = field.getAnnotation(Column.class);

        return new UiDetailMetadata(
                detail.key(),
                detail.title(),
                detail.dialogHeader(),
                detail.parentField(),
                detail.parentValue(),
                detail.messages(),
                detail.detailEndpoint()
        );
    }

    public UiMasterMetadata getUiMaster(Class<?> entityClass) {
        if (entityClass.isAnnotationPresent(UiMaster.class)) {
            UiMaster uiMaster = entityClass.getAnnotation(UiMaster.class);
            return toUiMasterMetadata(uiMaster);
        }
        return null;
    }

    private UiMasterMetadata toUiMasterMetadata(UiMaster master) {
        return new UiMasterMetadata(
                master.title(),
                master.dialogHeader(),
                master.optionLabel(),
                master.messages(),
                master.masterEndPoint()
        );
    }

    public boolean hasMethod(Class cls, String methodName) {
        try {
            // Look for the method (assuming no parameters)
            Method method = cls.getMethod(methodName);
            return method != null;
        } catch (NoSuchMethodException e) {
            // Method does not exist
            return false;
        }
    }

    private String getTargetEntity(Field field) {
        OneToMany oneToMany = field.getAnnotation(OneToMany.class);
        return oneToMany.targetEntity().getName();
    }

    private UiFieldMetadata toMetadata(Field field) {
        UiField uiField = field.getAnnotation(UiField.class);
        Column column = field.getAnnotation(Column.class);

        boolean isId = field.isAnnotationPresent(Id.class);

        boolean required = uiField.required();

        if (column != null && !column.nullable()) {
            required = true;
        }

        return new UiFieldMetadata(
                field.getName(),
                uiField.label().isBlank() ? toLabel(field.getName()) : uiField.label(),
                uiField.type().isBlank() ? toInputType(field, field.getType()) : uiField.type(),
                uiField.sortable(),
                uiField.hidden(),
                uiField.editable() && !isId,
                required,
                uiField.order(),
                uiField.optionLabel(),
                uiField.optionValue(),
                uiField.optionsEndpoint(),
                uiField.enumOptions()
        );
    }

    private String toLabel(String name) {
        if ("id".equalsIgnoreCase(name)) {
            return "ID";
        }

        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private String toInputType(Class<?> type) {
        if (Number.class.isAssignableFrom(type)
                || type == int.class
                || type == long.class
                || type == double.class
                || type == float.class) {
            return "number";
        }

        if (type == Boolean.class || type == boolean.class) {
            return "checkbox";
        }

        if (type.isEnum()) {
            return "enum";
        }

        if (type == LocalDate.class || type == Date.class) {
            return "date";
        }

        if (type == LocalDateTime.class) {
            return "datetime";
        }
        
        return "text";
    }

    private String toInputType(Field field, Class<?> type) {
        if (field.isAnnotationPresent(ManyToOne.class)) {
            return "manyToOne";
        }
        if (field.isAnnotationPresent(OneToMany.class)) {
            return "oneToMany";
        }

        // Check for textarea - String fields with large column length
        if (type == String.class) {
            Column column = field.getAnnotation(Column.class);
            if (column != null && column.length() > 255) {
                return "textarea";
            }
        }

        return toInputType(type);
    }
}
