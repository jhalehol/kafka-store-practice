package org.dataart.sales.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SaleEvent {

    private Long salesmanId;
    private Long productId;
    private BigDecimal price;
}
