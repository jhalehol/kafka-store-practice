package org.dataart.sales.procesor;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.dataart.sales.model.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.dataart.sales.procesor.serdes.ApplicationSerdes.*;

@Slf4j
@ApplicationScoped
public class TopologyBuilder {

    public static final String SALES_BOARD_LEADER = "sales-board-leader";

    @Produces
    public Topology buildTopology() {
        final StreamsBuilder builder = new StreamsBuilder();

        // Part 1

        // TODO: MAYBE USE BRANCHES?
        final KStream<String, SaleEvent> salesKStream = builder.stream("sales",
                Consumed.with(Serdes.String(), SALES_SERDE))
                .peek((k, v) -> log.info("Handling sale record id: {} value: {}", k, v))
                // Re-key for co-partitioning
                .selectKey((k, v) -> v.getSalesmanId().toString());

        // Part 1
        final KTable<String, Salesman> salesmanKTable = builder
                .stream("salesmen", Consumed.with(Serdes.String(), SALESMAN_SERDE))
                .peek((k, v) -> log.info("Handling salesman record id: {} value: {}", k, v))
                .toTable(Named.as("salesman-ktable"));

        // Part 1
        final GlobalKTable<String, Product> productGlobalKTable = builder
                .globalTable("products", Consumed.with(Serdes.String(), PRODUCT_SERDE));


        buildEnrichedSalesStream(salesKStream, salesmanKTable, productGlobalKTable);

        return builder.build();
    }


    private void buildEnrichedSalesStream(final KStream<String, SaleEvent> salesStream,
                                          final KTable<String, Salesman> salesmanTable,
                                          final GlobalKTable<String, Product> productsGlobalTable) {
        // Part 2 Create joiners
        final Joined<String, SaleEvent, Salesman> joinedSaleVendor = Joined.with(Serdes.String(), SALES_SERDE, SALESMAN_SERDE);
        final ValueJoiner<SaleEvent, Salesman, SalesmanSale> joinerSaleVendor = (sale, salesman) -> SalesmanSale.builder()
                .sale(sale)
                .salesman(salesman)
                .build();

        // Part 2 Join sale with salesman
        final KStream<String, SalesmanSale> salesmanSalesKStream = salesStream
                .peek((k, v) -> log.info("Salesman sale result after join id: {} value: {}", k, v))
                .join(salesmanTable, joinerSaleVendor, joinedSaleVendor);

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
                .join(productsGlobalTable, keyValueMapper, enrichedValueJoiner, Named.as("enriched-sale-kstream"));

        final KGroupedStream<String, EnrichedSaleScore> enrichedProductGroupedStream = enrichedKStream
                .peek((k, v) -> log.info("Enriched stream salesman id: {}, value: {}", k, v))
                .groupBy((k, v) -> v.getProductId().toString(), Grouped.with(Serdes.String(), ENRICHED_SALE_SERDE));

        final KGroupedStream<String, EnrichedSaleScore> enrichedSalesmanGroupedStream = enrichedKStream
                .peek((k, v) -> log.info("Enriched stream salesman id: {}, value: {}", k, v))
                .groupBy((k, v) -> v.getSalesmanId().toString(), Grouped.with(Serdes.String(), ENRICHED_SALE_SERDE));

        buildAggregations(enrichedSalesmanGroupedStream, enrichedProductGroupedStream);
    }

    private void buildAggregations(final KGroupedStream<String, EnrichedSaleScore> salesmanGroupedTable,
                                   final KGroupedStream<String, EnrichedSaleScore> productGroupedStream) {

        salesmanGroupedTable.aggregate(
                () -> SalesmanAggregated.builder()
                        .salesCount(0)
                        .name("")
                        .id(0L)
                        .totalAmountSales(BigDecimal.ZERO)
                        .avgAmountSales(BigDecimal.ZERO).build(),
                (key, value, aggregator) -> SalesmanAggregated.builder()
                        .id(value.getSalesmanId())
                        .name(value.getSalesmanName())
                        .salesCount(aggregator.getSalesCount() + 1)
                        .totalAmountSales(aggregator.getTotalAmountSales().add(value.getPrice()))
                        .avgAmountSales((aggregator.getTotalAmountSales()
                                .add(value.getPrice())
                                .divide(BigDecimal.valueOf(aggregator.getSalesCount() + 1D), RoundingMode.CEILING)))
                        .build(),
                Materialized.with(Serdes.String(), SALESMAN_AGGREGATED_SERDE))
                .toStream()
                .to("salesman-sales-average", Produced.with(Serdes.String(), SALESMAN_AGGREGATED_SERDE));

        final Initializer<HighSales> highSalesInitializer = HighSales::new;
        final Aggregator<String, EnrichedSaleScore, HighSales> highSalesAggregator = (
                key, enriched, highSales) -> highSales.addEnrichedSale(enriched);

        final KTable<String, HighSales> highSalesKTable = productGroupedStream
                .aggregate(highSalesInitializer, highSalesAggregator,
                        Materialized.<String, HighSales, KeyValueStore<Bytes, byte[]>>as(SALES_BOARD_LEADER)
                                .withKeySerde(Serdes.String()).withValueSerde(HIGH_SALES_SERDE));

        highSalesKTable.toStream()
                .map((k, v) -> {
                    final EnrichedSaleScore enriched = v.getFirstHighSale();
                    return KeyValue.pair(k, ProductHighSale.builder()
                            .id(enriched.getProductId().toString())
                            .description(enriched.getProductName())
                            .price(enriched.getPrice())
                            .build());
                })
                .peek((k, v) -> log.info("Producing data to topic highsales: {}", v))
                .to("high-sales-product", Produced.with(Serdes.String(), PRODUCT_HIGH_SALES_SERDE));
    }
}
