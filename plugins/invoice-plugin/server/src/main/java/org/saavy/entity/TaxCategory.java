package org.saavy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tax_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaxCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", length = 100, nullable = false, unique = true)
    private String code;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "pnl_category", length = 50, nullable = false)
    private String pnlCategory; // REVENUE, COGS, OPEX, OTHER_INCOME, OTHER_EXPENSE

    @Column(name = "bir_schedule_line", length = 255)
    private String birScheduleLine;

    @Column(name = "is_deductible", nullable = false)
    private Boolean isDeductible = true;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = true;

    @Column(name = "statutory_cap_percent", precision = 5, scale = 2)
    private BigDecimal statutoryCapPercent;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
