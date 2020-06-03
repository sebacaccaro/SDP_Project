package node;

import java.util.LinkedList;
import java.util.List;

import node.PMSensor.Buffer;
import node.PMSensor.Measurement;
import node.StatOuterClass.Stat;

public class SlidingWindowBuffer implements Buffer {
    private final static int SLIDING_WINDOW_SIZE = 12;
    private final static int ELEMENTS_TO_DISCARD = 6;

    private Stat lastStat = null;

    private LinkedList<Measurement> queue = new LinkedList<Measurement>();
    private List<Measurement> measurementBuffer = new LinkedList<Measurement>();

    @Override
    public void addMeasurement(Measurement m) {
        // The queue is needed to ensure mesurments are added in the windows buffer
        // in the same order they arrive
        queue.add(m);
        synchronized (this) {
            measurementBuffer.add(queue.pop());
            if (measurementBuffer.size() == SLIDING_WINDOW_SIZE) {
                Measurement mean = mean(measurementBuffer);
                measurementBuffer = measurementBuffer.subList(ELEMENTS_TO_DISCARD, SLIDING_WINDOW_SIZE);
                setLastStat(mean);
            }
        }

    }

    private synchronized void setLastStat(Measurement m) {
        lastStat = Stat.newBuilder().setTimestamp(m.getTimestamp()).setValue(m.getValue()).build();
    }

    public synchronized Stat getLastStat() {
        Stat ret = lastStat;
        lastStat = null;
        return ret;
    }

    private Measurement mean(List<Measurement> measurements) {
        double value = 0;
        long timestamp = 0;
        for (Measurement m : measurements) {
            value += m.getValue();
            timestamp += m.getTimestamp();
        }
        value = value / SLIDING_WINDOW_SIZE;
        timestamp = (long) (timestamp * 1.0 / SLIDING_WINDOW_SIZE);
        return new Measurement(measurements.get(0).getId(), measurements.get(0).getType(), value, timestamp);
    }

}