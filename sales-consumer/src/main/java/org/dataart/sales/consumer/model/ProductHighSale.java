package org.dataart.sales.consumer.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Builder
@Data
public class ProductHighSale {

    private String id;
    private String description;
    private BigDecimal price;
}
