package org.dataart.sales.consumer;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.dataart.sales.model.ProductHighSale;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@Slf4j
@ApplicationScoped
public class HighSalesConsumer {

    @Incoming("high-sales")
    public void consumeHighSales(final ProductHighSale highSales) {
        log.info("High Sale was received {}", highSales);
    }
}
