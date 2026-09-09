package org.saavy.services;
import org.modelmapper.ModelMapper;
import org.saavy.entity.*;
import org.saavy.reference.BaseJpaRepository;
import org.saavy.reference.ReflectionSearchSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InvoiceFileService extends JPAService<InvoiceFile, InvoiceFileDTO, Long> {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private InvoiceFileRepository invoiceFileRepository;
    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private InvoiceService invoiceService;

    @Override
    protected BaseJpaRepository<InvoiceFile, Long> getJpaRepository() {
        return invoiceFileRepository;
    }

//    @Override
//    public List<InvoiceFileDTO> findByParentId(String field, Long id) {
//        if ("invoice".equals(field)) {
//            return invoiceFileRepository.findAllByInvoiceId(id).stream()
//                    .map(this::toDTO)
//                    .collect(Collectors.toList());
//        }
//        return List.of();
//    }

//    @Override
//    public Page<InvoiceFileDTO> findByParentIdWithPage(String field, Long id,int page, int rows, String search) {
//        if ("invoice".equals(field)) {
//            Pageable pageable = PageRequest.of(page, rows);
//            Specification<InvoiceFile> spec = new ReflectionSearchSpecification<>(search);
//            return invoiceFileRepository.findAllByInvoiceId(id,spec, pageable)
//                    .map(this::toDTO);
//        }
//        return null;
//    }

    // ✅ Mapping: Entity → DTO
    @Override
    public InvoiceFileDTO toDTO(InvoiceFile invoiceFile) {

        InvoiceFileDTO dto = modelMapper.map(invoiceFile, InvoiceFileDTO.class);

        return dto;

//        return new InvoiceFileDTO(
//                invoiceFile.getId(),
//                invoiceFile.getFileName(),
//                invoiceFile.getContentType(),
//                invoiceFile.getContent(),
//                invoiceService.toDTO(invoiceFile.getInvoice())
//        );
    }

    // ✅ Mapping: DTO → Entity
    @Override
    public InvoiceFile toEntity(InvoiceFileDTO dto, Long id) {

        InvoiceFile invoiceFile = modelMapper.map(dto, InvoiceFile.class);
        invoiceFile.setId(id != null?id:dto.getId());

//        InvoiceFile invoiceFile = new InvoiceFile();
//        invoiceFile.setId(id != null?id:dto.getId());
//        invoiceFile.setFileName(dto.getFileName());
//        invoiceFile.setContentType(dto.getContentType());
//        invoiceFile.setContent(dto.getContent());
//        invoiceFile.setInvoice(invoiceRepository.getReferenceById(dto.getInvoice().getId()));
        return invoiceFile;
    }
}
