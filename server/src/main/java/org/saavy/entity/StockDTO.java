package org.saavy.entity;

import lombok.Data;

@Data
public class StockDTO {
    private Long id;
    private ProductDTO product;
    private SupplierDTO supplier;
    private int quantity;
}
