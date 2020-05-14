package gateway.ConcurrentStructures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class tester {
    public static void main(String[] args) throws InterruptedException {
        ConcurrentMap<String, String> lol = new ConcurrentMap<String, String>();
        List<Thread> t1 = new ArrayList<Thread>();
        lol.add("5", "5");
        lol.add("6", "6");
        lol.add("7", "7");
        lol.add("8", "8");
        System.out.println("-------------------");
        /*
         * t1.add(new Thread(() -> lol.add("1", "1"))); t1.add(new Thread(() ->
         * lol.add("2", "2"))); t1.add(new Thread(() -> lol.add("3", "3"))); t1.add(new
         * Thread(() -> lol.add("4", "4"))); t1.add(new Thread(() -> lol.read("5")));
         * t1.add(new Thread(() -> lol.read("6"))); t1.add(new Thread(() ->
         * lol.read("7"))); t1.add(new Thread(() -> lol.read("8")));
         */
        Collections.shuffle(t1);
        for (Thread t : t1) {
            t.start();
        }
        for (Thread t : t1) {
            t.join();
        }
        System.out.println("FINE");
    }
}