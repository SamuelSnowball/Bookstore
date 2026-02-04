import { useState } from "react";
import {
  PaymentElement,
  useCheckout
} from '@stripe/react-stripe-js/checkout';
import { 
  AppBar,
  Box, 
  TextField, 
  Button, 
  Typography, 
  CircularProgress, 
  Paper,
  Alert,
  Container,
  Toolbar
} from '@mui/material';
import { AutoStories as AutoStoriesIcon } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';

const validateEmail = async (email: string, checkout: any) => {
  const updateResult = await checkout.updateEmail(email);
  const isValid = updateResult.type !== "error";

  return { isValid, message: !isValid ? updateResult.error.message : null };
}

const EmailInput = ({ checkout, email, setEmail, error, setError }: { checkout: any; email: string; setEmail: (email: string) => void; error: string | null; setError: (error: string | null) => void }) => {
  const handleBlur = async () => {
    if (!email) {
      return;
    }

    const { isValid, message } = await validateEmail(email, checkout);
    if (!isValid) {
      setError(message);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setError(null);
    setEmail(e.target.value);
  };

  return (
    <TextField
      id="email"
      label="Email"
      type="email"
      value={email}
      onChange={handleChange}
      onBlur={handleBlur}
      error={!!error}
      helperText={error}
      fullWidth
      required
      sx={{ mb: 3 }}
    />
  );
};

const CheckoutForm = ({ cartTotal }: { cartTotal?: number }) => {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [emailError, setEmailError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const checkoutState = useCheckout();

  if (checkoutState.type === 'loading') {
    return (
      <Box sx={{ flexGrow: 1, minHeight: '100vh', background: 'transparent' }}>
        <AppBar position="static" sx={{ background: 'linear-gradient(90deg, #1976d2 0%, #1565c0 100%)' }}>
          <Toolbar sx={{ py: 1 }}>
            <Box 
              sx={{ flexGrow: 1, display: 'flex', alignItems: 'center', gap: 2, cursor: 'pointer' }}
              onClick={() => navigate("/")}
            >
              <Box
                sx={{
                  bgcolor: 'rgba(255, 255, 255, 0.15)',
                  borderRadius: 2,
                  p: 1,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                }}
              >
                <AutoStoriesIcon sx={{ fontSize: 32 }} />
              </Box>
              <Box>
                <Typography variant="h5" component="div" sx={{ fontWeight: 600, letterSpacing: 0.5 }}>
                  Book Store
                </Typography>
                <Typography variant="caption" sx={{ opacity: 0.9 }}>
                  Discover your next great read
                </Typography>
              </Box>
            </Box>
          </Toolbar>
        </AppBar>
        <Container maxWidth="sm">
          <Box sx={{ display: 'flex', justifyContent: 'center', mt: 2 }}>
            <CircularProgress />
          </Box>
        </Container>
      </Box>
    );
  }

  if (checkoutState.type === 'error') {
    return (
      <Box sx={{ flexGrow: 1, minHeight: '100vh', background: 'transparent' }}>
        <AppBar position="static" sx={{ background: 'linear-gradient(90deg, #1976d2 0%, #1565c0 100%)' }}>
          <Toolbar sx={{ py: 1 }}>
            <Box 
              sx={{ flexGrow: 1, display: 'flex', alignItems: 'center', gap: 2, cursor: 'pointer' }}
              onClick={() => navigate("/")}
            >
              <Box
                sx={{
                  bgcolor: 'rgba(255, 255, 255, 0.15)',
                  borderRadius: 2,
                  p: 1,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                }}
              >
                <AutoStoriesIcon sx={{ fontSize: 32 }} />
              </Box>
              <Box>
                <Typography variant="h5" component="div" sx={{ fontWeight: 600, letterSpacing: 0.5 }}>
                  Book Store
                </Typography>
                <Typography variant="caption" sx={{ opacity: 0.9 }}>
                  Discover your next great read
                </Typography>
              </Box>
            </Box>
          </Toolbar>
        </AppBar>
        <Container maxWidth="sm">
          <Alert severity="error" sx={{ mt: 2 }}>
            Error: {checkoutState.error.message}
          </Alert>
        </Container>
      </Box>
    );
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    const {checkout} = checkoutState;
    setIsSubmitting(true);

    const { isValid, message } = await validateEmail(email, checkout);
    if (!isValid) {
      setEmailError(message);
      setMessage(message);
      setIsSubmitting(false);
      return;
    }

    const confirmResult = await checkout.confirm();

    // This point will only be reached if there is an immediate error when
    // confirming the payment. Otherwise, your customer will be redirected to
    // your `return_url`. For some payment methods like iDEAL, your customer will
    // be redirected to an intermediate site first to authorize the payment, then
    // redirected to the `return_url`.
    if (confirmResult.type === 'error') {
      setMessage(confirmResult.error.message);
    }

    setIsSubmitting(false);
  };

  return (
    <Box sx={{ flexGrow: 1, minHeight: '100vh', background: 'transparent' }}>
      <AppBar position="static" sx={{ background: 'linear-gradient(90deg, #1976d2 0%, #1565c0 100%)' }}>
        <Toolbar sx={{ py: 1 }}>
          <Box 
            sx={{ flexGrow: 1, display: 'flex', alignItems: 'center', gap: 2, cursor: 'pointer' }}
            onClick={() => navigate("/")}
          >
            <Box
              sx={{
                bgcolor: 'rgba(255, 255, 255, 0.15)',
                borderRadius: 2,
                p: 1,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
            >
              <AutoStoriesIcon sx={{ fontSize: 32 }} />
            </Box>
            <Box>
              <Typography variant="h5" component="div" sx={{ fontWeight: 600, letterSpacing: 0.5 }}>
                Book Store
              </Typography>
              <Typography variant="caption" sx={{ opacity: 0.9 }}>
                Discover your next great read
              </Typography>
            </Box>
          </Box>
        </Toolbar>
      </AppBar>
      <Container maxWidth="sm">
      <Paper elevation={3} sx={{ p: 4, mt: 2 }}>
        <Typography variant="h5" component="h1" gutterBottom sx={{ mb: 3 }}>
          Checkout
        </Typography>
        
        <form onSubmit={handleSubmit}>
          <EmailInput
            checkout={checkoutState.checkout}
            email={email}
            setEmail={setEmail}
            error={emailError}
            setError={setEmailError}
          />
          
          <Typography variant="h6" component="h2" gutterBottom sx={{ mb: 2 }}>
            Payment
          </Typography>
          
          <Alert severity="info" sx={{ mb: 3 }}>
            <Typography variant="body2" sx={{ fontWeight: 600, mb: 0.5 }}>
              Test Mode
            </Typography>
            <Typography variant="body2">
              Select <strong>Card</strong> as payment method, then use card number <strong>4242 4242 4242 4242</strong> with any future expiration date and any 3-digit security code. This is a test card recognized by Stripe, so you can complete the payment without actually paying anything.
            </Typography>
          </Alert>
          
          <Box sx={{ mb: 3 }}>
            <PaymentElement id="payment-element" />
          </Box>
          
          <Button 
            type="submit"
            variant="contained" 
            color="primary" 
            fullWidth
            size="large"
            disabled={isSubmitting}
            sx={{ mt: 2 }}
          >
            {isSubmitting ? (
              <CircularProgress size={24} color="inherit" />
            ) : (
              cartTotal ? `Pay $${cartTotal.toFixed(2)} now` : 'Pay now'
            )}
          </Button>
          
          {message && (
            <Alert severity="error" sx={{ mt: 2 }}>
              {message}
            </Alert>
          )}
        </form>
      </Paper>
    </Container>
    </Box>
  );
}

export default CheckoutForm;