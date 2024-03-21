package org.dataart.sales.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ProductHighSale {

    private String id;
    private Double price;
}
