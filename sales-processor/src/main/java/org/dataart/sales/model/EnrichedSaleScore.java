package org.dataart.sales.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class EnrichedSaleScore implements Comparable<EnrichedSaleScore> {

    private Long salesmanId;
    private Long productId;
    private String salesmanName;
    private String productName;
    private BigDecimal price;

    @Override
    public int compareTo(final EnrichedSaleScore o) {
        return price.compareTo(o.getPrice());
    }
}
