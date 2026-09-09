package org.saavy.controllers;

import org.saavy.entity.EmployeeBadge;
import org.saavy.entity.EmployeeBadgeDTO;
import org.saavy.services.EmployeeBadgeService;
import org.saavy.services.JPAService;
import org.springframework.beans.factory.annotation.Autowired;
import org.saavy.entity.AvatarFileDTO;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/employee-badges")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
public class EmployeeBadgeController extends BaseController<EmployeeBadge, EmployeeBadgeDTO, Long> {

    @Autowired
    private EmployeeBadgeService employeeBadgeService;

    @Override
    protected JPAService<EmployeeBadge, EmployeeBadgeDTO, Long> getService() {
        return employeeBadgeService;
    }

    @PostMapping(value = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AvatarFileDTO> uploadAvatar(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            AvatarFileDTO avatarDTO = employeeBadgeService.uploadAvatar(id, file);
            return ResponseEntity.ok(avatarDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
