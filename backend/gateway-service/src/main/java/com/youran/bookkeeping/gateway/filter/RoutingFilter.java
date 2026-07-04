package com.youran.bookkeeping.gateway.filter;

import com.youran.bookkeeping.gateway.service.RouteService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RoutingFilter extends OncePerRequestFilter {

    private final RouteService routeService;

    public RoutingFilter(RouteService routeService) {
        this.routeService = routeService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws IOException {
        String path = request.getRequestURI();

        // Only handle /api/* paths; let others pass through (e.g., actuator, swagger)
        if (!path.startsWith("/api/")) {
            try {
                filterChain.doFilter(request, response);
            } catch (Exception e) {
                response.setStatus(500);
                response.getWriter().write("Internal error");
            }
            return;
        }

        // Handle OPTIONS preflight (CORS handled by CorsConfig)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // Forward via RouteService
        ResponseEntity<byte[]> proxyResponse = routeService.forward(request);

        // Copy status
        response.setStatus(proxyResponse.getStatusCode().value());

        // Copy response headers
        proxyResponse.getHeaders().forEach((key, values) -> {
            for (String value : values) {
                response.setHeader(key, value);
            }
        });

        // Copy response body
        if (proxyResponse.getBody() != null) {
            response.getOutputStream().write(proxyResponse.getBody());
        }
    }
}
