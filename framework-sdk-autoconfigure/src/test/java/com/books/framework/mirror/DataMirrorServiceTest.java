package com.books.framework.mirror;

import com.books.framework.manager.DataMirrorManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataMirrorServiceTest {

    @Mock
    private DataMirrorManager manager;

    @InjectMocks
    private DataMirrorService service;

    @Test
    void recordUpdatePersistsBeforeAndAfter() {
        when(manager.insertEntry(
                eq("sample_orders"),
                eq(9L),
                eq("T1"),
                eq("UPDATE"),
                eq("{\"before\":1}"),
                eq("{\"after\":2}"),
                eq("[\"amount\"]"),
                eq("batch-1"),
                eq("sample order update"),
                any()
        )).thenReturn(new DataMirrorLogEntry());

        service.recordUpdate(
                "sample_orders",
                9L,
                "T1",
                "{\"before\":1}",
                "{\"after\":2}",
                "[\"amount\"]",
                "sample order update",
                "batch-1"
        );

        verify(manager).insertEntry(
                eq("sample_orders"),
                eq(9L),
                eq("T1"),
                eq("UPDATE"),
                eq("{\"before\":1}"),
                eq("{\"after\":2}"),
                eq("[\"amount\"]"),
                eq("batch-1"),
                eq("sample order update"),
                any()
        );
    }

    @Test
    void recordInsertHasNullBeforeJson() {
        ArgumentCaptor<String> beforeCaptor = ArgumentCaptor.forClass(String.class);
        when(manager.insertEntry(
                eq("sample_orders"),
                eq(1L),
                eq("T1"),
                eq("INSERT"),
                beforeCaptor.capture(),
                eq("{\"after\":1}"),
                eq(null),
                eq("batch-2"),
                eq("手工录入"),
                any()
        )).thenReturn(new DataMirrorLogEntry());

        service.recordInsert(
                "sample_orders",
                1L,
                "T1",
                "{\"after\":1}",
                "手工录入",
                "batch-2"
        );

        assertNull(beforeCaptor.getValue());
    }

    @Test
    void snapshotRowQueriesById() {
        when(manager.queryForMap("SELECT * FROM sample_orders WHERE id = ?", 5L))
                .thenReturn(Map.of("id", 5L, "trans_no", "T1"));
        when(manager.serializeRow(any())).thenReturn("{\"id\":5}");

        String json = service.snapshotRow("sample_orders", 5L);

        assertEquals("{\"id\":5}", json);
    }
}
