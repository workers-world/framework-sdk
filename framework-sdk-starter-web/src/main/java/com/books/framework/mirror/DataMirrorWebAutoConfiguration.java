package com.books.framework.mirror;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@AutoConfigureAfter(DataMirrorAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class DataMirrorWebAutoConfiguration {

    @Bean
    @ConditionalOnBean(DataMirrorQueryService.class)
    @ConditionalOnMissingBean(DataMirrorController.class)
    public DataMirrorController dataMirrorController(DataMirrorQueryService queryService) {
        return new DataMirrorController(queryService);
    }
}
