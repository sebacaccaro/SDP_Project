package gateway.resources;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import gateway.ConcurrentStructures.DuplicateKeyException;
import gateway.store.Store;
import gateway.store.beans.NodeBean;
import gateway.store.beans.StatUnitBean;

/**
 * Resource handler for single nodes talking to the gateway
 */
@Path("node")
public class NodeResource {
    @Inject
    private javax.inject.Provider<org.glassfish.grizzly.http.server.Request> request;

    /**
     * Method handling HTTP GET requests. The returned object will be sent to the
     * client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     * @throws InterruptedException
     */
    @POST
    @Path("/join")
    @Produces({ "application/json" })
    public Response join(NodeBean joiningNode) {
        joiningNode.setIp(request.get().getRemoteAddr().trim());
        Store s = new Store();
        try {
            s.addNode(joiningNode);
            return Response.ok(s.getNodes()).build();
        } catch (DuplicateKeyException e) {
            // 409 Conflict: The user can change and repeat the request
            return Response.status(409).entity(e.getMessage()).build();
        } catch (InterruptedException e) {
            // 500 Internal Server Error
            return Response.status(500).build();
        }
    }

    @DELETE
    @Path("/leave/{id}")
    @Produces({ "application/json" })
    public Response leave(@PathParam("id") int id) {
        Store s = new Store();
        try {
            s.removeNode(id);
        } catch (InterruptedException e) {
            // 500 Internal Server Error
            return Response.status(500).build();
        }
        return Response.ok().build();
    }

    @POST
    @Path("/send_stats")
    @Produces({ "application/json" })
    public Response sendStats(StatUnitBean receivedStats) {
        Store s = new Store();
        try {
            System.out.println("Gateway: " + receivedStats);
            s.addStat(receivedStats);
        } catch (InterruptedException e) {
            // 500 Internal Server Error
            return Response.status(500).build();
        }
        return Response.ok().build();
    }

}