package org.saavy.controllers;

import org.saavy.entity.Role;
import org.saavy.entity.RoleDTO;
import org.saavy.entity.UserRoleDTO;
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

    @GetMapping({"/by-user/{userId}", "/by-users/{userId}"})
    public List<UserRoleDTO> getRolesByUserId(@PathVariable Long userId) {
        return userService.getUserRolesByUserId(userId);
    }

    @GetMapping({"/by-user/{userId}/page", "/by-users/{userId}/page"})
    public Page<UserRoleDTO> getRolesByUserIdWithPage(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int rows,
            @RequestParam(defaultValue = "") String search
    ) {
        return userService.getUserRolesByUserIdWithPage(userId, page, rows, search);
    }
}
