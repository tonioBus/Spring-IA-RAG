package com.aquila.ia.rag.llm;

import lombok.Getter;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class Ollama {

    @Value("${spring.ia.ollama.chat.options.model}")
    private String modelName = "deepseek-r1:7b";

    @Value("${spring.ia.ollama.chat.options.temperature}")
    private double temperature = 0.7;

    private final OllamaChatModel chatModel;

    private final OllamaOptions ollamaOptions;

    public Ollama(OllamaChatModel chatModel) {
        this.chatModel = chatModel;
        this.ollamaOptions = OllamaOptions.builder()
                .model(modelName)
                .temperature(temperature)
                .build();
    }

}
