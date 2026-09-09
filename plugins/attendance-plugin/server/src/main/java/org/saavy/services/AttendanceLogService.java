package org.saavy.services;

import org.modelmapper.ModelMapper;
import org.saavy.entity.AttendanceLog;
import org.saavy.entity.AttendanceLogDTO;
import org.saavy.entity.AttendanceLogRepository;
import org.saavy.reference.BaseJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AttendanceLogService extends JPAService<AttendanceLog, AttendanceLogDTO, Long> {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AttendanceLogRepository attendanceLogRepository;

    @Override
    protected BaseJpaRepository<AttendanceLog, Long> getJpaRepository() {
        return attendanceLogRepository;
    }

    @Override
    public AttendanceLogDTO toDTO(AttendanceLog entity) {
        if (entity == null) return null;
        return modelMapper.map(entity, AttendanceLogDTO.class);
    }

    @Override
    public AttendanceLog toEntity(AttendanceLogDTO dto, Long id) {
        if (dto == null) return null;
        AttendanceLog entity = modelMapper.map(dto, AttendanceLog.class);
        entity.setId(id != null ? id : dto.getId());
        if (entity.getTimestamp() == null) {
            entity.setTimestamp(LocalDateTime.now());
        }
        return entity;
    }
}
