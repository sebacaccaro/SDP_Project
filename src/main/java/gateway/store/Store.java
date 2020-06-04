package gateway.store;

import gateway.store.beans.*;

import gateway.ConcurrentStructures.ConcurrentMap;
import gateway.ConcurrentStructures.DuplicateKeyException;

import java.util.List;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import gateway.ConcurrentStructures.ConcurrentList;

public class Store {
    private static final ConcurrentMap<Integer, NodeBean> nodes = new ConcurrentMap<Integer, NodeBean>();
    private static final ConcurrentList<StatUnitBean> stats = new ConcurrentList<StatUnitBean>();

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

    public NumberOfNodesBean getNodeCount() throws InterruptedException {
        NumberOfNodesBean n = new NumberOfNodesBean();
        n.setNumberOfNodes(nodes.size());
        return n;
    }

    public void addStat(StatUnitBean sb) throws InterruptedException {
        stats.add(sb);
    }

    public StatsUnitListBean getStats(int elementsToGet) throws InterruptedException {
        StatsUnitListBean statsList = new StatsUnitListBean();
        statsList.setStats(stats.getDeepCopy(elementsToGet));
        return statsList;
    }

    public AvgSdStatsBean getAverageAndStandardDeviation(int elementsToGet) throws InterruptedException {
        StatsUnitListBean statsList = new StatsUnitListBean();
        statsList.setStats(stats.getDeepCopy(elementsToGet));
        AvgSdStatsBean assb = new AvgSdStatsBean();
        List<StatUnitBean> stats = statsList.getStats();
        if (stats.size() == 0) {
            assb.setAverage(0);
            assb.setStandard_deviation(0);
            return assb;
        }

        double[] values = new double[stats.size()];
        for (int i = 0; i < stats.size(); i++) {
            values[i] = stats.get(i).getValue();
        }

        StandardDeviation sd = new StandardDeviation();
        Mean m = new Mean();
        assb.setAverage(m.evaluate(values));
        assb.setStandard_deviation(sd.evaluate(values));
        return assb;
    }
}