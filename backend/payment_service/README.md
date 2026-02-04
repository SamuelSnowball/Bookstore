# Payment Service

Microservice responsible for handling Stripe payment operations for the Bookstore application.

## Overview

The Payment Service integrates with Stripe to process payments for book orders. It communicates with the Order Service to fetch cart items and create orders after successful payment.

## Features

- **Stripe Checkout Integration**: Create custom UI checkout sessions
- **Payment Status Tracking**: Query payment session status
- **Order Completion**: Create orders after payment confirmation
- **JWT Authentication**: Secured endpoints (except session status endpoint)
- **Service-to-Service Communication**: Uses RestTemplate to call Order Service APIs

## Technology Stack

- **Java 21**
- **Spring Boot 3.4.2**
- **Stripe Java SDK 28.4.0**
- **RestTemplate** for HTTP client communication
- **JUnit 5** & **Mockito** for testing

## API Endpoints

### POST /payment/create-checkout-session
Creates a Stripe Checkout Session from the authenticated user's cart.

**Authentication**: Required (JWT)

**Response**:
```json
{
  "clientSecret": "cs_test_..."
}
```

### GET /payment/session-status?session_id={sessionId}
Retrieves the status of a Checkout Session.

**Authentication**: Not required (session ID acts as secure token)

**Response**:
```json
{
  "status": "complete",
  "payment_status": "paid",
  "payment_intent_id": "pi_..."
}
```

### POST /payment/complete-order?session_id={sessionId}
Creates an order after successful payment verification.

**Authentication**: Required (JWT)

**Response**:
```json
{
  "orderId": 123,
  "message": "Order created successfully"
}
```

## Configuration

### application.properties

```properties
server.port=9003
stripe.api.key=${STRIPE_API_KEY:sk_test_YOUR_KEY_HERE}
redirect.url=${REDIRECT_URL:http://localhost:5173}
order.service.url=${ORDER_SERVICE_URL:http://localhost:9002}
```

### Environment Variables

- `STRIPE_API_KEY`: Your Stripe secret API key
- `REDIRECT_URL`: Frontend URL for payment completion redirect
- `ORDER_SERVICE_URL`: URL of the Order Service

## Running Locally

1. Set environment variables:
```bash
export STRIPE_API_KEY=sk_test_your_stripe_key
export ORDER_SERVICE_URL=http://localhost:9002
```

2. Start the service:
```bash
mvn spring-boot:run
```

3. The service will be available at `http://localhost:9003`

## Testing

Run tests:
```bash
mvn test
```

Note: Payment tests use mocked Stripe API. For integration testing, configure Stripe test mode API keys.

## Dependencies

- **Order Service**: Must be running on port 9002
- **Common Module**: Shared security and JWT utilities
- **Stripe API**: Requires valid API key

## Architecture Notes

- Uses RestTemplate to call Order Service REST endpoints
- Propagates JWT tokens to Order Service for authentication
- TODO: Consider migrating to Spring Cloud OpenFeign for better service discovery
- Payment verification is handled through Stripe session IDs

## Security

- All endpoints except `/payment/session-status` require JWT authentication
- Session status endpoint is secured via Stripe session ID (one-time use)
- JWT tokens are propagated to Order Service for authorization
