package gateway.store.beans;

import javax.xml.bind.annotation.XmlRootElement;

import gateway.ConcurrentStructures.CloneInterface;

@XmlRootElement
public class StatUnitBean implements CloneInterface<StatUnitBean> {
    private int timestamp; // TODO: seconds or milliseconds?
    private double MOCKSTAT; // TODO: change with actual stat

    public StatUnitBean() {
    }

    public int getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public double getMOCKSTAT() {
        return this.MOCKSTAT;
    }

    public void setMOCKSTAT(double MOCKSTAT) {
        this.MOCKSTAT = MOCKSTAT;
    }

    public StatUnitBean clone() {
        StatUnitBean cloned = new StatUnitBean();
        cloned.setMOCKSTAT(this.MOCKSTAT);
        cloned.setTimestamp(this.timestamp);
        return cloned;
    }
}