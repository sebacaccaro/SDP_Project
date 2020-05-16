package gateway.store.beans;

import javax.xml.bind.annotation.XmlRootElement;

import gateway.ConcurrentStructures.CloneInterface;

@XmlRootElement
public class StatUnitBean implements CloneInterface<StatUnitBean>{
    private int timestamp; //TODO: seconds or milliseconds?
    private int MOCKSTAT; //TODO: change with actual stat


    public StatUnitBean() {
    }

    public int getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public int getMOCKSTAT() {
        return this.MOCKSTAT;
    }

    public void setMOCKSTAT(int MOCKSTAT) {
        this.MOCKSTAT = MOCKSTAT;
    }


    public StatUnitBean clone(){
        StatUnitBean cloned = new StatUnitBean();
        cloned.setMOCKSTAT(this.MOCKSTAT);
        cloned.setTimestamp(this.timestamp);
        return cloned;
    }
}