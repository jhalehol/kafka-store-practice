package org.dataart.sales.consumer.rest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.dataart.sales.consumer.exception.NotFoundException;
import org.dataart.sales.consumer.service.ReportsService;

@ApplicationScoped
@Path("/reports")
public class HighSalesResources {

    @Inject
    ReportsService reportsService;

    @Path("/averages")
    @GET
    public Response getSalesAverages() {
        return Response.ok(reportsService.getAllSalesAverages()).build();
    }

    @Path("/averages/{id}")
    @GET
    public Response getSalesAverages(final Long id) throws NotFoundException {
        return Response.ok(reportsService.getSalesAverage(id)).build();
    }

    @Path("/high-sales")
    @GET
    public Response getHighSales() {
        return Response.ok(reportsService.getAllHighSales()).build();
    }

    @Path("/averages/store-metadata")
    @GET
    public Response getAveragesMetadata() {
        return Response.ok(reportsService.getMetadataForAverages()).build();
    }
}
