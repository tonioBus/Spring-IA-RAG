package com.aquila.ia.mcp.servers;

import io.modelcontextprotocol.server.*;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpServerTransportProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.server.autoconfigure.McpServerAutoConfiguration;
import org.springframework.ai.mcp.server.autoconfigure.McpServerProperties;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.BiConsumer;

@Slf4j
@Component
public class Example1 extends McpServerAutoConfiguration {

    public Example1() {
        super();
        log.info("Example1");
    }

    @Override
    public McpServerTransportProvider stdioServerTransport() {
        log.info("stdioServerTransport");
        return super.stdioServerTransport();
    }

    @Override
    public McpSchema.ServerCapabilities.Builder capabilitiesBuilder() {
        log.info("capabilitiesBuilder");
        return super.capabilitiesBuilder();
    }

    @Override
    public List<McpServerFeatures.SyncToolSpecification> syncTools(ObjectProvider<List<ToolCallback>> toolCalls, List<ToolCallback> toolCallbacksList, McpServerProperties serverProperties) {
        log.info("syncTools");
        return super.syncTools(toolCalls, toolCallbacksList, serverProperties);
    }

    @Override
    public McpSyncServer mcpSyncServer(McpServerTransportProvider transportProvider, McpSchema.ServerCapabilities.Builder capabilitiesBuilder, McpServerProperties serverProperties, ObjectProvider<List<McpServerFeatures.SyncToolSpecification>> tools, ObjectProvider<List<McpServerFeatures.SyncResourceSpecification>> resources, ObjectProvider<List<McpServerFeatures.SyncPromptSpecification>> prompts, ObjectProvider<List<McpServerFeatures.SyncCompletionSpecification>> completions, ObjectProvider<BiConsumer<McpSyncServerExchange, List<McpSchema.Root>>> rootsChangeConsumers, List<ToolCallbackProvider> toolCallbackProvider) {
        log.info("mcpSyncServer");
        return super.mcpSyncServer(transportProvider, capabilitiesBuilder, serverProperties, tools, resources, prompts, completions, rootsChangeConsumers, toolCallbackProvider);
    }

    @Override
    public List<McpServerFeatures.AsyncToolSpecification> asyncTools(ObjectProvider<List<ToolCallback>> toolCalls, List<ToolCallback> toolCallbackList, McpServerProperties serverProperties) {
        log.info("asyncTools");
        return super.asyncTools(toolCalls, toolCallbackList, serverProperties);
    }

    @Override
    public McpAsyncServer mcpAsyncServer(McpServerTransportProvider transportProvider, McpSchema.ServerCapabilities.Builder capabilitiesBuilder, McpServerProperties serverProperties, ObjectProvider<List<McpServerFeatures.AsyncToolSpecification>> tools, ObjectProvider<List<McpServerFeatures.AsyncResourceSpecification>> resources, ObjectProvider<List<McpServerFeatures.AsyncPromptSpecification>> prompts, ObjectProvider<List<McpServerFeatures.AsyncCompletionSpecification>> completions, ObjectProvider<BiConsumer<McpAsyncServerExchange, List<McpSchema.Root>>> rootsChangeConsumer, List<ToolCallbackProvider> toolCallbackProvider) {
        log.info("mcpAsyncServer");
        return super.mcpAsyncServer(transportProvider, capabilitiesBuilder, serverProperties, tools, resources, prompts, completions, rootsChangeConsumer, toolCallbackProvider);
    }
}
