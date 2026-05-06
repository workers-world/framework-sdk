package com.books.framework.llm.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

import java.util.*;

public class LLMEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String AI_EXCLUDES = String.join(",",
            "org.springframework.ai.model.openai.autoconfigure.OpenAiAudioSpeechAutoConfiguration",
            "org.springframework.ai.model.openai.autoconfigure.OpenAiAudioTranscriptionAutoConfiguration",
            "org.springframework.ai.model.openai.autoconfigure.OpenAiChatAutoConfiguration",
            "org.springframework.ai.model.openai.autoconfigure.OpenAiEmbeddingAutoConfiguration",
            "org.springframework.ai.model.ollama.autoconfigure.OllamaChatAutoConfiguration",
            "org.springframework.ai.model.ollama.autoconfigure.OllamaEmbeddingAutoConfiguration",
            "org.springframework.ai.model.ollama.autoconfigure.OllamaApiAutoConfiguration"
    );

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment env, SpringApplication app) {
        if ("true".equals(env.getProperty("book.llm.enable"))) {
            return;
        }
        String current = env.getProperty("spring.autoconfigure.exclude", "");
        String combined = StringUtils.hasText(current) ? current + "," + AI_EXCLUDES : AI_EXCLUDES;

        Map<String, Object> props = new HashMap<>();
        props.put("spring.autoconfigure.exclude", combined);
        env.getPropertySources().addFirst(new MapPropertySource("llm-disable-ai-autoconfig", props));
    }
}
