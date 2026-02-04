describe("Book Store", () => {
  beforeEach(() => {

    // This file needs fixing, I've left it broken for now
    // This file needs fixing, I've left it broken for now
    // This file needs fixing, I've left it broken for now
    // This file needs fixing, I've left it broken for now
    // This file needs fixing, I've left it broken for now
    
    // real backend login - cy.login();


    // Reset user cart before each test
    // TOOD, proper e2e tests - need a reset method, actually no I dont I can just login with a random user
    // which will get created and have an empty cart. cy.request("POST", "/api/test/reset-cart");
  });

  it("should be able to add items to cart and checkout successfully", () => {
    // Cart should be initially empty
    cy.get('[aria-label="cart count icon"]').within(() => {
      cy.get(".MuiBadge-badge").should("have.text", "");
    });

    // Add first book to cart, get by ID
    cy.get('[data-book-id="1"]').within(() => {
      cy.contains("Add to Cart").click();
    });

    cy.get('[aria-label="cart count icon"]').within(() => {
      cy.get(".MuiBadge-badge").should("have.text", "1");
    });

    // Click checkout button
    cy.get('[aria-label="cart count icon"]').click();

    // Get the drawer, then search within it
    // Use .last() to get the cart drawer (the most recently opened one)
    cy.get('[role="presentation"]')
      .last()
      .within(() => {
        cy.contains("Shopping Cart");
        cy.contains("The Forgotten Chronicles");
        cy.contains("Go to Checkout").click();
      });

    // Should be redirected to checkout page
    cy.url().should("include", "/checkout");

    // Fill out Stripe email field (this is outside the iframe)
    cy.get('#email').type("test@example.com");

    // Wait for Stripe Payment Element to load
    cy.wait(3000);

    // Card is usually selected by default, but if you need to click it:
    // Try to find and click "Card" tab - this might be outside iframe or use different selector
    cy.get('body').then($body => {
      // Check if Card selector exists and click it (optional - Card is often default)
      if ($body.find('.p-TabLabel:contains("Card")').length > 0) {
        cy.contains('.p-TabLabel', 'Card').click();
      }
    });

    cy.wait(1000);

    // Find and fill the card number iframe
    cy.get('iframe[title*="Secure card number"]').then(($iframe) => {
      const iframeBody = $iframe.contents().find('body');
      cy.wrap(iframeBody).find('input[name="number"]').type('4242424242424242', { delay: 50 });
    });

    // Find and fill the expiry date iframe
    cy.get('iframe[title*="Secure expiration date"]').then(($iframe) => {
      const iframeBody = $iframe.contents().find('body');
      cy.wrap(iframeBody).find('input[name="expiry"]').type('1234', { delay: 50 });
    });

    // Find and fill the CVC iframe
    cy.get('iframe[title*="Secure CVC"]').then(($iframe) => {
      const iframeBody = $iframe.contents().find('body');
      cy.wrap(iframeBody).find('input[name="cvc"]').type('123', { delay: 50 });
    });

    // Submit payment button is outside iframe
    cy.contains('button', 'Pay').click();

    // Verify payment success
    cy.contains("Payment Successful", { timeout: 10000 });

    // Verify cart is now empty again
    cy.get('[aria-label="cart count icon"]').within(() => {
      cy.get(".MuiBadge-badge").should("have.text", "");
    });

  });

  it("should be able to view the cart", () => {
    cy.get('[aria-label="cart count icon"]').within(() => {
      cy.get(".MuiBadge-badge").should("have.text", "3");
    });

    cy.get('[aria-label="cart count icon"]').click();

    // Get the drawer, then search within it
    // Use .last() to get the cart drawer (the most recently opened one)
    cy.get('[role="presentation"]')
      .last()
      .within(() => {
        cy.contains("Shopping Cart");
        cy.contains("The Forgotten Chronicles");
        cy.contains("The Cursed Mystery");
        cy.contains("The Final Ocean of City");
        cy.contains("The Great Desert").should("not.exist"); // Book not in cart
      });
  });

  it("should be able to search the books list", () => {
    // Doesn't use getBooks stub, loads from backend

    // Type into search box
    cy.get('input[placeholder="Search by title..."]').type("City");

    // Assert there is only 1 book
    cy.get("[data-book-id]").should("have.length", 1);
    cy.contains("The Final Ocean of City");
  });
});
