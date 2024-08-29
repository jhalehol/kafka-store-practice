package org.dataart.sales.consumer.topology;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.Stores;
import org.dataart.sales.consumer.model.ProductHighSale;
import org.dataart.sales.consumer.model.SalesmanSalesSummary;

import static org.dataart.sales.consumer.serdes.ApplicationSerdes.PRODUCT_HIGH_SALE_SERDE;
import static org.dataart.sales.consumer.serdes.ApplicationSerdes.SALESMAN_SALES_SUMMARY_SERDE;

@ApplicationScoped
public class TopologyBuilder {

    public static final String HIGH_SALES_STORE_NAME = "high-sales-store";
    public static final String SALESMAN_AVERAGES_STORE_NAME = "averages-sales-store";

    @Produces
    public Topology buildTopology() {
        final StreamsBuilder builder = new StreamsBuilder();

        final KeyValueBytesStoreSupplier storeSalesSupplier = Stores.persistentKeyValueStore(HIGH_SALES_STORE_NAME);
        var highSalesTable = builder.table("high-sales-product",
                Materialized.<String, ProductHighSale>as(storeSalesSupplier)
                        .withKeySerde(Serdes.String())
                        .withValueSerde(PRODUCT_HIGH_SALE_SERDE));

        final KeyValueBytesStoreSupplier storeSalesmanSupplier = Stores.inMemoryKeyValueStore(SALESMAN_AVERAGES_STORE_NAME);
        var avgSalesTable = builder.table("salesman-sales-average",
                Materialized.<String, SalesmanSalesSummary>as(storeSalesmanSupplier)
                        .withKeySerde(Serdes.String())
                        .withValueSerde(SALESMAN_SALES_SUMMARY_SERDE));


        return builder.build();
    }
}
