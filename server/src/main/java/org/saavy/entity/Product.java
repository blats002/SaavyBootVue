package org.saavy.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.saavy.component.UiDetail;
import org.saavy.component.UiField;
import org.saavy.component.UiMaster;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product")
@Getter
@Setter
@NoArgsConstructor      // Required for JPA
@AllArgsConstructor
@UiMaster(title = "Product",
        dialogHeader = "Product Detail",
        optionLabel = "displayValue",
        messages = "{\"created\":\"Product Created\"" +
                ",\"updated\":\"Product Updated\"" +
                ",\"deleted\":\"Product Deleted\"" +
                ",\"deletedMany\":\"Products Deleted\"" +
                "}",
        masterEndPoint = "product"
)
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @UiField(label = "ID", type = "number", hidden = true, editable = false, order = 1)
    private Long id;

    @Column(nullable = false)
    @UiField(label = "Name", type = "text", order = 2)
    private String name;

    @Column(nullable = false)
    @UiField(label = "Price", type = "number", order = 3)
    private double price;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true,  fetch = FetchType.LAZY)
    @UiDetail(
            key = "stocks",
            title = "Stocks",
            parentField = "product",
            parentValue = "{\"id\":\"id\", \"name\":\"name\"}",
            messages =  "{\"created\":\"Stock Created\"" +
                    ",\"updated\":\"Stock Updated\"" +
                    ",\"deleted\":\"Stock Deleted\"" +
                    ",\"deletedMany\":\"Stocks Deleted\"" +
                    "}",
            detailEndpoint  = "stock"
    )
    @JsonManagedReference
    private List<Stock> stocks = new ArrayList<>();




    // ... existing code ...
}
