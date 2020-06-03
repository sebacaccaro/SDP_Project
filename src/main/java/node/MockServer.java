package node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import gateway.store.beans.NodeBean;

public class MockServer {
    private static HashMap<Integer, NodeBean> nodes = new HashMap<Integer, NodeBean>();

    public List<NodeBean> register(NodeBean nb) {
        nodes.put(nb.getId(), nb);
        return new ArrayList<NodeBean>(nodes.values());
    }
}