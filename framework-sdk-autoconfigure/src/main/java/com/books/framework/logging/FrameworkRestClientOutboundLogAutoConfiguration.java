package com.books.framework.logging;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * 为注入的 {@link RestClient.Builder} 追加出站日志拦截器。
 *
 * <p>设计约束(修复历史缺陷):
 * <ul>
 *   <li><b>不抢占 request factory</b>:默认只追加拦截器,保留应用/Boot 默认选择的客户端引擎
 *       (连接池、HTTP/2 等)。历史版本无条件替换为无超时的 {@link SimpleClientHttpRequestFactory},
 *       导致未自行设置超时的消费方继承无限读超时,且客户端引擎被降级。</li>
 *   <li><b>不再全局套 BufferingClientHttpRequestFactory</b>:响应体缓存由
 *       {@link OutboundHttpLogInterceptor} 在「DEBUG + 开启响应体日志」时按需完成
 *       ({@code CachingClientHttpResponseWrapper}),无谓的全量缓冲白白占用内存。</li>
 *   <li><b>超时为显式 opt-in</b>:配置 {@code books.framework.access-log.outbound-connect-timeout} /
 *       {@code outbound-read-timeout}(Duration)后,才由本自动配置设置带超时的工厂;
 *       不配置则不干预工厂,超时责任归应用(与 Boot 默认行为一致)。</li>
 * </ul>
 */
@AutoConfiguration
@ConditionalOnClass(RestClient.class)
@ConditionalOnProperty(prefix = "books.framework.access-log", name = "outbound-enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(FrameworkRestClientOutboundLogAutoConfiguration.OutboundTimeoutProperties.class)
public class FrameworkRestClientOutboundLogAutoConfiguration {

    @Bean
    public RestClientCustomizer outboundHttpLogRestClientCustomizer(OutboundTimeoutProperties timeoutProperties) {
        return builder -> {
            if (timeoutProperties.getConnectTimeout() != null || timeoutProperties.getReadTimeout() != null) {
                SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
                if (timeoutProperties.getConnectTimeout() != null) {
                    factory.setConnectTimeout((int) timeoutProperties.getConnectTimeout().toMillis());
                }
                if (timeoutProperties.getReadTimeout() != null) {
                    factory.setReadTimeout((int) timeoutProperties.getReadTimeout().toMillis());
                }
                builder.requestFactory(factory);
            }
            builder.requestInterceptor(new OutboundHttpLogInterceptor());
        };
    }

    /** 出站客户端超时属性;两者皆空(默认)表示不干预 request factory。 */
    public static class OutboundTimeoutProperties {

        /** 连接超时;null 表示不设置(不干预工厂选择) */
        private java.time.Duration connectTimeout;
        /** 读超时;null 表示不设置 */
        private java.time.Duration readTimeout;

        public java.time.Duration getConnectTimeout() {
            return connectTimeout;
        }

        public void setConnectTimeout(java.time.Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        public java.time.Duration getReadTimeout() {
            return readTimeout;
        }

        public void setReadTimeout(java.time.Duration readTimeout) {
            this.readTimeout = readTimeout;
        }
    }
}
