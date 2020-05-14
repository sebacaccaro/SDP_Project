package gateway.ConcurrentStructures;

public class ConcurrentInt {
    int i;

    public ConcurrentInt(int v) {
        i = v;
    }

    public ConcurrentInt() {
        this(0);
    }

    public synchronized int add(int a) {
        i += a;
        return i;
    }

    public synchronized int getValue() {
        return i;
    }
}