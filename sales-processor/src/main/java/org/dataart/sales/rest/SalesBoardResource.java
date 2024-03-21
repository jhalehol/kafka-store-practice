package org.dataart.sales.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.dataart.sales.model.HighSales;
import org.dataart.sales.service.SalesBoardService;

import java.util.Optional;

@Path("sales/leaderboard")
public class SalesBoardResource {

    @Inject
    SalesBoardService salesBoardService;

    @GET
    @Path("/{productId}")
    public Response getHighSalesByProductId(final String productId) {
        final Optional<HighSales> highSales = salesBoardService.getProductHighSales(productId);
        if (highSales.isPresent()) {
            return Response.ok(highSales.get()).build();
        }

        return Response.status(Response.Status.NOT_FOUND.getStatusCode()).build();
    }

    @GET
    public Response getHigSalesList() {
        return Response.ok(salesBoardService.getAllHighSales()).build();
    }
}
