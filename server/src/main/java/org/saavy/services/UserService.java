package org.saavy.services;

import org.apache.commons.lang3.StringUtils;
import org.saavy.entity.*;
import org.saavy.reference.BaseJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService extends JPAService<User, UserDTO, Long> {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RoleService roleService;

    @Override
    protected BaseJpaRepository<User, Long> getJpaRepository() {
        return userRepository;
    }

    @Override
    public UserDTO toDTO(User entity) {
        if (entity == null) {
            return null;
        }

        Set<RoleDTO> roleDTOs = entity.getRoles().stream()
                .map(r -> new RoleDTO(r.getId(), r.getName(), r.getDescription()))
                .collect(Collectors.toSet());


        UserDTO dto = new UserDTO();
        dto.setId(entity.getId());
        dto.setUsername(entity.getUsername());
        dto.setFullName(entity.getFullName());
        dto.setEmail(entity.getEmail());
        dto.setPassword(null); // Don't expose password
        dto.setEnabled(entity.getEnabled());
        dto.setRoles(roleDTOs);
        dto.setDisplayString(entity.getFullName() != null ? entity.getFullName() : entity.getUsername());

        java.util.List<UserRoleDTO> userRoleDTOs = userRepository.findById(entity.getId())
                .map(user -> user.getRoles().stream()
                        .map(r -> new UserRoleDTO(dto, roleService.toDTO(r)))
                        .collect(Collectors.toList()))
                .orElse(java.util.Collections.emptyList());

        dto.setUserRoles(userRoleDTOs);

        return dto;
    }

    @Override
    public User toEntity(UserDTO dto, Long id) {
        if (dto == null) {
            return null;
        }

        Long targetId = id != null ? id : dto.getId();
        User entity;

        if (targetId != null) {
            entity = userRepository.findById(targetId).orElse(new User());
            entity.setId(targetId);
        } else {
            entity = new User();
        }

        entity.setUsername(dto.getUsername());
        entity.setEmail(dto.getEmail());
        entity.setFullName(dto.getFullName());
        entity.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : true);

        // Password handling
        if (StringUtils.isNotBlank(dto.getPassword())) {
            entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        } else if (targetId == null) {
            // Default password for new user if none provided
            entity.setPassword(passwordEncoder.encode("password123"));
        }

        // Roles handling
        Set<Role> roles = new HashSet<>();
        if (dto.getRoles() != null && !dto.getRoles().isEmpty()) {
            for (RoleDTO rDto : dto.getRoles()) {
                if (rDto.getId() != null) {
                    roleRepository.findById(rDto.getId()).ifPresent(roles::add);
                } else if (StringUtils.isNotBlank(rDto.getName())) {
                    roleRepository.findByName(rDto.getName()).ifPresent(roles::add);
                }
            }
        }

        if (roles.isEmpty() && targetId == null) {
            // Assign default ROLE_USER if none specified
            roleRepository.findByName("ROLE_USER").ifPresent(roles::add);
        }

        if (!roles.isEmpty() || targetId == null) {
            entity.setRoles(roles);
        }

        return entity;
    }

//    public java.util.List<UserRoleDTO> getUserRolesByUserId(Long userId) {
//        return userRepository.findById(userId)
//                .map(user -> user.getRoles().stream()
//                        .map(r -> new UserRoleDTO(toDTO(user), roleService.toDTO(r)))
//                        .collect(Collectors.toList()))
//                .orElse(java.util.Collections.emptyList());
//    }

    public java.util.List<UserRoleDTO> getUserRolesByUserId(Long userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    // Create a simple UserDTO without userRoles to avoid recursion
                    UserDTO userDTO = new UserDTO();
                    userDTO.setId(user.getId());
                    userDTO.setUsername(user.getUsername());
                    userDTO.setFullName(user.getFullName());
                    userDTO.setEmail(user.getEmail());
                    userDTO.setEnabled(user.getEnabled());
                    userDTO.setDisplayString(user.getFullName() != null ? user.getFullName() : user.getUsername());

                    return user.getRoles().stream()
                            .map(r -> new UserRoleDTO(userDTO, roleService.toDTO(r)))
                            .collect(Collectors.toList());
                })
                .orElse(java.util.Collections.emptyList());
    }

    public org.springframework.data.domain.Page<UserRoleDTO> getUserRolesByUserIdWithPage(Long userId, int page, int rows, String search) {
        java.util.List<UserRoleDTO> roles = getUserRolesByUserId(userId);
        if (StringUtils.isNotBlank(search)) {
            String lowerSearch = search.toLowerCase();
            roles = roles.stream()
                    .filter(r -> (r.getRole().getName() != null && r.getRole().getName().toLowerCase().contains(lowerSearch)) ||
                            (r.getRole().getDescription() != null && r.getRole().getDescription().toLowerCase().contains(lowerSearch)))
                    .collect(Collectors.toList());
        }
        int pageSize = rows > 0 ? rows : (roles.isEmpty() ? 10 : roles.size());
        int start = Math.min(page * pageSize, roles.size());
        int end = Math.min(start + pageSize, roles.size());
        java.util.List<UserRoleDTO> subList = roles.subList(start, end);
        return new org.springframework.data.domain.PageImpl<>(subList, org.springframework.data.domain.PageRequest.of(page, pageSize), roles.size());
    }

    public java.util.List<RoleDTO> getRolesByUserId(Long userId) {
        return userRepository.findById(userId)
                .map(user -> user.getRoles().stream()
                        .map(r -> new RoleDTO(r.getId(), r.getName(), r.getDescription()))
                        .collect(Collectors.toList()))
                .orElse(java.util.Collections.emptyList());
    }

    public org.springframework.data.domain.Page<RoleDTO> getRolesByUserIdWithPage(Long userId, int page, int rows, String search) {
        java.util.List<RoleDTO> roles = getRolesByUserId(userId);
        if (StringUtils.isNotBlank(search)) {
            String lowerSearch = search.toLowerCase();
            roles = roles.stream()
                    .filter(r -> (r.getName() != null && r.getName().toLowerCase().contains(lowerSearch)) ||
                                 (r.getDescription() != null && r.getDescription().toLowerCase().contains(lowerSearch)))
                    .collect(Collectors.toList());
        }
        int pageSize = rows > 0 ? rows : (roles.isEmpty() ? 10 : roles.size());
        int start = Math.min(page * pageSize, roles.size());
        int end = Math.min(start + pageSize, roles.size());
        java.util.List<RoleDTO> subList = roles.subList(start, end);
        return new org.springframework.data.domain.PageImpl<>(subList, org.springframework.data.domain.PageRequest.of(page, pageSize), roles.size());
    }

    public void removeRole(Long userId, Long roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        user.getRoles().removeIf(r -> r.getId().equals(roleId));
        User saved = userRepository.save(user);
        if(saved.getRoles().contains(roleRepository.getReferenceById(roleId))){
            throw new RuntimeException("Role was not removed from user with id: " + userId);
        }
    }
}
