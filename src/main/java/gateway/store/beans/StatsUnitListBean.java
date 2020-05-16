package gateway.store.beans;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class StatsUnitListBean {
    private List<StatUnitBean> stats;


    public StatsUnitListBean() {
    }


    public List<StatUnitBean> getStats() {
        return this.stats;
    }

    public void setStats(List<StatUnitBean> stats) {
        /* TODO: should use new to do the copy? */
        this.stats = stats;
    }

}