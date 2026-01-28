package io.github.testrail.mcp.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for TestRail API connection.
 *
 * <p>These properties are loaded from environment variables or application.yml:</p>
 * <ul>
 *   <li>TESTRAIL_URL or testrail.base-url</li>
 *   <li>TESTRAIL_USERNAME or testrail.username</li>
 *   <li>TESTRAIL_API_KEY or testrail.api-key</li>
 * </ul>
 */
@Validated
@ConfigurationProperties(prefix = "testrail")
public class TestrailProperties {

    @NotBlank(message = "TestRail base URL is required. Set TESTRAIL_URL environment variable.")
    private String baseUrl;

    @NotBlank(message = "TestRail username is required. Set TESTRAIL_USERNAME environment variable.")
    private String username;

    @NotBlank(message = "TestRail API key is required. Set TESTRAIL_API_KEY environment variable.")
    private String apiKey;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Returns the full API base URL with the API v2 path appended.
     *
     * @return the complete API URL
     */
    public String getApiUrl() {
        String url = baseUrl;
        if (!url.endsWith("/")) {
            url += "/";
        }
        return url + "index.php?/api/v2/";
    }
}
