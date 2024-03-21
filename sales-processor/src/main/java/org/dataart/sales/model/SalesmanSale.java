package org.dataart.sales.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SalesmanSale {

    private Salesman salesman;
    private SaleEvent sale;
}
