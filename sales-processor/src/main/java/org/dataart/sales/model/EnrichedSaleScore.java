package org.dataart.sales.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EnrichedSaleScore implements Comparable<EnrichedSaleScore> {

    private Long salesmanId;
    private Long productId;
    private String salesmanName;
    private String productName;
    private Double price;

    @Override
    public int compareTo(final EnrichedSaleScore o) {
        return Double.compare(o.getPrice(), price);
    }
}
