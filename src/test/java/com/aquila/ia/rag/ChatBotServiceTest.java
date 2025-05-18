package com.aquila.ia.rag;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;

import java.util.stream.Collectors;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Main.class)
@ActiveProfiles("test")
class ChatBotServiceTest {

    @Autowired
    private ChatBotService chatBotService;

    @Test
    void test() {
        ChatClient chatClient = chatBotService.getChatClient();
        Advisor retrievalAugmentationAdvisor = chatBotService.getRetrievalAugmentationAdvisor();
        final String question = "give me example of a typeB formatted message";
        ChatClient.StreamResponseSpec answer = chatClient.prompt()
                .user(question)
                .advisors(retrievalAugmentationAdvisor)
                .stream();
        Flux<String> answer1 = answer.content().
                transformDeferred(flux -> flux.map(sz -> sz.replace("\n", "<br/>")));
//        log.info("content = \n{}", answer.content());
//        answer = chatClient.prompt()
//                .user(question)
//                .advisors(retrievalAugmentationAdvisor)
//                .call();
//        log.info("chatResponse = \n{}", answer.chatResponse());
//        answer = chatClient.prompt()
//                .user(question)
//                .advisors(retrievalAugmentationAdvisor)
//                .call();
        // answer1.doOnComplete(sz -> log.info("chatClientResponse = \n{}", sz));
        answer1.toStream().forEach(System.out::printf);
        // log.info("chatClientResponse = \n{}", answer1.toStream().collect(Collectors.joining()));
    }
}