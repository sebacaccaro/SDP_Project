package gateway.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import gateway.store.Store;

/**
 * Resource handler for analystis to get data
 */
@Path("analyst")
public class AnalystResource {

    @GET
    @Path("number_of_nodes")
    @Produces({ "application/json" })
    public Response numberOfNodes() {
        Store s = new Store();
        try {
            return Response.ok(s.getNodeCount()).build();
        } catch (InterruptedException e) {
            // 500 Internal Server Error
            return Response.status(500).build();
        }
    }

    @GET
    @Path("last_stats")
    @Produces({ "application/json" })
    public Response lastNStats(@QueryParam("n") int numberOfElements) {
        Store s = new Store();
        try {
            return Response.ok(s.getStats(numberOfElements)).build();
        } catch (InterruptedException e) {
            // 500 Internal Server Error
            return Response.status(500).build();
        }
    }

    @GET
    @Path("average_deviation")
    @Produces({ "application/json" })
    public Response getAvgAndDeviation(@QueryParam("n") int numberOfElements) {
        Store s = new Store();
        try {
            return Response.ok(s.getAverageAndStandardDeviation(numberOfElements)).build();
        } catch (InterruptedException e) {
            // 500 Internal Server Error
            return Response.status(500).build();
        }
    }
}