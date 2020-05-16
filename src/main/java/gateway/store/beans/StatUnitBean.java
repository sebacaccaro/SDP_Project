package gateway.store.beans;

import javax.xml.bind.annotation.XmlRootElement;

import gateway.ConcurrentStructures.CloneInterface;

@XmlRootElement
public class StatUnitBean implements CloneInterface<StatUnitBean> {
    private int timestamp; // TODO: seconds or milliseconds?
    private double mockstat; // TODO: change with actual stat

    public StatUnitBean() {
    }

    public int getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public double getMockstat() {
        return this.mockstat;
    }

    public void setMockstat(double mockstat) {
        this.mockstat = mockstat;
    }

    public StatUnitBean clone() {
        StatUnitBean cloned = new StatUnitBean();
        cloned.setMockstat(this.mockstat);
        cloned.setTimestamp(this.timestamp);
        return cloned;
    }

    /* TODO: fix with real time */
    public String toString() {
        return "Time: " + timestamp + " Stat: " + mockstat;
    }
}