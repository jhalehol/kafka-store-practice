package org.dataart.sales.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SalesmanAggregated {

    private Long id;
    private String name;
    private Integer salesCount;
    private BigDecimal totalAmountSales;
    private BigDecimal avgAmountSales;
}
