import { CheckCircle, Error, AutoStories as AutoStoriesIcon } from "@mui/icons-material";
import {
  Alert,
  AppBar,
  Avatar,
  Box,
  Button,
  CircularProgress,
  Container,
  Divider,
  Paper,
  Toolbar,
  Typography
} from "@mui/material";
import { useEffect, useState, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { API_CONFIG } from '../../config';

const Complete = () => {
  const [loading, setLoading] = useState(true);
  const [status, setStatus] = useState<string | null>(null);
  const [paymentIntentId, setPaymentIntentId] = useState('');
  const [_orderCreated, setOrderCreated] = useState(false);
  const navigate = useNavigate();
  const orderCreationAttempted = useRef(false);

  useEffect(() => {
    const queryString = window.location.search;
    const urlParams = new URLSearchParams(queryString);
    const sessionId = urlParams.get('session_id');

    if (!sessionId) {
      setLoading(false);
      return;
    }

    // Prevent duplicate calls in React Strict Mode
    if (orderCreationAttempted.current) {
      return;
    }
    orderCreationAttempted.current = true;

    // First, check the payment status
    fetch(`${API_CONFIG.API_URL}/payment/session-status?session_id=${sessionId}`)
      .then((res) => res.json())
      .then((data) => {
        setStatus(data.status);
        setPaymentIntentId(data.payment_intent_id);
        
        // If payment is complete, create the order
        if (data.status === 'complete' && data.payment_status === 'paid') {
          const token = sessionStorage.getItem('authToken');
          return fetch(`${API_CONFIG.API_URL}/payment/complete-order?session_id=${sessionId}`, {
            method: 'POST',
            headers: {
              'Authorization': `Bearer ${token}`
            }
          })
          .then((res) => res.json())
          .then((orderData) => {
            console.log('Order created:', orderData);
            setOrderCreated(true);
          })
          .catch((err) => {
            console.error('Failed to create order:', err);
            // Don't fail the whole flow if order creation fails
          });
        }
      })
      .catch(() => setStatus('error'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
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
        <Container maxWidth="sm" sx={{ mt: 8, textAlign: 'center' }}>
          <CircularProgress />
          <Typography sx={{ mt: 2 }}>Processing payment...</Typography>
        </Container>
      </Box>
    );
  }

  const isSuccess = status === 'complete';

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
      <Container maxWidth="sm" sx={{ mt: 8 }}>
      <Paper elevation={3} sx={{ p: 4, textAlign: 'center' }}>
        <Avatar
          sx={{
            width: 80,
            height: 80,
            bgcolor: isSuccess ? 'success.main' : 'error.main',
            margin: '0 auto',
            mb: 3
          }}
        >
          {isSuccess ? <CheckCircle sx={{ fontSize: 50 }} /> : <Error sx={{ fontSize: 50 }} />}
        </Avatar>

        <Typography variant="h4" gutterBottom>
          {isSuccess ? 'Payment Successful!' : 'Payment Failed'}
        </Typography>

        <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
          {isSuccess 
            ? 'Thank you for your purchase. Your order has been confirmed.'
            : 'Something went wrong with your payment. Please try again.'}
        </Typography>

        {isSuccess && (
          <Alert severity="success" sx={{ mb: 3, textAlign: 'left' }}>
            <Typography variant="body2">
              <strong>Payment ID:</strong> {paymentIntentId}
            </Typography>
          </Alert>
        )}

        <Divider sx={{ my: 3 }} />

        <Box sx={{ display: 'flex', gap: 2, justifyContent: 'center', flexWrap: 'wrap' }}>
          <Button 
            variant="contained" 
            onClick={() => navigate('/')}
          >
            Back to Store
          </Button>
          
          {!isSuccess && (
            <Button 
              variant="outlined" 
              onClick={() => navigate('/checkout')}
            >
              Try Again
            </Button>
          )}

          {isSuccess && paymentIntentId && (
            <Button 
              variant="outlined"
              href={`https://dashboard.stripe.com/payments/${paymentIntentId}`}
              target="_blank"
              rel="noopener noreferrer"
            >
              View Receipt
            </Button>
          )}
        </Box>
      </Paper>
    </Container>
    </Box>
  );
}

export default Complete;