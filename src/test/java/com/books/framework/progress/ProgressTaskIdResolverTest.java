package com.books.framework.progress;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProgressTaskIdResolverTest {

    private final ProgressTaskIdResolver resolver = new ProgressTaskIdResolver();

    @Test
    void resolveTotalFromListArgument() {
        Integer total = resolver.resolveTotal(new Object[]{List.of("a", "b", "c")});
        assertEquals(3, total);
    }

    @Test
    void resolveTaskIdFromSpel() throws NoSuchMethodException {
        Method method = DemoService.class.getMethod("run", Long.class, List.class);
        String taskId = resolver.resolve("#fileId", method, new Object[]{42L, List.of()});
        assertEquals("42", taskId);
    }

    static class DemoService {
        public void run(Long fileId, List<String> rows) {
        }
    }
}
