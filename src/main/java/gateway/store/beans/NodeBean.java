package gateway.store.beans;

import javax.xml.bind.annotation.XmlRootElement;

import gateway.ConcurrentStructures.CloneInterface;

@XmlRootElement
public class NodeBean implements CloneInterface<NodeBean> {
    private String ip;
    private int id;
    private int port; 

    public NodeBean(){

    }

    public NodeBean clone() {
        NodeBean nb = new NodeBean();
        nb.setId(this.id);
        nb.setIp(new String(this.ip));
        nb.setPort(this.port);
        return nb;
    }


    public String getIp() {
        return this.ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

}