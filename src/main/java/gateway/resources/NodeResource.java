package gateway.resources;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import gateway.ConcurrentStructures.DuplicateKeyException;
import gateway.store.Store;
import gateway.store.beans.NodeBean;
import gateway.store.beans.NodeBeanList;

/**
 * Resource handler for single nodes talking to the gateway
 */
@Path("node")
public class NodeResource {

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
    public Response join(NodeBean joiningNode) throws InterruptedException {
        Store s = new Store();
        try {
            s.addNode(joiningNode);
        } catch (DuplicateKeyException e) {
            // 409 Conflict: The user can change and repeat the request
            return Response.status(409).entity(e.getMessage()).build();
        }
        return Response.ok(s.getNodes()).build();
    }
}