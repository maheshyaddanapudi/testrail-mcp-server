package io.github.testrail.mcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import java.io.IOException;

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
 *   <li>MCP_MODE - Set to "true" to enable stdin monitoring (for MCP clients like Cursor)</li>
 * </ul>
 *
 * <h2>MCP Mode</h2>
 * <p>When MCP_MODE=true, the application monitors stdin and automatically shuts down when the
 * parent process disconnects. This prevents orphaned processes when MCP clients restart.</p>
 *
 * <p>For standalone usage (terminal, nohup, systemd), leave MCP_MODE unset or set to "false".</p>
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class TestrailMcpServerApplication {

    private static final Logger log = LoggerFactory.getLogger(TestrailMcpServerApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(TestrailMcpServerApplication.class, args);

        // Only monitor stdin if explicitly running in MCP mode
        if (isMcpMode()) {
            log.info("MCP mode enabled - monitoring stdin for parent process lifecycle");
            startStdinMonitor();
        } else {
            log.info("Running in standalone mode - stdin monitoring disabled");
        }
    }

    /**
     * Checks if the application is running in MCP mode.
     * MCP mode is enabled by setting the MCP_MODE environment variable to "true" or "1".
     *
     * @return true if MCP mode is enabled
     */
    private static boolean isMcpMode() {
        String mcpMode = System.getenv("MCP_MODE");
        return "true".equalsIgnoreCase(mcpMode) || "1".equals(mcpMode);
    }

    /**
     * Starts a background thread that monitors stdin for EOF.
     * When stdin closes (parent process disconnected), the application shuts down gracefully.
     * This prevents orphaned processes when MCP clients like Cursor restart.
     */
    private static void startStdinMonitor() {
        Thread monitor = new Thread(() -> {
            try {
                byte[] buffer = new byte[1024];
                int bytesRead;
                // Keep reading until stdin closes (returns -1)
                while ((bytesRead = System.in.read(buffer)) != -1) {
                    // In MCP protocol, we may receive data on stdin
                    // Continue reading until EOF
                    if (log.isTraceEnabled()) {
                        log.trace("Read {} bytes from stdin", bytesRead);
                    }
                }
                log.warn("Parent process disconnected (stdin closed) - shutting down gracefully");
                System.exit(0);
            } catch (IOException e) {
                log.error("Error monitoring stdin - shutting down", e);
                System.exit(1);
            }
        }, "mcp-stdin-monitor");

        // Make it a daemon thread so it doesn't prevent JVM shutdown
        monitor.setDaemon(true);
        monitor.start();

        log.debug("Stdin monitor thread started");
    }
}
