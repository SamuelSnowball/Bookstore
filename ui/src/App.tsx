import {
  CheckoutProvider
} from '@stripe/react-stripe-js/checkout';
import { loadStripe } from '@stripe/stripe-js';
import { useEffect, useState, useRef } from "react";
import {
  Route,
  BrowserRouter as Router,
  Routes,
} from "react-router-dom";
import { CartItem } from "./types";
import CheckoutForm from './components/stripe/CheckoutForm';
import Complete from './components/stripe/Complete';
import { getCartItems } from './api';
import { API_CONFIG } from './config';

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
    
    // Fetch cart items to calculate total
    getCartItems()
      .then((items) => {
        const total = items.reduce((sum: number, item: CartItem) => sum + ((item.price || 0) * item.bookQuantity), 0);
        setCartTotal(total);
      })
      .catch((err) => console.error('Failed to fetch cart:', err));
    
    // Create checkout session when component mounts
    fetch(`${API_CONFIG.API_URL}/payment/create-checkout-session`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
      },
      // No body required as we get cart on the server side
    })
      .then((res) => res.json())
      .then((data) => setClientSecret(data.clientSecret))
      .catch((err) => setError(err.message));
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