package gateway.ConcurrentStructures;

import java.util.ArrayList;
import java.util.List;

public class ConcurrentList<T extends CloneInterface<T>> extends AbstractLockableStructure {
    private final List<T> baseList; 

    public ConcurrentList(){
        this.baseList = new ArrayList<T>();
    }

    public void add(T newElement) throws InterruptedException {
        lockWrite();
        baseList.add(newElement);
        unlockWrite();
    }

    public List<T> getDeepCopy(int elementsToGet) throws InterruptedException {
        lockRead();
        List<T> cp = new ArrayList<>();
        int start = baseList.size()-elementsToGet;
        start = start < 0 ? 0: start;
        for(int i = start; i < baseList.size(); i++){
            cp.add(baseList.get(i).clone());
        }
        unlockRead();
        return cp;
    }
}