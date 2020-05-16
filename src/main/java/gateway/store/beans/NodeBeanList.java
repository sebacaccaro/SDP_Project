package gateway.store.beans;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class NodeBeanList {
    private List<NodeBean> nodes;


    public NodeBeanList() {
    }


    public List<NodeBean> getNodes() {
        return this.nodes;
    }

    public void setNodes(List<NodeBean> nodes) {
        /* TODO: should use new to do the copy? */
        this.nodes = nodes;
    }

}