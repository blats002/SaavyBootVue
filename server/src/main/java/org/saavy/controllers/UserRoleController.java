package org.saavy.controllers;

import org.saavy.entity.RoleDTO;
import org.saavy.entity.UserDTO;
import org.saavy.entity.UserRoleDTO;
import org.saavy.services.RoleService;
import org.saavy.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/userrole")
@PreAuthorize("hasRole('ADMIN')")
public class UserRoleController {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @GetMapping("/by-{field}/{id}")
    public java.util.List<UserRoleDTO> getUserRolesByUserId(@PathVariable Long id) {
        java.util.List<UserRoleDTO> userRoles = userService.getUserRolesByUserId(id);
        userRoles.forEach(userRole -> {
            if (userRole.getUser() != null) {
                userRole.getUser().setRoles(null);
                userRole.getUser().setUserRoles(null);
            }
        });
        return userRoles;
    }

    @GetMapping("/by-{field}/{id}/page")
    public Page<UserRoleDTO> getUserRolesByUserIdWithPage(@PathVariable Long id,
                                                          @RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "0") int rows,
                                                          @RequestParam(defaultValue = "") String search) {
        Page<UserRoleDTO> userRoles = userService.getUserRolesByUserIdWithPage(id, page, rows, search);
        userRoles.forEach(userRole -> {
            if (userRole.getUser() != null) {
                userRole.getUser().setRoles(null);
                userRole.getUser().setUserRoles(null);
            }
        });
        return userRoles;
    }

    @DeleteMapping
    public void delete(@RequestBody UserRoleDTO dto) {
        Long targetUserId = dto.getUser() != null ? dto.getUser().getId() : null ;
        if (targetUserId != null) {
            userService.removeRole(targetUserId, dto.getRole().getId());
        } else {
            throw new RuntimeException("User id is required");
        }
    }

    @PostMapping
    public ResponseEntity<?> save(@RequestBody UserRoleDTO dto) {
        if (dto.getUser() == null || dto.getUser().getId() == null) {
            return ResponseEntity.badRequest().body("User is required");
        }
        if (dto.getRole() == null || dto.getRole().getId() == null) {
            return ResponseEntity.badRequest().body("Role is required");
        }
        Optional<UserDTO> userOpt = userService.findById(dto.getUser().getId());
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        UserDTO userDto = userOpt.get();
        RoleDTO roleDto = roleService.findById(dto.getRole().getId()).orElse(dto.getRole());
        if (userDto != null) {
            userDto.setRoles(null);
            userDto.setUserRoles(null);
        }
        if (userService.save(userDto) != null) {
            return ResponseEntity.ok(new UserRoleDTO(userDto, roleDto));
        }
        return ResponseEntity.internalServerError().build();
    }
}
