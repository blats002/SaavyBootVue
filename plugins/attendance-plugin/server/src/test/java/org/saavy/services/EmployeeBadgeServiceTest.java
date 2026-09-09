package org.saavy.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.saavy.config.ModelMapperConfig;
import org.saavy.entity.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeBadgeServiceTest {

    @Mock
    private EmployeeBadgeRepository employeeBadgeRepository;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    private EmployeeBadgeService employeeBadgeService;

    private EmployeeBadge sampleBadge;

    @BeforeEach
    void setUp() {
        sampleBadge = new EmployeeBadge();
        sampleBadge.setId(1L);
        sampleBadge.setEmployeeId("EMP-100");
        sampleBadge.setEmployeeName("Alice Smith");
        sampleBadge.setDepartment("Finance");
        sampleBadge.setQrToken("QR-100");
        sampleBadge.setPinCode("4321");
        sampleBadge.setIsActive(true);
    }

    @Test
    @DisplayName("Should convert entity to DTO correctly")
    void testToDTO() {
        EmployeeBadgeDTO dto = employeeBadgeService.toDTO(sampleBadge);

        assertNotNull(dto);
        assertEquals("EMP-100", dto.getEmployeeId());
        assertEquals("Alice Smith", dto.getEmployeeName());
        assertEquals("Finance", dto.getDepartment());
    }

    @Test
    @DisplayName("Should preserve existing avatar when updating badge entity without replacing file")
    void testPreserveExistingAvatarOnUpdate() {
        AvatarFile existingAvatar = new AvatarFile();
        existingAvatar.setId(99L);
        existingAvatar.setFileName("avatar.png");
        sampleBadge.setAvatarFile(existingAvatar);

        when(employeeBadgeRepository.findById(1L)).thenReturn(Optional.of(sampleBadge));

        EmployeeBadgeDTO dto = new EmployeeBadgeDTO();
        dto.setId(1L);
        dto.setEmployeeId("EMP-100");
        dto.setEmployeeName("Alice Smith Updated");
        dto.setAvatarFile(new AvatarFileDTO()); // DTO has avatarFile without ID

        EmployeeBadge entity = employeeBadgeService.toEntity(dto, 1L);

        assertNotNull(entity);
        assertNotNull(entity.getAvatarFile());
        assertEquals(99L, entity.getAvatarFile().getId());
    }
}
