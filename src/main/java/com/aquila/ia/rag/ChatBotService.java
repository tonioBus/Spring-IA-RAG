package com.aquila.ia.rag;

import com.aquila.ia.rag.llm.Ollama;
import io.micrometer.observation.ObservationRegistry;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.DefaultChatClientBuilder;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class ChatBotService {

    private final Ollama ollama;

    private final VectorStore vectorStore;

    @Getter
    private ChatClient chatClient;

    @Getter
    private Advisor retrievalAugmentationAdvisor;

    private DefaultChatClientBuilder chatClientBuilder;

    @PostConstruct
    void postConstruct() {
        chatClientBuilder = new DefaultChatClientBuilder(ollama.getChatModel(), ObservationRegistry.NOOP, null);
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
        chatClient = ChatClient.builder(ollama.getChatModel())
                .defaultAdvisors(retrievalAugmentationAdvisor)
//                        .searchRequest(SearchRequest.builder()
//                                .similarityThreshold(0.8d)
//                                .topK(6)
//                                .build())
//                        .build())
                .build();
    }

    @Deprecated
    public String createPrompt(String question) {
        return chatClient.prompt()
                // .advisors(retrievalAugmentationAdvisor)
                .user(question)
                .call()
                .content();
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
