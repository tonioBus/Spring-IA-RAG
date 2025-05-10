package com.aquila.ia.rag;

import com.aquila.ia.rag.llm.Ollama;
import io.micrometer.observation.ObservationRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.DefaultChatClientBuilder;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class ChatBotService {

    private final Ollama ollama;

    private final VectorStore vectorStore;

    private ChatClient chatClient;

    private Advisor retrievalAugmentationAdvisor;

    private DefaultChatClientBuilder chatClientBuilder;

    @PostConstruct
    void postConstruct() {
        chatClient = ChatClient.builder(ollama.getChatModel())
                .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore)
                        .searchRequest(SearchRequest.builder().build())
                        .build())
                .build();
        chatClientBuilder = new DefaultChatClientBuilder(ollama.getChatModel(), ObservationRegistry.NOOP, null);
        retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
                .queryTransformers(RewriteQueryTransformer.builder()
                        .chatClientBuilder(chatClientBuilder.build().mutate())
                        .build())
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .similarityThreshold(0.50)
                        .vectorStore(vectorStore)
                        .build())
                .build();
    }

    // @Autowired
    // private DataRetrievalService dataRetrievalService;

    private final String PROMPT_BLUEPRINT = """
              Answer the query strictly referring the provided context:
              {context}
              Query:
              {query}
              In case you don't have any answer from the context provided, just say:
              I'm sorry I don't have the information you are looking for.
            """;


//    public String chat(String query) {
//        return chatClient.call(createPrompt(query, null)); //dataRetrievalService.searchData(query)));
//    }

    /*
    private String createPrompt1(String query, List<Document> context) {
        PromptTemplate promptTemplate = new PromptTemplate(PROMPT_BLUEPRINT);
        promptTemplate.add("query", query);
        // promptTemplate.add("context", context);
        return promptTemplate.render();
    }*/

    public String createPrompt(String question) {
        return chatClient.prompt()
                .advisors(retrievalAugmentationAdvisor)
                .user(question)
                .call()
                .content();
    }

    public Flux<String> createPromptFlux(String question) {
        return chatClient.prompt()
                .user(question)
                .advisors(retrievalAugmentationAdvisor)
                .stream()
                .content();
    }


}
