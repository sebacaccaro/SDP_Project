package gateway.store.beans;

public class AvgSdStatsBean {
    private double average;
    private double standard_deviation;

    public AvgSdStatsBean() {
    }

    public double getAverage() {
        return this.average;
    }

    public void setAverage(double average) {
        this.average = average;
    }

    public double getStandard_deviation() {
        return this.standard_deviation;
    }

    public void setStandard_deviation(double standard_deviation) {
        this.standard_deviation = standard_deviation;
    }

}