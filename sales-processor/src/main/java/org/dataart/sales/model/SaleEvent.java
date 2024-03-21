package org.dataart.sales.model;

import lombok.Data;

@Data
public class SaleEvent {

    private Long salesmanId;
    private Long productId;
    private Double price;
}
