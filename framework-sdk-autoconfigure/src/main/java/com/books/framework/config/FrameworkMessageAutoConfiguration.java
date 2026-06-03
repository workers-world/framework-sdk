package com.books.framework.config;

import com.books.framework.message.BaseMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = JacksonAutoConfiguration.class)
@ConditionalOnClass(ObjectMapper.class)
public class FrameworkMessageAutoConfiguration {

    @Bean
    public BaseMessageObjectMapperInitializer baseMessageObjectMapperInitializer(
            ObjectProvider<ObjectMapper> objectMapperProvider) {
        return new BaseMessageObjectMapperInitializer(objectMapperProvider);
    }

    static class BaseMessageObjectMapperInitializer {
        BaseMessageObjectMapperInitializer(ObjectProvider<ObjectMapper> objectMapperProvider) {
            objectMapperProvider.ifAvailable(BaseMessage::setObjectMapper);
        }
    }
}
