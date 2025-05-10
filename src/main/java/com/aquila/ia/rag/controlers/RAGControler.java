package com.aquila.ia.rag.controlers;

import com.aquila.ia.rag.ChatBotService;
import com.aquila.ia.rag.DataLoaderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@RequiredArgsConstructor
@RestController
public class RAGControler {

    // final private Ollama ollama;
    final private ChatBotService chatBotService;
    final private DataLoaderService dataLoaderService;

    @GetMapping(value = "/llm")
    public String getAnswer(@RequestParam final String prompt) {
        log.info("getAnswer({})", prompt);
        return chatBotService.createPrompt(prompt);
    }

    @GetMapping("/stream")
    @Async
    public Flux<String> getStreamedResponse(@RequestParam String message) {
        return chatBotService.createPromptFlux(message);
    }


    @GetMapping(value = "/reload")
    public void reload() {
        dataLoaderService.load();
    }

}
