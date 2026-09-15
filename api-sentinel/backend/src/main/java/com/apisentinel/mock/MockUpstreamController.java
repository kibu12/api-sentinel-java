package com.apisentinel.mock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/mock-upstream")
public class MockUpstreamController {

    private static final Logger log = LoggerFactory.getLogger(MockUpstreamController.class);
    private final AtomicInteger requestCount = new AtomicInteger(0);

    @RequestMapping(value = "/echo", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT})
    public ResponseEntity<Map<String, Object>> echo(
            @RequestBody(required = false) String body,
            @RequestParam Map<String, String> params,
            @RequestHeader HttpHeaders headers) {

        int count = requestCount.incrementAndGet();
        log.info("Mock Upstream /echo received request #{}", count);

        Map<String, Object> response = Map.of(
                "status", "UPSTREAM_SUCCESS",
                "message", "Hello from Mock Upstream Provider",
                "requestNumber", count,
                "timestamp", Instant.now().toString(),
                "receivedParams", params,
                "receivedBody", body != null ? body : ""
        );

        return ResponseEntity.ok(response);
    }

    @RequestMapping(value = "/delay", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Map<String, Object>> delay(
            @RequestParam(defaultValue = "2000") long ms) {

        log.info("Mock Upstream /delay sleeping for {}ms", ms);
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }

        return ResponseEntity.ok(Map.of(
                "status", "UPSTREAM_SUCCESS",
                "delayedMs", ms,
                "timestamp", Instant.now().toString()
        ));
    }

    @RequestMapping(value = "/flaky", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Map<String, Object>> flaky() {
        int count = requestCount.incrementAndGet();
        // Fails every other request to test retries
        if (count % 2 == 1) {
            log.warn("Mock Upstream /flaky triggered simulated 500 error (#{})", count);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "UPSTREAM_ERROR",
                    "error", "Simulated transient 500 server error",
                    "attempt", count
            ));
        }

        return ResponseEntity.ok(Map.of(
                "status", "UPSTREAM_RECOVERED",
                "message", "Flaky upstream succeeded on attempt #" + count
        ));
    }

    @RequestMapping(value = "/fail", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Map<String, Object>> fail() {
        log.warn("Mock Upstream /fail triggered continuous 503 service unavailable");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "status", "UPSTREAM_DOWN",
                "error", "Simulated upstream complete failure"
        ));
    }

    @RequestMapping(value = "/rate-limited", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Map<String, Object>> rateLimited() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Retry-After", "10");
        return new ResponseEntity<>(Map.of(
                "status", "UPSTREAM_RATE_LIMITED",
                "error", "Upstream 429 Too Many Requests"
        ), headers, HttpStatus.TOO_MANY_REQUESTS);
    }
}
