package org.saavy.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SampleItemDTO implements Serializable {
    private Long id;
    private String name;
    private String description;
    private double price;
    private Boolean active = true;
}
