import {
  CheckoutProvider
} from '@stripe/react-stripe-js/checkout';
import { loadStripe } from '@stripe/stripe-js';
import { useEffect, useRef, useState } from "react";
import {
  Route,
  BrowserRouter as Router,
  Routes,
} from "react-router-dom";
import { createCheckoutSession, createOrderFromCart, getCartItems } from './api';
import CheckoutForm from './components/stripe/CheckoutForm';
import Complete from './components/stripe/Complete';
import { CartItem } from "./types";

import "./App.css";
import BookStore from "./BookStore";
import OrderHistory from "./OrderHistory";
import OrdersPage from './OrdersPage';

const stripePromise = loadStripe("pk_test_51SuGK8AyhBVq0J7lxPy8zpTrkkBCCLiI3hqeUfHH71fwK72LDy9IydpiZtW0S0eonX2k3u5NwBjcO3kdK0m8dFyd007m8cUrLz");

const App = () => {
  return (
    <div className="App">
      <Router>
        <Routes>
          <Route path="/" element={<BookStore />} />
          <Route path="/orders" element={<OrdersPage />} />
          <Route path="/orders-history" element={<OrderHistory />} />
          <Route path="/checkout" element={<CheckoutWrapper />} />
          <Route path="/complete" element={<Complete />} />
        </Routes>
      </Router>
    </div>
  )
}

/*
CheckoutWrapper created so that we don't call to create a checkout session when first load the app.
*/
const CheckoutWrapper = () => {
  const [clientSecret, setClientSecret] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [cartTotal, setCartTotal] = useState<number>(0);
  const sessionCreationAttempted = useRef(false);

  useEffect(() => {
    const token = sessionStorage.getItem('authToken');
    
    if (!token) {
      setError('Not authenticated');
      return;
    }

    // Prevent duplicate calls in React Strict Mode
    if (sessionCreationAttempted.current) {
      return;
    }
    sessionCreationAttempted.current = true;
    
    // Flow: Get cart -> Create order -> Create checkout session
    getCartItems()
      .then((cartItems: CartItem[]) => {
        if (!cartItems || cartItems.length === 0) {
          throw new Error('Cart is empty');
        }

        const total = cartItems.reduce((sum: number, item: CartItem) => 
          sum + ((item.price || 0) * item.bookQuantity), 0
        );
        setCartTotal(total);

        // Create order from cart
        return createOrderFromCart().then((orderId: number) => ({
          cartItems,
          orderId,
          total
        }));
      })
      .then(({ cartItems, orderId, total }) => {
        // Build payment request
        const paymentRequest = {
          userId: 0, // Will be extracted from JWT on backend
          orderId: orderId,
          totalAmount: total,
          items: cartItems.map((item: CartItem) => ({
            bookId: item.bookId,
            title: item.title,
            price: item.price || 0,
            quantity: item.bookQuantity
          }))
        };

        console.log('Payment request being sent:', JSON.stringify(paymentRequest, null, 2));
        
        // Create checkout session
        return createCheckoutSession(paymentRequest);
      })
      .then((data) => {
        setClientSecret(data.clientSecret);
      })
      .catch((err) => {
        console.error('Checkout creation error:', err);
        setError(err.message);
      });
  }, []);

  if (error) {
    return <div>Error creating checkout session: {error}</div>;
  }

  if (!clientSecret) {
    return <div>Loading checkout...</div>;
  }

  const appearance = {
    theme: 'stripe' as const,
  };

  return (
    <CheckoutProvider
      stripe={stripePromise}
      options={{
        clientSecret,
        elementsOptions: { appearance },
      }}
    >
      <CheckoutForm cartTotal={cartTotal} />
    </CheckoutProvider>
  );
};

export default App;