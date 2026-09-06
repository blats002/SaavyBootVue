package org.saavy.controllers;

import org.saavy.entity.User;
import org.saavy.entity.UserDTO;
import org.saavy.entity.UserRoleDTO;
import org.saavy.services.JPAService;
import org.saavy.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController extends BaseController<User, UserDTO, Long> {

    @Autowired
    private UserService userService;

    @Override
    protected JPAService<User, UserDTO, Long> getService() {
        return userService;
    }
}
