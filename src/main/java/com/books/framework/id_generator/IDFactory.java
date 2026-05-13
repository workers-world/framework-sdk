package com.books.framework.id_generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * @author zero
 * @description todo
 * @date 2026-05-11
 */
public class IDFactory {
    private IDGenerator idGenerator;

    public IDGenerator getIDGenerator(String prefix) {
        if (idGenerator == null) {
            synchronized (IDFactory.class) {
                if (idGenerator == null) {
                    return new IDGenerator(prefix);
                }
            }
        }

        return idGenerator;
    }

    public static void main(String[] args) throws InterruptedException {
        IDFactory factory = new IDFactory();
        IDGenerator generator = factory.getIDGenerator("CMB");
//        for (int i = 0; i < 100; i++) {
//            System.out.println(generator.genId());
//        }

        Map<String, List<String>> map = new ConcurrentHashMap<>();

        CountDownLatch countDownLatch = new CountDownLatch(3);
        Runnable task = () -> {
            for (int i = 0; i < 100; i++) {
                if (map.containsKey(Thread.currentThread().getName())) {
                    List<String> list = map.get(Thread.currentThread().getName());
                    list.add(generator.genId());
                } else {
                    map.put(Thread.currentThread().getName(), new ArrayList<>());
                }
            }
            countDownLatch.countDown();

        };


        for (int i = 0; i < 3; i++) {
            new Thread(task, "thread" + i).start();
        }

        countDownLatch.await();

        System.out.println(map);
        System.out.println(map.get("thread0").size());
        System.out.println(map.get("thread1").size());
        System.out.println(map.get("thread2").size());


    }
}
