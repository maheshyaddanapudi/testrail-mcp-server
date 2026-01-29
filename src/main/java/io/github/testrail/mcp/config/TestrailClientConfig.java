package io.github.testrail.mcp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;

/**
 * Configuration for the TestRail API client.
 *
 * <p>Creates a pre-configured WebClient with Basic Authentication
 * using the credentials from TestrailProperties.</p>
 */
@Configuration
public class TestrailClientConfig {

    private final TestrailProperties properties;

    public TestrailClientConfig(TestrailProperties properties) {
        this.properties = properties;
    }

    /**
     * Creates a WebClient configured for TestRail API calls.
     *
     * <p>The client is pre-configured with:</p>
     * <ul>
     *   <li>Base URL pointing to TestRail API v2</li>
     *   <li>Basic Authentication header</li>
     *   <li>JSON content type</li>
     * </ul>
     *
     * @return configured WebClient instance
     */
    @Bean
    public WebClient testrailWebClient() {
        String credentials = properties.getUsername() + ":" + properties.getApiKey();
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

        return WebClient.builder()
                .baseUrl(properties.getApiUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodedCredentials)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
