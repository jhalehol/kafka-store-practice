package org.dataart.sales.consumer.repository;

import com.fasterxml.jackson.core.util.JacksonFeature;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.dataart.sales.consumer.exception.NotFoundException;
import org.dataart.sales.consumer.model.HostStoreInfo;
import org.dataart.sales.consumer.model.KeyValueSaleSummary;
import org.dataart.sales.consumer.model.SalesmanSalesSummary;
import org.dataart.sales.consumer.service.MetadataService;

import java.util.List;

import static org.dataart.sales.consumer.topology.TopologyBuilder.SALESMAN_AVERAGES_STORE_NAME;

@Slf4j
@ApplicationScoped
public class SalesAveragesRepository {

    private static final String CURRENT_HOST = "localhost:9900";
    private static final int CURRENT_PORT = 9900;
    private static final String EXTERNAL_QUERY_PATH = "/reports/averages/%s";

    @Inject
    KafkaStreams kafkaStreams;

    @Inject
    MetadataService metadataService;

    private final Client client = ClientBuilder.newBuilder().register(JacksonFeature.class).build();

    ReadOnlyKeyValueStore<String, SalesmanSalesSummary> store;

    public List<SalesmanSalesSummary> getAllSalesAverages() {
        validateStore();

        return Multi.createFrom().iterable(store::all)
                .subscribe()
                .asStream()
                .map(item -> item.value)
                .toList();
    }

    public SalesmanSalesSummary getSalesmanSummary(final Long id) throws NotFoundException {
        var storeInfo = metadataService.streamsMetadataForStoreAndKey(SALESMAN_AVERAGES_STORE_NAME, id, new LongSerializer());
        if (thisHost(storeInfo)) {
            return store.get(id.toString());
        }

        final KeyValueSaleSummary valueBean = fetchByKey(storeInfo, String.format(EXTERNAL_QUERY_PATH, id));
        return valueBean.getSummary();
    }

    private KeyValueSaleSummary fetchByKey(final HostStoreInfo host, final String path) {
        return client.target(String.format("http://%s:%d/%s", host.getHost(), host.getPort(), path))
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(new GenericType<>() {});
    }

    private boolean thisHost(final HostStoreInfo host) {
        return host.getHost().equals(CURRENT_HOST) &&
                host.getPort() == CURRENT_PORT;
    }



    public List<HostStoreInfo> getMetaData() {
        return metadataService.streamsMetadataForStore(SALESMAN_AVERAGES_STORE_NAME);
    }

    private void validateStore() {
        if (store != null) {
            return;
        }

        initializeStore();
    }

    private void initializeStore() {
        try {
            store = kafkaStreams.store(StoreQueryParameters.fromNameAndType(SALESMAN_AVERAGES_STORE_NAME, QueryableStoreTypes.keyValueStore()));
        } catch (Exception e) {
            log.error("Unable to initialize the store", e);
        }
    }
}
