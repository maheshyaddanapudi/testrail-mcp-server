package io.github.testrail.mcp.config;

import io.github.testrail.mcp.tools.McpExposedTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration that registers the MCP-exposed tools with Spring AI's MCP server.
 *
 * <p>Spring AI's MCP server auto-configuration discovers tools through
 * {@link ToolCallbackProvider} beans. This configuration creates a
 * {@link MethodToolCallbackProvider} that scans {@link McpExposedTools} for
 * {@code @Tool} annotated methods and registers them as MCP tool callbacks.</p>
 *
 * <p>Only the two methods on {@link McpExposedTools} ({@code search_tools} and
 * {@code execute_tool}) are exposed to MCP clients. The 101 internal tools
 * annotated with {@code @InternalTool} remain hidden and are only accessible
 * through these two gateway methods.</p>
 */
@Configuration
public class McpToolConfig {

    /**
     * Creates a {@link ToolCallbackProvider} that wraps the {@link McpExposedTools}
     * component, enabling Spring AI's MCP server to discover and register the
     * {@code search_tools} and {@code execute_tool} methods as MCP tool callbacks.
     */
    @Bean
    public ToolCallbackProvider mcpToolCallbackProvider(McpExposedTools mcpExposedTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(mcpExposedTools)
                .build();
    }
}
