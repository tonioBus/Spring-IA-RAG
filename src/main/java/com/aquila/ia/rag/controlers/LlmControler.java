package com.aquila.ia.rag.controlers;

import com.aquila.ia.rag.llm.Ollama;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

import java.util.Date;

@Slf4j
@RequiredArgsConstructor
@RestController
public class LlmControler {

    final private Ollama ollama;

    @GetMapping(value = "/llm")
    public String getAnswer(@RequestParam final String prompt) {
        log.info("getAnswer({})", prompt);

        return ollama.answer(prompt);
    }

    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public String greeting(String message) throws Exception {
        Thread.sleep(1000); // simulated delay
        return "Hello, " + HtmlUtils.htmlEscape(new Date() + "!");
    }
}
