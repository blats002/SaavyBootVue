package org.saavy.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProductDTO {
    private Long id;
    private String name;
    private double price;
    private List<StockDTO> stocks = new ArrayList<>();
}
