package org.saavy.services;

import org.modelmapper.ModelMapper;
import org.saavy.entity.EmployeeBadge;
import org.saavy.entity.EmployeeBadgeDTO;
import org.saavy.entity.EmployeeBadgeRepository;
import org.saavy.reference.BaseJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.saavy.entity.AvatarFile;
import org.saavy.entity.AvatarFileDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

@Service
public class EmployeeBadgeService extends JPAService<EmployeeBadge, EmployeeBadgeDTO, Long> {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private EmployeeBadgeRepository employeeBadgeRepository;

    @Override
    protected BaseJpaRepository<EmployeeBadge, Long> getJpaRepository() {
        return employeeBadgeRepository;
    }

    @Override
    public EmployeeBadgeDTO toDTO(EmployeeBadge entity) {
        if (entity == null) return null;
        return modelMapper.map(entity, EmployeeBadgeDTO.class);
    }

    @Override
    public EmployeeBadge toEntity(EmployeeBadgeDTO dto, Long id) {
        if (dto == null) return null;
        Long badgeId = id != null ? id : dto.getId();
        EmployeeBadge entity = modelMapper.map(dto, EmployeeBadge.class);
        entity.setId(badgeId);
        if (entity.getQrToken() == null || entity.getQrToken().isBlank()) {
            entity.setQrToken(UUID.randomUUID().toString());
        }

        if (badgeId != null && entity.getAvatarFile() != null && entity.getAvatarFile().getId() == null) {
            employeeBadgeRepository.findById(badgeId).ifPresent(existing -> {
                if (existing.getAvatarFile() != null) {
                    entity.getAvatarFile().setId(existing.getAvatarFile().getId());
                }
            });
        }
        return entity;
    }

    public AvatarFileDTO uploadAvatar(Long badgeId, MultipartFile file) throws IOException {
        EmployeeBadge badge = employeeBadgeRepository.findById(badgeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee badge not found with id: " + badgeId));

        AvatarFile avatarFile = badge.getAvatarFile();
        if (avatarFile == null) {
            avatarFile = new AvatarFile();
        }

        avatarFile.setFileName(file.getOriginalFilename());
        avatarFile.setContentType(file.getContentType());
        String base64Content = "data:" + file.getContentType() + ";base64," + Base64.getEncoder().encodeToString(file.getBytes());
        avatarFile.setContent(base64Content);

        badge.setAvatarFile(avatarFile);
        employeeBadgeRepository.save(badge);

        return modelMapper.map(avatarFile, AvatarFileDTO.class);
    }
}
