package com.example.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.filter.FilterFunctions.setPath;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;

/**
 * API Gateway Configuration
 * Routes external requests to internal microservices
 */
@Configuration
public class GatewayConfig {

    @Value("${services.entity-service.url:http://entity-service-entity-service.bookstore.svc.cluster.local:9001}")
    private String entityServiceUrl;

    @Value("${services.order-service.url:http://order-service-order-service.bookstore.svc.cluster.local:9002}")
    private String orderServiceUrl;

    @Value("${services.payment-service.url:http://payment-service-payment-service.bookstore.svc.cluster.local:9003}")
    private String paymentServiceUrl;

    /**
     * Route configuration for proxying requests to backend services
     */
    @Bean
    public RouterFunction<ServerResponse> gatewayRoutes() {
        return route("entity_service_routes")
                .route(request -> 
                    request.path().startsWith("/book") || 
                    request.path().startsWith("/address"), 
                    HandlerFunctions.http(entityServiceUrl))
                .build()
            .and(route("order_service_routes")
                .route(request -> 
                    request.path().startsWith("/cart") || 
                    request.path().startsWith("/orders"), 
                    HandlerFunctions.http(orderServiceUrl))
                .build())
            .and(route("payment_service_routes")
                .route(request -> 
                    request.path().startsWith("/payment"), 
                    HandlerFunctions.http(paymentServiceUrl))
                .build());
    }
}
