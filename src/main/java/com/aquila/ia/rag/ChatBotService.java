package com.aquila.ia.rag;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micrometer.observation.ObservationRegistry;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.DefaultChatClientBuilder;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatBotService {

    private final OllamaChatModel ollamaChatModel;

    private final VectorStore vectorStore;

    @Getter
    private ChatClient chatClient;

    @Getter
    private Advisor retrievalAugmentationAdvisor;

    @PostConstruct
    void postConstruct() {
        log.info("ollamaChatModel:{}", ollamaChatModel);
        final DefaultChatClientBuilder chatClientBuilder = new DefaultChatClientBuilder(ollamaChatModel,
                ObservationRegistry.NOOP,
                null);
        retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
                .queryTransformers(RewriteQueryTransformer.builder()
                        .chatClientBuilder(chatClientBuilder.build().mutate())
                        .build())
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        // .similarityThreshold(0.73)
                        // .topK(5)
                        .vectorStore(vectorStore)
                        .build())
                .build();
        record MathReasoning(
                @JsonProperty(required = true, value = "steps") Steps steps,
                @JsonProperty(required = true, value = "final_answer") String finalAnswer) {

            record Steps(
                    @JsonProperty(required = true, value = "items") Items[] items) {

                record Items(
                        @JsonProperty(required = true, value = "explanation") String explanation,
                        @JsonProperty(required = true, value = "output") String output) {
                }
            }
        }

        var outputConverter = new BeanOutputConverter<>(MathReasoning.class);
        ChatOptions chatOptions = OllamaOptions.builder()
                //.model(OllamaModel.LLAMA3_2.getName())
                .format(outputConverter.getJsonSchemaMap())
                .build();
        chatClient = ChatClient.builder(ollamaChatModel)
                .defaultAdvisors(retrievalAugmentationAdvisor)
//                        .searchRequest(SearchRequest.builder()
//                                .similarityThreshold(0.8d)
//                                .topK(6)
//                                .build())
//                        .build())
                //.defaultOptions(chatOptions)
                .build();
    }


    public Flux<String> createPromptFlux(String question) {
        return chatClient.prompt()
                .user(question)
                .advisors(retrievalAugmentationAdvisor)
                .stream()
                .content().
                transformDeferred(flux -> flux.map(sz -> sz.replace("\n", "\r\n")));
    }

}
