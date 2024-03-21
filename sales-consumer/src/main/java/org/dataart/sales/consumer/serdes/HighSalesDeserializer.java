package org.dataart.sales.consumer.serdes;

import org.dataart.sales.model.ProductHighSale;

public class HighSalesDeserializer extends CustomDeserializer<ProductHighSale> {
    public HighSalesDeserializer() {
        super(ProductHighSale.class);
    }
}
