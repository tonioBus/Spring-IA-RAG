package com.aquila.ia.rag.controlers;

import com.aquila.ia.rag.ChatBotService;
import com.aquila.ia.rag.DataLoaderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.StreamingModel;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Date;

@Slf4j
@RequiredArgsConstructor
@RestController
public class LlmControler {

    // final private Ollama ollama;
    final private ChatBotService chatBotService;
    final private DataLoaderService dataLoaderService;

    @GetMapping(value = "/llm")
    public String getAnswer(@RequestParam final String prompt) {
        log.info("getAnswer({})", prompt);
        return chatBotService.createPrompt(prompt);
    }

    @GetMapping("/stream")
    public Flux<String> getStreamedResponse(@RequestParam String message) {
        return chatBotService.createPromptFlux(message);
    }


    @GetMapping(value = "/reload")
    public void reload() {
        dataLoaderService.load();
    }

}
