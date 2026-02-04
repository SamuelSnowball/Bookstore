
Cypress.Commands.add('login', (username = 'username', password = 'DemoUserPassword!$$') => {
  // We can either mock the login or do a real login after this.
  // Just use cy.intercept or not as required.
  cy.visit('/');
  cy.get('#username').type(username);
  cy.get('#password').type(password); // by ID
  cy.get('button[type="submit"]').click(); // by type
  cy.contains('Book Store');
});

// Example: Add item to cart
Cypress.Commands.add('addToCart', (bookId) => {
  cy.get(`[data-book-id="${bookId}"]`).within(() => {
    cy.contains('Add to Cart').click();
  });
});

// -- This is a child command --
// Example: Get cart count from badge
Cypress.Commands.add('getCartCount', { prevSubject: 'element' }, (subject) => {
  return cy.wrap(subject).find('.MuiBadge-badge');
});

// -- This is a dual command --
// Cypress.Commands.add('dismiss', { prevSubject: 'optional'}, (subject, options) => { ... })

// -- This will overwrite an existing command --
// Cypress.Commands.overwrite('visit', (originalFn, url, options) => { ... })

// Prevent Cypress from failing on Stripe iframe errors
Cypress.on('uncaught:exception', (err, runnable) => {
  // Prevents Cypress from failing on cross-origin iframe errors
  return false;
});
