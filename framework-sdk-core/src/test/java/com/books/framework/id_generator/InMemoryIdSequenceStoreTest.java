package com.books.framework.id_generator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryIdSequenceStoreTest {

    @Test
    void shouldIncrementPerPrefixAndDate() {
        InMemoryIdSequenceStore store = new InMemoryIdSequenceStore();
        assertEquals(1, store.nextSeq("EV", "20260603"));
        assertEquals(2, store.nextSeq("EV", "20260603"));
        assertEquals(1, store.nextSeq("EV", "20260604"));
        assertEquals(1, store.nextSeq("P", "20260603"));
    }

    @Test
    void shouldShareStoreAcrossGenerators() {
        IdSequenceStore store = new InMemoryIdSequenceStore();
        IDGenerator ev = new IDGenerator("EV", store);
        IDGenerator ev2 = new IDGenerator("EV", store);
        String id1 = ev.genId();
        String id2 = ev2.genId();
        long n1 = Long.parseLong(id1.substring(id1.length() - 6));
        long n2 = Long.parseLong(id2.substring(id2.length() - 6));
        assertEquals(n1 + 1, n2);
    }
}
