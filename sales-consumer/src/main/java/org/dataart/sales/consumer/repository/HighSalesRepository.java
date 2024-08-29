package org.dataart.sales.consumer.repository;

import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.dataart.sales.consumer.model.ProductHighSale;

import java.util.List;

import static org.dataart.sales.consumer.topology.TopologyBuilder.HIGH_SALES_STORE_NAME;

@Slf4j
@ApplicationScoped
public class HighSalesRepository {


    @Inject
    KafkaStreams kafkaStreams;

    ReadOnlyKeyValueStore<String, ProductHighSale> store;

    public List<ProductHighSale> getCurrentHighSales() {
        validateStore();

        return Multi.createFrom().iterable(store::all)
                .subscribe()
                .asStream()
                .map(item -> item.value)
                .toList();
    }

    private void validateStore() {
        if (store != null) {
            return;
        }

        initializeStore();

    }

    private void initializeStore() {
        try {
            store = kafkaStreams.store(StoreQueryParameters.fromNameAndType(HIGH_SALES_STORE_NAME, QueryableStoreTypes.keyValueStore()));
        } catch (Exception e) {
            log.error("Unable to initialize the store", e);
        }
    }
}
