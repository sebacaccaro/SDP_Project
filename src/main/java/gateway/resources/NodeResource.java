package gateway.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
    @GET
    @Produces({"application/json"})
    public NodeBeanList getOne() throws InterruptedException {
        Store s = new Store();
        return s.getNodes();
    }
}