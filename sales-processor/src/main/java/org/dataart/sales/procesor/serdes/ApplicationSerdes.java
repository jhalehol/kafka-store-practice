package org.dataart.sales.procesor.serdes;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.dataart.sales.model.*;

public class ApplicationSerdes {

    public static final Serde<SaleEvent> SALES_SERDE = buildSerde(SaleEvent.class);
    public static final Serde<Salesman> SALESMAN_SERDE = buildSerde(Salesman.class);
    public static final Serde<Product> PRODUCT_SERDE = buildSerde(Product.class);
    public static final Serde<EnrichedSaleScore> ENRICHED_SALE_SERDE = buildSerde(EnrichedSaleScore.class);
    public static final Serde<HighSales> HIGH_SALES_SERDE = buildSerde(HighSales.class);
    public static final Serde<ProductHighSale> PRODUCT_HIGH_SALES_SERDE = buildSerde(ProductHighSale.class);
    public static final Serde<SalesmanAggregated> SALESMAN_AGGREGATED_SERDE = buildSerde(SalesmanAggregated.class);


    private static <T> Serde<T> buildSerde(Class clazz) {
        final CustomSerializer<T> ser = new CustomSerializer<>();
        final CustomDeserializer<T> des = new CustomDeserializer<>(clazz);
        return Serdes.serdeFrom(ser, des);
    }
}
