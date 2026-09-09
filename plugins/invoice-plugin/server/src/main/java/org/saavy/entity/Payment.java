package org.saavy.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.saavy.component.UiField;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment extends BaseFile{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "amount")
    public BigDecimal amount;

    @Column(name = "payment_date")
    public LocalDate paymentDate;

    @Column(name = "reference")
    public String reference;

    @ManyToOne
    @JoinColumn(name = "invoice_id")
    public Invoice invoice;
}

