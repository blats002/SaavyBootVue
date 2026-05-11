package org.saavy.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.saavy.component.UiField;

// ... existing code ...
@Entity
@Table(name = "stock")
@Getter
@Setter
@NoArgsConstructor      // Required for JPA
@AllArgsConstructor
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @UiField(label = "ID", type = "number", hidden = true, editable = false, order = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @UiField(
            label = "Product",
            type = "manyToOne",
            editable = true,
            required = true,
            sortable = true,
            order = 2,
            optionLabel = "name",
            optionValue = "id",
            optionsEndpoint = "product"
    )
    @JsonBackReference
    private Product product;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "supplier_id", nullable = false)
    @UiField(
            label = "Supplier",
            type = "manyToOne",
            editable = true,
            required = true,
            sortable = true,
            order = 3,
            optionLabel = "name",
            optionValue = "id",
            optionsEndpoint = "supplier"
    )
    private Supplier supplier;

    @Column(nullable = false)
    @UiField(label = "Quantity", type = "number", order = 4)
    private int quantity;

    @JsonProperty("displayValue")
    public String getDisplayValue() { // Use the same getter method
        return product.getName() + " " + supplier.getName();
    }
}
