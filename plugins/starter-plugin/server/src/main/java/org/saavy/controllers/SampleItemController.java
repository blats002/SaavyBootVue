package org.saavy.controllers;

import org.saavy.entity.SampleItem;
import org.saavy.entity.SampleItemDTO;
import org.saavy.services.JPAService;
import org.saavy.services.SampleItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sample-items")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
public class SampleItemController extends BaseController<SampleItem, SampleItemDTO, Long> {

    @Autowired
    private SampleItemService sampleItemService;

    @Override
    protected JPAService<SampleItem, SampleItemDTO, Long> getService() {
        return sampleItemService;
    }
}
