package org.saavy.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.saavy.reference.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "invoice_number")
    public String invoiceNumber;

    @ManyToOne
    @JoinColumn(name = "party_id")
    public Party party;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tax_category_id")
    public TaxCategory taxCategory;

    @Column(name = "cwt_2307_amount", precision = 38, scale = 2)
    public BigDecimal cwt2307Amount = BigDecimal.ZERO;

    @Column(name = "vat_status", length = 50)
    public String vatStatus = "VAT_INCLUSIVE";

    @Column(name = "is_deductible")
    public Boolean isDeductible = true;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "invoice_status", length = 50)
    public InvoiceStatus invoiceStatus;

    @Column(name = "invoice_date")
    public LocalDate invoiceDate;

    @Column(name = "due_date")
    public LocalDate dueDate;

    @Column(name = "total_amount")
    public BigDecimal totalAmount;

    @Transient
    private BigDecimal outstandingAmount;

    public BigDecimal getOutstandingAmount() {
        if (totalAmount == null) {
            return BigDecimal.ZERO;
        }
        if (payments == null || payments.isEmpty()) {
            return totalAmount;
        }
        BigDecimal totalPaid = payments.stream()
                .map(Payment::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return totalAmount.subtract(totalPaid);
    }

    public void recalculateStatus(List<Payment> currentPayments) {
        BigDecimal total = this.totalAmount != null ? this.totalAmount : BigDecimal.ZERO;
        BigDecimal totalPaid = BigDecimal.ZERO;
        if (currentPayments != null && !currentPayments.isEmpty()) {
            totalPaid = currentPayments.stream()
                    .map(Payment::getAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        if (totalPaid.compareTo(BigDecimal.ZERO) <= 0) {
            this.invoiceStatus = (this instanceof VendorInvoice)
                    ? InvoiceStatus.RECEIVED
                    : InvoiceStatus.AWAITING_PAYMENT;
        } else {
            int comparison = totalPaid.compareTo(total);
            if (comparison < 0) {
                this.invoiceStatus = InvoiceStatus.PARTIALLY_PAID;
            } else if (comparison == 0) {
                this.invoiceStatus = InvoiceStatus.PAID;
            } else {
                this.invoiceStatus = InvoiceStatus.OVERPAID;
            }
        }
    }

    public void recalculateStatus() {
        recalculateStatus(this.payments);
    }

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL)
    public List<InvoiceFile> files;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL)
    public List<Payment> payments;
}
