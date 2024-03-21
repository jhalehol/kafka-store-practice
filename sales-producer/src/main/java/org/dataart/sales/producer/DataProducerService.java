package org.dataart.sales.producer;

import io.smallrye.reactive.messaging.kafka.Record;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.dataart.sales.model.*;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@Slf4j
@ApplicationScoped
public class DataProducerService {

    @Inject
    @Channel("products-out")
    private Emitter<Record<String, Product>> productsEmitter;

    @Inject
    @Channel("salesman-out")
    private Emitter<Record<String, Salesman>> salesmanEmitter;

    @Inject
    @Channel("sale-out")
    private Emitter<SaleEvent> saleEmitter;

    public void sendProduct(final Product product) {
        productsEmitter.send(Record.of(product.getId().toString(), product));
    }

    public void sendSalesman(final Salesman salesman) {
        salesmanEmitter.send(Record.of(salesman.getId().toString(), salesman));
    }

    public void sendSale(final SaleEvent sale) {
        saleEmitter.send(sale);
    }
}
