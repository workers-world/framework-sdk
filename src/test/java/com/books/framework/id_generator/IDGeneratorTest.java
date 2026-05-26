package com.books.framework.id_generator;

import com.books.framework.utils.DateUtils;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class IDGeneratorTest {

    @Test
    void shouldIncludePrefixAndDate() {
        IDGenerator g = new IDGenerator("CMB");
        String id = g.genId();
        assertTrue(id.startsWith("CMB" + DateUtils.getCurDate()),
                () -> "ID 应以 prefix + 8位日期开头: " + id);
    }

    @Test
    void shouldIncrementSequentially() {
        IDGenerator g = new IDGenerator("TEST");
        String id1 = g.genId();
        String id2 = g.genId();
        long n1 = Long.parseLong(id1.substring(id1.length() - 6));
        long n2 = Long.parseLong(id2.substring(id2.length() - 6));
        assertEquals(n1 + 1, n2);
    }

    @Test
    void shouldStartFromOne() {
        IDGenerator g = new IDGenerator("T");
        String id = g.genId();
        assertTrue(id.endsWith("000001"), () -> "首条 ID 序号应为 000001: " + id);
    }

    @Test
    void shouldBeThreadSafe() throws InterruptedException {
        IDGenerator g = new IDGenerator("MT");
        int threads = 8;
        int perThread = 1000;
        CountDownLatch latch = new CountDownLatch(threads);
        Set<String> set = ConcurrentHashMap.newKeySet(threads * perThread);
        AtomicReference<Throwable> error = new AtomicReference<>();

        for (int i = 0; i < threads; i++) {
            new Thread(() -> {
                try {
                    for (int j = 0; j < perThread; j++) {
                        String id = g.genId();
                        if (!set.add(id)) {
                            error.set(new AssertionError("重复 ID: " + id));
                            return;
                        }
                    }
                } catch (Exception e) {
                    error.set(e);
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await();
        assertNull(error.get(), () -> error.get() != null ? error.get().getMessage() : "");
        assertEquals(threads * perThread, set.size());
    }

    @Test
    void shouldThrowWhenOverflow() {
        // 使用反射直接设置 seq 到临界值来模拟溢出
        IDGenerator g = new IDGenerator("OV");
        for (long i = 1; i <= 999_999; i++) {
            g.genId();
        }
        assertThrows(IllegalStateException.class, g::genId);
    }
}
