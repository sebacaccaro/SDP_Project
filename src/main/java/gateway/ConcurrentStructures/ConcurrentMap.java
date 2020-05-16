package gateway.ConcurrentStructures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConcurrentMap<KeyType, ValueType extends CloneInterface<ValueType>> extends AbstractLockableStructure {
    private final Map<KeyType, ValueType> baseMap;

    public ConcurrentMap() {
        baseMap = new HashMap<KeyType, ValueType>();
    }

    public ValueType read(KeyType k) throws InterruptedException {
        lockRead();
        ValueType readVakue = baseMap.get(k).clone();
        unlockRead();
        return readVakue;
    }

    public void add(KeyType k, ValueType v) throws InterruptedException,DuplicateKeyException {
        lockWrite();
        if (baseMap.containsKey(k)){
            unlockWrite();
            throw new DuplicateKeyException("You're trying to add a node with ID= "+k+", but a node with the same ID it's already registered");
        }
        baseMap.put(k, v);
        unlockWrite();
    }

    public void remove(KeyType k) throws InterruptedException {
        lockWrite();
        baseMap.remove(k);
        unlockWrite();
    }

    public List<ValueType> toList() throws InterruptedException {
        lockRead();
        List<ValueType> mapAsList = new ArrayList<ValueType>();
        for (ValueType v : baseMap.values()) {
            mapAsList.add(v.clone());
        }
        unlockRead();
        return mapAsList;
    }

    public int size() throws InterruptedException {
        lockRead();
        int count = baseMap.size();
        unlockRead();
        return count;
    }



}
