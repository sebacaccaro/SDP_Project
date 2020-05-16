package gateway.store.beans;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class NumberOfNodesBean {
    private int numberOfNodes;

    public NumberOfNodesBean() {
    }

    public int getNumberOfNodes() {
        return this.numberOfNodes;
    }

    public void setNumberOfNodes(int numberOfNodes) {
        this.numberOfNodes = numberOfNodes;
    }

}