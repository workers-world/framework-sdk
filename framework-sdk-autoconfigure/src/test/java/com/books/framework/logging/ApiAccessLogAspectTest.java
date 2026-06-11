package com.books.framework.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.books.framework.annotation.ApiName;
import com.books.framework.config.FrameworkProperties;
import com.books.framework.constants.ErrorCodeEnum;
import com.books.framework.exception.AppException;
import com.books.framework.message.BaseMessage;
import com.books.framework.trace.TraceIdUtils;
import com.books.framework.validation.ApiNameBaseMessageValidationAspect;
import com.books.framework.validation.BeanValidationRunner;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringJUnitConfig(ApiAccessLogAspectTest.TestConfig.class)
class ApiAccessLogAspectTest {

    private static final String LOGGER_NAME = "com.books.framework.logging.ApiAccessLog";

    @org.springframework.beans.factory.annotation.Autowired
    private TestController testController;

    @org.springframework.beans.factory.annotation.Autowired
    private FrameworkProperties frameworkProperties;

    private ListAppender<ILoggingEvent> listAppender;

    @Data
    public static class TestX1 {
        @NotBlank(message = "name 不能为空")
        private String name;
    }

    @RestController
    static class TestController {

        @ApiName(name = "createItem", input = {TestX1.class}, output = {TestX1.class})
        @PostMapping("/api/test/create")
        public BaseMessage create(@RequestBody(required = false) BaseMessage request) {
            BaseMessage response = request.newResponse();
            response.addDto(new TestX1());
            return response;
        }

        @GetMapping("/api/test/plain")
        public String plain() {
            return "ok";
        }

        @GetMapping("/actuator/health")
        public String health() {
            return "UP";
        }
    }

    @Configuration
    @EnableAspectJAutoProxy
    static class TestConfig {

        @Bean
        FrameworkProperties frameworkProperties() {
            return new FrameworkProperties();
        }

        @Bean
        TestController testController() {
            return new TestController();
        }

        @Bean
        AccessLogSanitizer accessLogSanitizer(FrameworkProperties properties) {
            FrameworkProperties.AccessLog accessLog = properties.getAccessLog();
            return new AccessLogSanitizer(new HashSet<>(accessLog.getMaskFields()), accessLog.getMaxBodyLength());
        }

        @Bean
        ApiAccessLogAspect apiAccessLogAspect(FrameworkProperties properties, AccessLogSanitizer sanitizer) {
            return new ApiAccessLogAspect(properties.getAccessLog(), sanitizer);
        }

        @Bean
        Validator validator() {
            return jakarta.validation.Validation.buildDefaultValidatorFactory().getValidator();
        }

        @Bean
        BeanValidationRunner beanValidationRunner(Validator validator) {
            return new BeanValidationRunner(validator);
        }

        @Bean
        ApiNameBaseMessageValidationAspect apiNameBaseMessageValidationAspect(BeanValidationRunner runner) {
            return new ApiNameBaseMessageValidationAspect(runner);
        }
    }

    @BeforeEach
    void setUp() {
        FrameworkProperties.AccessLog accessLog = frameworkProperties.getAccessLog();
        accessLog.setEnabled(true);
        accessLog.setRequireApiName(false);
        accessLog.setLogRequestBody(true);
        accessLog.setLogResponseBody(true);
        accessLog.setExcludePathPatterns(List.of("/actuator/**"));

        Logger logger = (Logger) LoggerFactory.getLogger(LOGGER_NAME);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        logger.setLevel(Level.DEBUG);

        TraceIdUtils.set("test-trace-id");
        bindRequest("/api/test/create", "POST");
    }

    @AfterEach
    void tearDown() {
        TraceIdUtils.clear();
        RequestContextHolder.resetRequestAttributes();
        Logger logger = (Logger) LoggerFactory.getLogger(LOGGER_NAME);
        logger.detachAppender(listAppender);
    }

    @Test
    @DisplayName("带 @ApiName 的接口记录 access 元数据")
    void shouldLogApiNameMetadata() {
        TestX1 dto = new TestX1();
        dto.setName("alice");
        BaseMessage request = BaseMessage.builder().messageId("msg-001").addDto(dto).build();

        testController.create(request);

        String infoLog = findLogByLevel(Level.INFO);
        assertThat(infoLog).contains("api_access");
        assertThat(infoLog).contains("apiName=createItem");
        assertThat(infoLog).contains("controller=TestController");
        assertThat(infoLog).contains("httpMethod=POST");
        assertThat(infoLog).contains("uri=/api/test/create");
        assertThat(infoLog).contains("messageId=msg-001");
        assertThat(infoLog).contains("inputType=TestX1");
        assertThat(infoLog).contains("code=SUCCESS");
        assertThat(infoLog).contains("traceId=test-trace-id");
        assertThat(infoLog).contains("durationMs=");
    }

    @Test
    @DisplayName("无 @ApiName 时 fallback 到 class#method")
    void shouldFallbackApiNameWhenAnnotationMissing() {
        bindRequest("/api/test/plain", "GET");
        listAppender.list.clear();

        testController.plain();

        String infoLog = findLogByLevel(Level.INFO);
        assertThat(infoLog).contains("apiName=TestController#plain");
        assertThat(infoLog).contains("inputType=-");
    }

    @Test
    @DisplayName("require-api-name=true 时跳过无注解方法")
    void shouldSkipWhenRequireApiNameAndAnnotationMissing() {
        frameworkProperties.getAccessLog().setRequireApiName(true);
        bindRequest("/api/test/plain", "GET");
        listAppender.list.clear();

        testController.plain();

        assertThat(listAppender.list).isEmpty();
    }

    @Test
    @DisplayName("exclude 路径不记日志")
    void shouldSkipExcludedPath() {
        bindRequest("/actuator/health", "GET");
        listAppender.list.clear();

        testController.health();

        assertThat(listAppender.list).isEmpty();
    }

    @Test
    @DisplayName("校验失败仍记录 access 且 code 非 SUCCESS")
    void shouldLogValidationFailure() {
        TestX1 dto = new TestX1();
        dto.setName("");
        BaseMessage request = BaseMessage.builder().messageId("msg-002").addDto(dto).build();
        listAppender.list.clear();

        assertThatThrownBy(() -> testController.create(request))
                .isInstanceOf(AppException.class);

        String warnLog = findLogByLevel(Level.WARN);
        assertThat(warnLog).contains("apiName=createItem");
        assertThat(warnLog).contains("code=" + ErrorCodeEnum.PARAM_ERROR.getCode());
        assertThat(warnLog).contains("error=AppException");
    }

    @Test
    @DisplayName("DEBUG 级别记录脱敏后的 request/response body")
    void shouldLogMaskedBodyOnDebug() {
        TestX1 dto = new TestX1();
        dto.setName("alice");
        BaseMessage request = BaseMessage.builder().messageId("msg-003").addDto(dto).build();
        request.put("token", "secret-token");
        listAppender.list.clear();

        testController.create(request);

        String requestBodyLog = listAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .filter(message -> message.contains("api_access_request_body"))
                .findFirst()
                .orElse("");
        String responseBodyLog = listAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .filter(message -> message.contains("api_access_response_body"))
                .findFirst()
                .orElse("");

        assertThat(requestBodyLog).contains("alice");
        assertThat(requestBodyLog).contains("\"token\":\"****\"");
        assertThat(requestBodyLog).doesNotContain("secret-token");
        assertThat(responseBodyLog).contains("TestX1");
    }

    private void bindRequest(String uri, String method) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        request.setRemoteAddr("127.0.0.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private String findLogByLevel(Level level) {
        return listAppender.list.stream()
                .filter(event -> event.getLevel() == level)
                .map(ILoggingEvent::getFormattedMessage)
                .filter(message -> message.contains("api_access"))
                .findFirst()
                .orElse("");
    }
}
