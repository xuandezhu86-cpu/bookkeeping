package com.youran.bookkeeping.gateway.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class RouteService {

    private final RestTemplate restTemplate = new RestTemplate();

    // Route map: URL prefix -> target base URL (sorted by longest prefix first)
    private final List<Map.Entry<String, String>> routes = List.of(
        Map.entry("/api/auth",        "http://localhost:8081"),
        Map.entry("/api/avatars",     "http://localhost:8081"),
        Map.entry("/api/categories",  "http://localhost:8082"),
        Map.entry("/api/records",     "http://localhost:8083"),
        Map.entry("/api/budgets",     "http://localhost:8084"),
        Map.entry("/api/statistics",  "http://localhost:8085")
    );

    public ResponseEntity<byte[]> forward(HttpServletRequest request) {
        String path = request.getRequestURI();

        // 路径遍历防御
        if (path.contains("..")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(("Invalid path").getBytes());
        }

        String targetBase = findTargetBase(path);
        if (targetBase == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(("No route found for: " + path).getBytes());
        }

        String queryString = request.getQueryString();
        String targetUrl = targetBase + path + (queryString != null ? "?" + queryString : "");

        try {
            HttpHeaders headers = new HttpHeaders();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                if (!"host".equalsIgnoreCase(headerName)) {
                    headers.add(headerName, request.getHeader(headerName));
                }
            }

            byte[] body = request.getInputStream().readAllBytes();
            HttpEntity<byte[]> entity = new HttpEntity<>(body.length > 0 ? body : null, headers);

            HttpMethod method = HttpMethod.valueOf(request.getMethod());
            ResponseEntity<byte[]> response = restTemplate.exchange(targetUrl, method, entity, byte[].class);

            HttpHeaders responseHeaders = new HttpHeaders();
            response.getHeaders().forEach((key, values) -> {
                if (!"transfer-encoding".equalsIgnoreCase(key)) {
                    responseHeaders.addAll(key, values);
                }
            });

            return ResponseEntity.status(response.getStatusCode())
                    .headers(responseHeaders)
                    .body(response.getBody());
        } catch (Exception e) {
            // Log the error server-side without exposing details to the client
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(("Gateway error").getBytes());
        }
    }

    private String findTargetBase(String path) {
        for (Map.Entry<String, String> entry : routes) {
            if (path.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }
}
