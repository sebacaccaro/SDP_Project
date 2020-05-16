package gateway.store;

import gateway.store.beans.*;

import gateway.ConcurrentStructures.ConcurrentMap;
import gateway.ConcurrentStructures.DuplicateKeyException;
import gateway.ConcurrentStructures.ConcurrentList;

public class Store {
    private static final ConcurrentMap<Integer, NodeBean> nodes = new ConcurrentMap<Integer, NodeBean>();
    private static final ConcurrentList<StatUnitBean> stats = new ConcurrentList<StatUnitBean>(); // TODO: switch to
                                                                                                  // conccurent list

    public Store() {
    };

    public void addNode(NodeBean nd) throws InterruptedException, DuplicateKeyException {
        nodes.add(nd.getId(), nd);
    }

    public void removeNode(Integer nodeId) throws InterruptedException {
        nodes.remove(nodeId);
    }

    public NodeBeanList getNodes() throws InterruptedException {
        NodeBeanList nodesList = new NodeBeanList();
        nodesList.setNodes(nodes.toList());
        return nodesList;
    }

    public void addStat(StatUnitBean sb) throws InterruptedException {
        stats.add(sb);
    }

    public StatsUnitListBean getStats(StatUnitBean sb) throws InterruptedException {
        StatsUnitListBean statsList = new StatsUnitListBean();
        statsList.setStats(stats.getDeepCopy());
        return statsList;
    }
}