package com.aquila.ia.rag.llm;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class Ollama {

    final private static String MODEL_NAME = "deepseek-r1:7b";

    private final OllamaChatModel chatModel;
    // private final OllamaOptions ollamaOptions;

    public Ollama(OllamaChatModel chatModel) {
        this.chatModel = chatModel;
//        this.ollamaOptions = OllamaOptions.builder()
//                .model(MODEL_NAME) // OllamaModel.LLAMA3_1)
//                .temperature(0.4)
//                .build();
    }

    public String answer(final String prompt) {
        ChatResponse response = chatModel.call(
                new Prompt(
                        prompt//, // "Generate the names of 5 famous pirates.",
                        // ollamaOptions
                ));
        return response.getResults().stream()
                .map(Generation::toString)
                .collect(Collectors.joining("\n"));
    }

}
