package org.dataart.sales.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.dataart.sales.model.Product;
import org.dataart.sales.model.SaleEvent;
import org.dataart.sales.model.Salesman;
import org.dataart.sales.producer.DataProducerService;

@Path("/sales")
public class SalesResources {

    @Inject
    DataProducerService producerService;

    @POST
    @Path("/products")
    public Response addProduct(final Product product) {
        producerService.sendProduct(product);
        return Response.accepted().build();
    }

    @POST
    @Path("/salesman")
    public Response addSalesman(final Salesman salesman) {
        producerService.sendSalesman(salesman);
        return Response.accepted().build();
    }

    @POST
    @Path("/sale")
    public Response addSalesman(final SaleEvent sale) {
        producerService.sendSale(sale);
        return Response.accepted().build();
    }

}
