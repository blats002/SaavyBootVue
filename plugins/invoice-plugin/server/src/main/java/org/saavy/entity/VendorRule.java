package org.saavy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "vendor_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VendorRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pattern", length = 255, nullable = false)
    private String pattern; // e.g. "MERALCO", "PLDT", "AWS", "SHOPEE"

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tax_category_id", nullable = false)
    private TaxCategory taxCategory;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
