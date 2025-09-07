package com.aquila.ia.mcp.servers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class McpServer1 { // } extends McpServerAutoConfiguration {

    static class AuthorRepository {
        @Tool(description = "Get Baeldung author details using an article title")
        Author getAuthorByArticleTitle(String articleTitle) {
            return new Author("John Doe", "john.doe@baeldung.com");
        }

        @Tool(description = "Get highest rated Baeldung authors")
        List<Author> getTopAuthors() {
            return List.of(
                    new Author("John Doe", "john.doe@baeldung.com"),
                    new Author("Jane Doe", "jane.doe@baeldung.com")
            );
        }

        record Author(String name, String email) {
        }
    }

    @Bean
    ToolCallbackProvider authorTools() {
        return MethodToolCallbackProvider
                .builder()
                .toolObjects(new AuthorRepository())
                .build();
    }

    public McpServer1() {
        super();
        log.info("McpServer1");
    }

}
