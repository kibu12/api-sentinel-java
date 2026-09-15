package com.apisentinel.gateway;

import com.apisentinel.exception.UpstreamTimeoutException;
import com.apisentinel.exception.UpstreamUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.Map;

@Component
public class UpstreamClient {

    private static final Logger log = LoggerFactory.getLogger(UpstreamClient.class);

    private final HttpClient httpClient;

    public UpstreamClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public record UpstreamResult(int statusCode, String body, long latencyMs) {}

    public UpstreamResult forward(
            String fullUrl,
            String method,
            Map<String, String> headers,
            String body,
            int timeoutMs) {

        long startTime = System.currentTimeMillis();
        String httpMethod = (method != null && !method.isBlank()) ? method.toUpperCase() : "GET";

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .timeout(Duration.ofMillis(Math.max(100, timeoutMs)));

        if (headers != null) {
            headers.forEach((k, v) -> {
                if (!k.equalsIgnoreCase("host") && !k.equalsIgnoreCase("content-length")) {
                    try {
                        builder.header(k, v);
                    } catch (Exception ignored) {}
                }
            });
        }

        if ("POST".equals(httpMethod) || "PUT".equals(httpMethod) || "PATCH".equals(httpMethod)) {
            builder.method(httpMethod, HttpRequest.BodyPublishers.ofString(body != null ? body : ""));
            if (headers == null || !headers.containsKey("Content-Type")) {
                builder.header("Content-Type", "application/json");
            }
        } else {
            builder.method(httpMethod, HttpRequest.BodyPublishers.noBody());
        }

        try {
            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            long latency = System.currentTimeMillis() - startTime;
            return new UpstreamResult(response.statusCode(), response.body(), latency);
        } catch (HttpTimeoutException e) {
            long latency = System.currentTimeMillis() - startTime;
            log.warn("Upstream call to [{}] timed out after {}ms", fullUrl, latency);
            throw new UpstreamTimeoutException("Upstream call timed out after " + timeoutMs + "ms");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new UpstreamUnavailableException("Upstream request thread interrupted");
        } catch (Exception e) {
            long latency = System.currentTimeMillis() - startTime;
            log.error("Upstream call to [{}] failed after {}ms: {}", fullUrl, latency, e.getMessage());
            throw new UpstreamUnavailableException("Failed to connect to upstream service: " + e.getMessage());
        }
    }
}
