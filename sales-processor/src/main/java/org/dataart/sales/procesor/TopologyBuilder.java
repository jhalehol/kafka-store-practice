package org.dataart.sales.procesor;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.dataart.sales.model.*;
import org.dataart.sales.procesor.serdes.CustomDeserializer;
import org.dataart.sales.procesor.serdes.CustomSerializer;

@Slf4j
@ApplicationScoped
public class TopologyBuilder {

    public static final String SALES_BOARD_LEADER = "sales-board-leader";

    @Produces
    public Topology buildTopology() {
        final StreamsBuilder builder = new StreamsBuilder();

        // Part 1
        final Serde<SaleEvent> salesSerde = buildSerde(SaleEvent.class);
        final KStream<String, SaleEvent> salesKStream = builder.stream("sales",
                Consumed.with(Serdes.String(), salesSerde))
                .peek((k, v) -> log.info("Handling sale record id: {} value: {}", k, v))
                // Re-key for co-partitioning
                .selectKey((k, v) -> v.getSalesmanId().toString());

        // Part 1
        final Serde<Salesman> salesmanSerde = buildSerde(Salesman.class);
        final KTable<String, Salesman> salesmanKTable = builder
                .stream("salesmen", Consumed.with(Serdes.String(), salesmanSerde))
                .peek((k, v) -> log.info("Handling salesman record id: {} value: {}", k, v))
                .toTable(Named.as("salesman-ktable"));

        // Part 1
        final Serde<Product> productSerde = buildSerde(Product.class);
        final GlobalKTable<String, Product> productGlobalKTable = builder
                .globalTable("products", Consumed.with(Serdes.String(), productSerde));

        // Part 2 Create joiners
        final Joined<String, SaleEvent, Salesman> joinedSaleVendor = Joined.with(Serdes.String(), salesSerde, salesmanSerde);
        final ValueJoiner<SaleEvent, Salesman, SalesmanSale> joinerSaleVendor = (sale, salesman) -> SalesmanSale.builder()
                .sale(sale)
                .salesman(salesman)
                .build();

        // Part 2 Join sale with salesman
        final KStream<String, SalesmanSale> salesmanSalesKStream = salesKStream
                .peek((k, v) -> log.info("Salesman sale result after join id: {} value: {}", k, v))
                .join(salesmanKTable, joinerSaleVendor, joinedSaleVendor);

        // Part 2 Join Salesman sale with product to enrich
        final KeyValueMapper<String, SalesmanSale, String> keyValueMapper = (key, saleInfo) -> saleInfo.getSale().getProductId().toString();
        final ValueJoiner<SalesmanSale, Product, EnrichedSaleScore> enrichedValueJoiner = (saleData, product) ->
                EnrichedSaleScore.builder()
                        .salesmanId(saleData.getSalesman().getId())
                        .salesmanName(saleData.getSalesman().getName())
                        .productId(product.getId())
                        .productName(product.getName())
                        .price(saleData.getSale().getPrice())
                        .build();

        final KStream<String, EnrichedSaleScore> enrichedKStream = salesmanSalesKStream
                .peek((k, v) -> log.info("Enriched result after join id: {} value: {}", k, v))
                .join(productGlobalKTable, keyValueMapper, enrichedValueJoiner, Named.as("enriched-sale-kstream"));

        final Serde<EnrichedSaleScore> enrichedSerde = buildSerde(EnrichedSaleScore.class);
        final KGroupedStream<String, EnrichedSaleScore> enrichedGroupedStream = enrichedKStream
                .peek((k, v) -> log.info("Enriched stream id: {}, value: {}", k, v))
                .groupBy((k, v) -> v.getProductId().toString(), Grouped.with(Serdes.String(), enrichedSerde));

        // Optional perform Group KTable salesman allowing to perform aggregations
        final KGroupedTable<String, Salesman> salesmanGroupedTable = salesmanKTable
                .groupBy(KeyValue::new, Grouped.with(Serdes.String(), salesmanSerde));

        salesmanGroupedTable.aggregate(
                () -> 0L,
                (key, value, aggregator) -> aggregator + 1L,
                (key, value, aggregator) -> aggregator - 1L,
                Materialized.with(Serdes.String(), Serdes.Long()));

        final Initializer<HighSales> highSalesInitializer = HighSales::new;
        final Aggregator<String, EnrichedSaleScore, HighSales> highSalesAggregator = (
                key, enriched, highSales) -> highSales.addEnrichedSale(enriched);

        final Serde<HighSales> highSalesSerde = buildSerde(HighSales.class);
        final KTable<String, HighSales> highSalesKTable = enrichedGroupedStream
                .aggregate(highSalesInitializer, highSalesAggregator,
                        Materialized.<String, HighSales, KeyValueStore<Bytes, byte[]>>as(SALES_BOARD_LEADER)
                                .withKeySerde(Serdes.String()).withValueSerde(highSalesSerde));

        final Serde<ProductHighSale> productHighSaleSerde = buildSerde(ProductHighSale.class);
        highSalesKTable.toStream()
                .map((k, v) -> {
                    final EnrichedSaleScore enriched = v.getFirstHighSale();
                    return KeyValue.pair(k, ProductHighSale.builder()
                            .id(enriched.getProductId().toString())
                            .price(enriched.getPrice())
                            .build());
                })
                .peek((k, v) -> log.info("Producing data to topic highsales: {}", v))
                .to("high-sales", Produced.with(Serdes.String(), productHighSaleSerde));

        return builder.build();
    }

    public <T> Serde<T> buildSerde(Class clazz) {
        final CustomSerializer<T> ser = new CustomSerializer<>();
        final CustomDeserializer<T> des = new CustomDeserializer<>(clazz);
        return Serdes.serdeFrom(ser, des);
    }

}
