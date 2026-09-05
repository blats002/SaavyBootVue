package org.saavy.controllers;

import org.saavy.entity.Role;
import org.saavy.entity.RoleDTO;
import org.saavy.services.JPAService;
import org.saavy.services.RoleService;
import org.saavy.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@PreAuthorize("hasRole('ADMIN')")
public class RoleController extends BaseController<Role, RoleDTO, Long> {

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserService userService;

    @Override
    protected JPAService<Role, RoleDTO, Long> getService() {
        return roleService;
    }

    @GetMapping("/by-user/{userId}")
    public List<RoleDTO> getRolesByUserId(@PathVariable Long userId) {
        return userService.getRolesByUserId(userId);
    }

    @GetMapping("/by-user/{userId}/page")
    public Page<RoleDTO> getRolesByUserIdWithPage(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int rows,
            @RequestParam(defaultValue = "") String search
    ) {
        return userService.getRolesByUserIdWithPage(userId, page, rows, search);
    }
}
