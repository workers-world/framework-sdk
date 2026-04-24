package com.books.framework.message;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BaseMessageTest {

    @Test
    void testNewResponseWithInstanceMethod() {
        BaseMessage request = BaseMessage.newRequest("msg-1", "client-a", "1.0");
        request.setSign("sig-1");

        BaseMessage response = request.newResponse();

        assertEquals("msg-1", response.getMessageId());
        assertEquals(request.getTimestamp(), response.getTimestamp());
        assertEquals("client-a", response.getSource());
        assertEquals("1.0", response.getVersion());
        assertEquals("sig-1", response.getSign());
        assertEquals("SUCCESS", response.getCode());
        assertEquals("操作成功", response.getMessage());
        assertNotNull(response.getResponseTime());
        assertFalse(response.getResponseTime().trim().isEmpty());
    }

    @Test
    void testNewErrorWithInstanceMethod() {
        BaseMessage request = BaseMessage.newRequest("msg-2", "client-b", "1.0");

        BaseMessage response = request.newError("PARAM_ERROR", "参数异常");

        assertEquals("msg-2", response.getMessageId());
        assertEquals("PARAM_ERROR", response.getCode());
        assertEquals("参数异常", response.getMessage());
        assertNotNull(response.getResponseTime());
        assertFalse(response.getResponseTime().trim().isEmpty());
    }

    @Test
    void testGetDtoFromAddedObject() {
        BaseMessage message = new BaseMessage();
        DemoDto dto = new DemoDto();
        dto.setId(100L);
        dto.setName("demo");
        message.addDto(dto);

        DemoDto parsed = message.getDto(DemoDto.class);

        assertNotNull(parsed);
        assertEquals(100L, parsed.getId());
        assertEquals("demo", parsed.getName());
    }

    @Test
    void testGetDtoFromJsonString() {
        BaseMessage message = new BaseMessage();
        message.getData().put("DemoDto", "{\"id\":101,\"name\":\"json\"}");

        DemoDto parsed = message.getDto(DemoDto.class);

        assertNotNull(parsed);
        assertEquals(101L, parsed.getId());
        assertEquals("json", parsed.getName());
    }

    @Test
    void testGetDtoListFromAddedList() {
        BaseMessage message = new BaseMessage();
        DemoDto a = new DemoDto();
        a.setId(1L);
        a.setName("a");
        DemoDto b = new DemoDto();
        b.setId(2L);
        b.setName("b");
        message.addDtoList(Arrays.asList(a, b));

        List<DemoDto> parsed = message.getDtoList(DemoDto.class);

        assertNotNull(parsed);
        assertEquals(2, parsed.size());
        assertEquals(1L, parsed.get(0).getId());
        assertEquals("a", parsed.get(0).getName());
        assertEquals(2L, parsed.get(1).getId());
        assertEquals("b", parsed.get(1).getName());
    }

    @Test
    void testGetDtoListFromJsonString() {
        BaseMessage message = new BaseMessage();
        message.getData().put("DemoDto", "[{\"id\":11,\"name\":\"x\"},{\"id\":12,\"name\":\"y\"}]");

        List<DemoDto> parsed = message.getDtoList(DemoDto.class);

        assertNotNull(parsed);
        assertEquals(2, parsed.size());
        assertEquals(11L, parsed.get(0).getId());
        assertEquals("x", parsed.get(0).getName());
        assertEquals(12L, parsed.get(1).getId());
        assertEquals("y", parsed.get(1).getName());
    }

    static class DemoDto {
        private Long id;
        private String name;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
