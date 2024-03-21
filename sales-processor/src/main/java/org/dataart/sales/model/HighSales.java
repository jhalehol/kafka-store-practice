package org.dataart.sales.model;

import lombok.Data;

import java.util.TreeSet;

@Data
public class HighSales {

    private final TreeSet<EnrichedSaleScore> highSales = new TreeSet<>();

    public HighSales addEnrichedSale(final EnrichedSaleScore sale) {
        highSales.add(sale);

        if (highSales.size() > 3) {
            highSales.remove(highSales.last());
        }

        return this;
    }

    public EnrichedSaleScore getFirstHighSale() {
        return highSales.first();
    }
}
