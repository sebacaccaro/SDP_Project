package gateway.ConcurrentStructures;

import java.util.HashMap;
import java.util.Map;

public class ConcurrentMap<KeyType, ValueType> extends AbstractLockableStructure {
    private final Map<KeyType, ValueType> baseMap;

    public ConcurrentMap() {
        baseMap = new HashMap<KeyType, ValueType>();
    }

    public ValueType read(KeyType k) throws InterruptedException {
        lockRead();
        ValueType readVakue = baseMap.get(k);
        unlockRead();
        return readVakue;
    }

    public void add(KeyType k, ValueType v) throws InterruptedException {
        lockWrite();
        baseMap.put(k, v);
        System.out.println("_ADD: " + k);
        unlockWrite();

    }

}
