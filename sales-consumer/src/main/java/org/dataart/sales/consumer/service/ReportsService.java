package org.dataart.sales.consumer.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.dataart.sales.consumer.exception.NotFoundException;
import org.dataart.sales.consumer.model.HostStoreInfo;
import org.dataart.sales.consumer.model.ProductHighSale;
import org.dataart.sales.consumer.model.SalesmanSalesSummary;
import org.dataart.sales.consumer.repository.HighSalesRepository;
import org.dataart.sales.consumer.repository.SalesAveragesRepository;
import org.dataart.sales.consumer.topology.PipelineMetadata;

import java.util.Comparator;
import java.util.List;

@ApplicationScoped
public class ReportsService {

    @Inject
    HighSalesRepository highSalesRepository;

    @Inject
    SalesAveragesRepository salesAveragesRepository;

    public List<ProductHighSale> getAllHighSales() {
        return highSalesRepository.getCurrentHighSales()
                .stream()
                .sorted(Comparator.comparing(ProductHighSale::getPrice).reversed())
                .toList();
    }

    public List<SalesmanSalesSummary> getAllSalesAverages() {
        return salesAveragesRepository.getAllSalesAverages();
    }

    public SalesmanSalesSummary getSalesAverage(final Long id) throws NotFoundException {
        return salesAveragesRepository.getSalesmanSummary(id);
    }

    public List<HostStoreInfo> getMetadataForAverages() {
        return salesAveragesRepository.getMetaData();
    }
}
