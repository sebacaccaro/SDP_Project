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

    public List<T> getDeepCopy() throws InterruptedException {
        lockRead();
        List<T> cp = new ArrayList<>();
        for (T t : baseList) {
            cp.add(t.clone());
        }
        unlockRead();
        return cp;
    }
}