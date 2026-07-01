package com.books.framework.mirror;

import com.books.framework.manager.DataMirrorManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataMirrorQueryServiceTest {

    @Mock
    private DataMirrorManager manager;

    @InjectMocks
    private DataMirrorQueryService service;

    @Test
    void getDetailUsesCustomLabelProvider() {
        DataMirrorLabelResolver resolver = new DataMirrorLabelResolver(
                List.of(new TestLabelProvider()),
                new DefaultDataMirrorFieldLabelProvider()
        );
        service = new DataMirrorQueryService(manager, resolver);

        DataMirrorLogEntry entry = new DataMirrorLogEntry();
        entry.setId(1L);
        entry.setTableName("ORIGINAL_BILL_TRANSACTION_T");
        entry.setRecordId(10L);
        entry.setBizKey("T1");
        entry.setOperation("UPDATE");
        entry.setBeforeJson("{\"amount\":-100}");
        entry.setAfterJson("{\"amount\":100}");
        entry.setChangedFields("[\"amount\"]");
        entry.setCreatedAt(LocalDateTime.now());

        when(manager.findById(1L)).thenReturn(entry);

        DataMirrorDetailZ1 detail = service.getDetail(1L);
        DataMirrorFieldDiffZ1 amount = detail.getFieldDiffs().stream()
                .filter(d -> "amount".equals(d.getFieldKey()))
                .findFirst()
                .orElseThrow();

        assertEquals("金额", amount.getLabel());
        assertTrue(amount.isChanged());
    }

    private static class TestLabelProvider implements DataMirrorFieldLabelProvider {
        @Override
        public boolean supports(String tableName) {
            return "ORIGINAL_BILL_TRANSACTION_T".equals(tableName);
        }

        @Override
        public String label(String tableName, String fieldKey) {
            if ("amount".equals(fieldKey)) {
                return "金额";
            }
            return null;
        }
    }
}
