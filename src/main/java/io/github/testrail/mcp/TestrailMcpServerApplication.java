package io.github.testrail.mcp;

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
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class TestrailMcpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestrailMcpServerApplication.class, args);
    }
}
