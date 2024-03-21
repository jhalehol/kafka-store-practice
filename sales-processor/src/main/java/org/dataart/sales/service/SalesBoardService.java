package org.dataart.sales.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.dataart.sales.model.HighSales;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.dataart.sales.procesor.TopologyBuilder.SALES_BOARD_LEADER;

@ApplicationScoped
public class SalesBoardService {

    @Inject
    KafkaStreams streams;

    private ReadOnlyKeyValueStore<String, HighSales> highSalesStore;

    public Optional<HighSales> getProductHighSales(final String productId) {
        initializeStore();

        return Optional.ofNullable(highSalesStore.get(productId));
    }

    public List<HighSales> getAllHighSales() {
        initializeStore();

        final List<HighSales> highSalesList = new ArrayList<>();
        try (var res = highSalesStore.all()) {
            res.forEachRemaining(scores -> highSalesList.add(scores.value));

        }

        return highSalesList;
    }

    private void initializeStore() {
        if (highSalesStore != null) {
            return;
        }

        highSalesStore = streams.store(StoreQueryParameters
                .fromNameAndType(SALES_BOARD_LEADER, QueryableStoreTypes.keyValueStore()));
    }

}
