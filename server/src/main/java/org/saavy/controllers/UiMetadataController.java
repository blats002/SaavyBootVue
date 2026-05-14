package org.saavy.controllers;

import org.saavy.component.UiDetailMetadata;
import org.saavy.component.UiFieldMetadata;
import org.saavy.component.UiMasterMetadata;
import org.saavy.services.UiFieldMetadataService;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

@RestController
@RequestMapping("/api/metadata")
public class UiMetadataController {

    private final UiFieldMetadataService uiFieldMetadataService;



    public UiMetadataController(UiFieldMetadataService uiFieldMetadataService) {
        this.uiFieldMetadataService = uiFieldMetadataService;
    }

    @GetMapping("/{resource}/fields")
    public List<UiFieldMetadata> getFields(@PathVariable String resource) {
        Class<?> entityClass = uiFieldMetadataService.getEntityRegistry().get(resource);

        if (entityClass == null) {
            throw new IllegalArgumentException("Unknown metadata resource: " + resource);
        }

        return uiFieldMetadataService.getUiFields(entityClass);
    }

    @GetMapping("/{resource}/mastermeta")
    public UiMasterMetadata getMasterFields(@PathVariable String resource) {
        Class<?> entityClass = uiFieldMetadataService.getEntityRegistry().get(resource);

        if (entityClass == null) {
            throw new IllegalArgumentException("Unknown metadata resource: " + resource);
        }

        return uiFieldMetadataService.getUiMaster(entityClass);
    }
    @GetMapping("/{resource}/detailmeta")
    public List<UiDetailMetadata> getDetailFields(@PathVariable String resource) {
        Class<?> entityClass = uiFieldMetadataService.getEntityRegistry().get(resource);

        if (entityClass == null) {
            throw new IllegalArgumentException("Unknown metadata resource: " + resource);
        }

        return uiFieldMetadataService.getUiDetail(entityClass);
    }
}
