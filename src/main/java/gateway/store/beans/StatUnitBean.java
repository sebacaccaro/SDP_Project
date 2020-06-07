package gateway.store.beans;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

import gateway.ConcurrentStructures.CloneInterface;

@XmlRootElement
public class StatUnitBean implements CloneInterface<StatUnitBean> {
    private long timestamp;
    private double value;

    public StatUnitBean() {
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getValue() {
        return this.value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public StatUnitBean clone() {
        StatUnitBean cloned = new StatUnitBean();
        cloned.setValue(this.value);
        cloned.setTimestamp(this.timestamp);
        return cloned;
    }

    public String toString() {
        return "Time: " + new Date(timestamp) + " Stat: " + value;
    }
}