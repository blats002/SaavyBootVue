package org.saavy.services;

import org.saavy.entity.Role;
import org.saavy.entity.RoleDTO;
import org.saavy.entity.RoleRepository;
import org.saavy.reference.BaseJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleService extends JPAService<Role, RoleDTO, Long> {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    protected BaseJpaRepository<Role, Long> getJpaRepository() {
        return roleRepository;
    }

    @Override
    public RoleDTO toDTO(Role entity) {
        if (entity == null) {
            return null;
        }
        return new RoleDTO(
                entity.getId(),
                entity.getName(),
                entity.getDescription()
        );
    }

    @Override
    public Role toEntity(RoleDTO dto, Long id) {
        if (dto == null) {
            return null;
        }
        return new Role(
                id != null ? id : dto.getId(),
                dto.getName(),
                dto.getDescription()
        );
    }
}
