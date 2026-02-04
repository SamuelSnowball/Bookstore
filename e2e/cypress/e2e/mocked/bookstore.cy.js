describe("Mocked login", () => {
  // Login and gets books
  beforeEach(() => {
    // Creds used to login
    // testusername
    // TestUserPassword!$$
    cy.intercept("POST", "http://localhost:9000/api/auth/login", {
      fixture: "login-success.json",
    }).as("loginRequest"); // login-success.json becomes the response body

    // Get books request
    cy.intercept("GET", "http://localhost:9001/book?prevPageLastBookId=0", {
      fixture: "books.json",
    }).as("getBooksRequest");

    cy.login();

    cy.wait("@loginRequest");
    cy.wait("@getBooksRequest");
  });

  it("should successfully login with mocked response and display book list", () => {
    // Should be redirected to main app
    cy.contains("The Forgotten Chronicles");
  });

  it("should have empty cart on login", () => {
    cy.get('[aria-label="cart count icon"]').within(() => {
      cy.get(".MuiBadge-badge").should("have.text", "");
    });
  });

  it("should be able to add to cart and go to checkout", () => {
    // Should make request to backend to add to cart
    cy.intercept("POST", "http://localhost:9002/cart", { statusCode: 200 }).as(
      "addToCartRequest",
    );
    // Should make request to get updated cart
    cy.intercept("GET", "http://localhost:9002/cart", {
      fixture: "get-cart.json",
    }).as("getCartRequest");

    // Add first book to cart, get by ID
    cy.get('[data-book-id="1"]').within(() => {
      cy.contains("Add to Cart").click();
    });

    cy.wait("@addToCartRequest");
    cy.wait("@getCartRequest");

    cy.get('[aria-label="cart count icon"]').within(() => {
      cy.get(".MuiBadge-badge").should("have.text", "1");
    });

    // Click cart button
    cy.get('[aria-label="cart count icon"]').click();

    // Get the drawer, then search within it
    // Use .last() to get the cart drawer (the most recently opened one)
    cy.get('[role="presentation"]')
      .last()
      .within(() => {
        cy.contains("Shopping Cart");
        cy.contains("The Forgotten Chronicles");
        cy.contains("Review Order").click();
      });

    // Should be redirected to order review page
    cy.url().should("include", "/orders");
  });

  // Order review todo - updating address etc
  it("should be able to review order make a payment", () => {
    cy.intercept("GET", "http://localhost:9001/address", {
      fixture: "get-address.json",
    }).as("getAddressRequest");

    cy.intercept("GET", "http://localhost:9002/cart", {
      fixture: "get-cart.json",
    }).as("getCartRequest");

    cy.visit("/orders");
    cy.wait("@getAddressRequest");
    cy.wait("@getCartRequest");
    cy.contains("The Forgotten Chronicles");

    cy.contains("Proceed to Checkout").click();
    cy.url().should("include", "/checkout");
  });
  

  it("should be able to make a payment", () => {
    cy.visit("/checkout");
     // TODO: Mock payment request and test payment flow
  });

});
