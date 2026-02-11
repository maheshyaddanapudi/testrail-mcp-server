package io.github.testrail.mcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * TestRail MCP Server Application.
 *
 * <p>This Spring Boot application exposes TestRail API operations as MCP (Model Context Protocol)
 * tools that can be invoked by AI assistants like Cursor. The server communicates via STDIO
 * and keeps all credentials secure on the local machine.</p>
 *
 * <p>Configuration is done via environment variables:</p>
 * <ul>
 *   <li>TESTRAIL_URL - The base URL of your TestRail instance</li>
 *   <li>TESTRAIL_USERNAME - Your TestRail username/email</li>
 *   <li>TESTRAIL_API_KEY - Your TestRail API key</li>
 * </ul>
 *
 * <h2>Lifecycle Management</h2>
 * <p>The application uses Spring AI's MCP server implementation, which automatically monitors
 * stdin and shuts down gracefully when the parent process (e.g., Cursor) disconnects. This
 * prevents orphaned processes when MCP clients restart.</p>
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class TestrailMcpServerApplication {

    private static final Logger log = LoggerFactory.getLogger(TestrailMcpServerApplication.class);

    public static void main(String[] args) {
        log.info("Starting TestRail MCP Server...");
        SpringApplication.run(TestrailMcpServerApplication.class, args);
        log.info("TestRail MCP Server started successfully");
    }
}
