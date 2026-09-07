package com.books.framework.logging;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.client.RestClient;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 回归防护:OutboundTimeoutProperties 一度漏标 @ConfigurationProperties,
 * 导致引用本自动配置的应用启动即崩(No ConfigurationProperties annotation found)。
 * 此测试确保属性绑定与 customizer 注册始终可用。
 */
@Tag("unit")
class FrameworkRestClientOutboundLogAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(FrameworkRestClientOutboundLogAutoConfiguration.class))
            .withBean(RestClient.Builder.class, RestClient::builder);

    @Test
    void registersCustomizerAndBindsTimeouts() {
        runner.withPropertyValues(
                        "books.framework.access-log.outbound.connect-timeout=2s",
                        "books.framework.access-log.outbound.read-timeout=5s")
                .run(ctx -> {
                    assertThat(ctx).hasNotFailed();
                    assertThat(ctx).hasSingleBean(RestClientCustomizerAccessor.customizerClass());
                    FrameworkRestClientOutboundLogAutoConfiguration.OutboundTimeoutProperties props =
                            ctx.getBean(FrameworkRestClientOutboundLogAutoConfiguration.OutboundTimeoutProperties.class);
                    assertThat(props.getConnectTimeout()).isEqualTo(Duration.ofSeconds(2));
                    assertThat(props.getReadTimeout()).isEqualTo(Duration.ofSeconds(5));
                });
    }

    @Test
    void timeoutsDefaultToNullSoFactoryIsNotTouched() {
        runner.run(ctx -> {
            assertThat(ctx).hasNotFailed();
            FrameworkRestClientOutboundLogAutoConfiguration.OutboundTimeoutProperties props =
                    ctx.getBean(FrameworkRestClientOutboundLogAutoConfiguration.OutboundTimeoutProperties.class);
            assertThat(props.getConnectTimeout()).isNull();
            assertThat(props.getReadTimeout()).isNull();
        });
    }

    /** 独立持有 customizer 类型,便于断言可读 */
    private static final class RestClientCustomizerAccessor {
        static Class<?> customizerClass() {
            return org.springframework.boot.restclient.RestClientCustomizer.class;
        }
    }
}
