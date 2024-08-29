package org.dataart.sales.consumer.serdes;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.dataart.sales.consumer.model.ProductHighSale;
import org.dataart.sales.consumer.model.SalesmanSalesSummary;

public class ApplicationSerdes {

    public static final Serde<ProductHighSale> PRODUCT_HIGH_SALE_SERDE = buildSerde(ProductHighSale.class);
    public static final Serde<SalesmanSalesSummary> SALESMAN_SALES_SUMMARY_SERDE = buildSerde(SalesmanSalesSummary.class);

    private static <T> Serde<T> buildSerde(Class clazz) {
        final CustomSerializer<T> ser = new CustomSerializer<T>() {
        };
        final CustomDeserializer<T> des = new CustomDeserializer<T>(clazz) {
        };
        return Serdes.serdeFrom(ser, des);
    }
}
