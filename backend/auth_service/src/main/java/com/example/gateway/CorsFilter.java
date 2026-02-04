package com.example.gateway;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * CORS filter that runs BEFORE Spring Security
 * Handles OPTIONS preflight requests for API Gateway
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(CorsFilter.class);

    @Value("${cors.allowed.origins:http://localhost:5173}")
    private String allowedOrigins;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;
        
        String origin = request.getHeader("Origin");
        String method = request.getMethod();
        
        logger.debug("CORS Filter - Method: {}, Origin: {}, Path: {}", method, origin, request.getRequestURI());
        logger.debug("CORS Filter - Checking origin '{}' against allowed: {}", origin, allowedOrigins);
        
        // Add CORS headers if origin is allowed
        if (origin != null && isAllowedOrigin(origin)) {
            logger.debug("CORS Filter - Adding headers for allowed origin: {}", origin);
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "*");
            response.setHeader("Access-Control-Max-Age", "3600");
        } else if (origin != null) {
            logger.warn("CORS Filter - Origin not allowed: {}", origin);
        }
        
        // Handle preflight OPTIONS request
        if ("OPTIONS".equalsIgnoreCase(method)) {
            logger.debug("CORS Filter - Handling OPTIONS preflight, returning 200");
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        
        chain.doFilter(req, res);
    }
    
    private boolean isAllowedOrigin(String origin) {
        List<String> allowed = Arrays.asList(allowedOrigins.split(","));
        return allowed.contains(origin);
    }
}
