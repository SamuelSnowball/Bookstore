
# Cypress E2E Tests

End-to-end and component tests for the Bookstore application.

## Test Strategy

This test suite focuses on two primary goals:

1. **UI Component Testing** - Tests UI components in isolation by stubbing backend API calls, since backend logic is tested separately via unit tests. Live in /mocked.
2. **End-to-End Integration** - A subset of tests that verify the full application flow without stubs, ensuring all services work together correctly. Live in /real.