package com.example.domainchecker.service;

import com.example.domainchecker.entity.DomainResult;
import com.example.domainchecker.repository.DomainResultRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

// Selenium
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

@Service
public class DomainCheckerService {

    @Autowired
    private DomainResultRepository domainResultRepository;

    @Value("${virustotal.api.key:}")
    private String virusTotalApiKey;

    @Value("${virustotal.api.url:https://www.virustotal.com/api/v3/domains/}")
    private String virusTotalApiUrl;

    @Value("${http.client.timeout:5000}")
    private int httpTimeout;

    @Value("${http.client.connection-timeout:3000}")
    private int connectionTimeout;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public DomainCheckerService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(3000))
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public DomainResult checkDomain(String domain) {
        String clean = cleanDomainName(domain);
        boolean isReachable = checkReachable(clean);
        int status = checkHttpStatus(clean);
        String vt = checkVirusTotal(clean);
        DomainResult result = new DomainResult(clean, isReachable, status, vt);
        // Populate details if available
        String details = getVirusTotalDetails(clean);
        result.setVirusDetails(details);
        // Curl-style check (non-persisted)
        String curlSummary = checkCurlStyle(clean);
        result.setCurlCheck(curlSummary);
        // Selenium lightweight check (optional best-effort)
        try {
            runSeleniumCheck(result);
        } catch (Exception ignored) {
            // keep non-fatal
        }
        return domainResultRepository.save(result);
    }

    /**
     * Perform a curl-like HTTP check using Java HttpClient.
     * Tries HTTPS first, then HTTP, follows redirects, returns a compact summary.
     */
    public String checkCurlStyle(String domain) {
        String[] protocols = {"https://", "http://"};
        for (String protocol : protocols) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(protocol + domain))
                        .timeout(Duration.ofMillis(Math.max(httpTimeout, 5000)))
                        .header("User-Agent", "curl/8.0")
                        .method("GET", HttpRequest.BodyPublishers.noBody())
                        .build();
                long start = System.nanoTime();
                HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
                long elapsedMs = (System.nanoTime() - start) / 1_000_000;
                int code = response.statusCode();
                return (protocol.startsWith("https") ? "HTTPS" : "HTTP") + " " + code + " (" + elapsedMs + " ms)";
            } catch (Exception ignored) {
            }
        }
        return "No Response";
    }

    public boolean checkReachable(String domain) {
        try {
            InetAddress inet = InetAddress.getByName(domain);
            return inet.isReachable(3000);
        } catch (IOException e) {
            return false;
        }
    }

    public int checkHttpStatus(String domain) {
        String[] protocols = {"http://", "https://"};
        for (String protocol : protocols) {
            try {
                URL url = new URL(protocol + domain);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(connectionTimeout);
                connection.setReadTimeout(10000);
                connection.setInstanceFollowRedirects(true);
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                int code = connection.getResponseCode();
                connection.disconnect();
                return code;
            } catch (IOException ignored) {
            }
        }
        return -1;
    }

    public String checkVirusTotal(String domain) {
        try {
            if (virusTotalApiKey == null || virusTotalApiKey.isBlank()) {
                return "Not Checked (API Key Required)";
            }
            String apiUrl = virusTotalApiUrl + domain;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("x-apikey", virusTotalApiKey)
                    .header("Accept", "application/json")
                    .timeout(Duration.ofMillis(httpTimeout))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode stats = objectMapper.readTree(response.body())
                        .path("data").path("attributes").path("last_analysis_stats");
                int malicious = stats.path("malicious").asInt();
                int suspicious = stats.path("suspicious").asInt();
                return (malicious > 0 || suspicious > 0) ? "Unsafe" : "Safe";
            }
            return "Error/No Data";
        } catch (Exception e) {
            return "Error/No Data";
        }
    }

    /**
     * Get detailed VirusTotal info (e.g., engines that flagged, categories)
     */
    public String getVirusTotalDetails(String domain) {
        try {
            if (virusTotalApiKey == null || virusTotalApiKey.isBlank()) {
                return null;
            }
            String apiUrl = virusTotalApiUrl + domain;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("x-apikey", virusTotalApiKey)
                    .header("Accept", "application/json")
                    .timeout(Duration.ofMillis(httpTimeout))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) return null;
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode data = root.path("data").path("attributes");
            JsonNode engines = data.path("last_analysis_results");
            StringBuilder sb = new StringBuilder();
            if (engines.isObject()) {
                engines.fields().forEachRemaining(entry -> {
                    String engine = entry.getKey();
                    JsonNode v = entry.getValue();
                    String category = v.path("category").asText("");
                    String result = v.path("result").asText("");
                    if ("malicious".equalsIgnoreCase(category) || "suspicious".equalsIgnoreCase(category)) {
                        // Append in a compact form
                        sb.append(engine).append(':').append(category);
                        if (!result.isEmpty()) sb.append('(').append(result).append(')');
                        sb.append("; ");
                    }
                });
            }
            String summary = sb.toString().trim();
            return summary.isEmpty() ? null : summary;
        } catch (Exception e) {
            return null;
        }
    }

    private String cleanDomainName(String domain) {
        if (domain == null) return null;
        String d = domain.trim().toLowerCase();
        if (d.startsWith("http://")) d = d.substring(7);
        if (d.startsWith("https://")) d = d.substring(8);
        int slash = d.indexOf('/');
        if (slash != -1) d = d.substring(0, slash);
        return d;
    }

    /**
     * Runs a headless Selenium check to collect:
     * - whether page is reachable (navigation succeeds)
     * - page title
     * - page source length
     * - load time in ms
     * Populates respective fields in DomainResult. Best-effort: failures recorded in seleniumError.
     * Requires Chrome/Chromium with compatible driver present on PATH or managed by Selenium Manager.
     */
    public void runSeleniumCheck(DomainResult result) {
        WebDriver driver = null;
        long start = System.nanoTime();
        String url = "https://" + result.getDomain();
        try {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless=new");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--window-size=1920,1080");
            driver = new ChromeDriver(options);

            driver.navigate().to(url);

            String title = driver.getTitle();
            String pageSource = driver.getPageSource();
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;

            result.setSeleniumReachable(true);
            result.setSeleniumTitle(title);
            result.setSeleniumPageSourceLength(pageSource != null ? pageSource.length() : 0);
            result.setSeleniumLoadTimeMs(elapsedMs);
        } catch (Exception e) {
            result.setSeleniumReachable(false);
            result.setSeleniumError(e.getClass().getSimpleName() + ": " + e.getMessage());
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            result.setSeleniumLoadTimeMs(elapsedMs);
        } finally {
            if (driver != null) {
                try { driver.quit(); } catch (Exception ignored) {}
            }
        }
    }
}

