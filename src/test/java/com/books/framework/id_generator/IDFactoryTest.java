package com.books.framework.id_generator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IDFactoryTest {

    @Test
    void shouldReturnSameInstanceForSamePrefix() {
        IDFactory factory = new IDFactory();
        IDGenerator g1 = factory.getIDGenerator("CMB");
        IDGenerator g2 = factory.getIDGenerator("CMB");
        assertSame(g1, g2);
    }

    @Test
    void shouldReturnDifferentInstanceForDifferentPrefix() {
        IDFactory factory = new IDFactory();
        IDGenerator g1 = factory.getIDGenerator("CMB");
        IDGenerator g2 = factory.getIDGenerator("CMB_CREDIT");
        assertNotSame(g1, g2);
    }

    @Test
    void differentPrefixShouldProduceDifferentIdPrefixes() {
        IDFactory factory = new IDFactory();
        IDGenerator g1 = factory.getIDGenerator("CMB");
        IDGenerator g2 = factory.getIDGenerator("CMB_CREDIT");
        String id1 = g1.genId();
        String id2 = g2.genId();
        assertTrue(id1.startsWith("CMB"), id1);
        assertTrue(id2.startsWith("CMB_CREDIT"), id2);
    }
}
