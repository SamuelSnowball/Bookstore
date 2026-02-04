import { Logout as LogoutIcon, ShoppingCart as ShoppingCartIcon, AutoStories as AutoStoriesIcon, Receipt as ReceiptIcon } from "@mui/icons-material";
import {
  AppBar,
  Badge,
  Box,
  Button,
  Container,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Divider,
  Drawer,
  IconButton,
  List,
  Snackbar,
  Alert,
  Stack,
  TextField,
  Toolbar,
  Tooltip,
  Typography
} from "@mui/material";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { getBooks, getCartItems, addToCart, removeCartItem } from "./api";
import "./App.css";
import BookPanel from "./BookPanel";
import CartItem from "./CartItem";
import Login from "./Login";
import Footer from "./Footer";
import { Book, CartItem as CartItemType } from "./types";

function BookStore() {
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const [isAuthenticated, setIsAuthenticated] = useState(() => {
    return !!sessionStorage.getItem('authToken');
  });
  const [username, setUsername] = useState(() => {
    return sessionStorage.getItem('username') || 'User';
  });

  // State to keep track of current page (0-based offset)
  const [pageOffset, setPageOffset] = useState(0);
  const [searchQuery, setSearchQuery] = useState("");
  const [snackbarOpen, setSnackbarOpen] = useState(false);
  const [snackbarMessage, setSnackbarMessage] = useState("");
  const [showLogoutDialog, setShowLogoutDialog] = useState(false);
  
  const getBooksQuery = useQuery({
    queryKey: ["books", pageOffset],
    queryFn: () => getBooks(pageOffset),
    enabled: isAuthenticated
  });

  const [showCart, setShowCart] = useState(false);

  const cartQuery = useQuery({
    queryKey: ["cart"],
    queryFn: () => getCartItems(),
    enabled: isAuthenticated
  });

  // Make into one useMutation, updateCartMutation
  const addToCartMutation = useMutation({
    mutationFn: (bookId: number) => addToCart(bookId, 1),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["cart"] });
      setSnackbarMessage("Item added to cart");
      setSnackbarOpen(true);
    }
  });

  const removeFromCartMutation = useMutation({
    mutationFn: (cartItemId: number) => removeCartItem(cartItemId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["cart"] });
    }
  });

  console.log("Books data:", getBooksQuery.data?.length ? getBooksQuery.data : "No data");

  const handleNextPage = () => {
    setPageOffset(prev => prev + 10);
  };

  const handlePreviousPage = () => {
    setPageOffset(prev => Math.max(0, prev - 10));
  };

  const handleLoginSuccess = (_token: string, _userId: number) => {
    const storedUsername = sessionStorage.getItem('username') || 'User';
    setUsername(storedUsername);
    setIsAuthenticated(true);
  };

  const handleLogout = () => {
    setShowLogoutDialog(true);
  };

  const confirmLogout = () => {
    sessionStorage.removeItem('authToken');
    sessionStorage.removeItem('userId');
    sessionStorage.removeItem('username');
    setIsAuthenticated(false);
    setUsername('User');
    queryClient.clear();
    setShowLogoutDialog(false);
  };

  const handleAddToCart = (bookId: number) => {
    addToCartMutation.mutate(bookId);
  };

  const handleRemoveFromCart = (cartItemId: number) => {
    removeFromCartMutation.mutate(cartItemId);
  };

  const handleCartOpen = () => {
    setShowCart(!showCart);
    // For now render this but eventually navigate to diff page
  };

  const cartItemCount = cartQuery.data?.reduce((sum: number, item: CartItemType) => sum + item.bookQuantity, 0) || 0;

  if (!isAuthenticated) {
    return <Login onLoginSuccess={handleLoginSuccess} />;
  }



  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <AppBar position="static" sx={{ background: 'linear-gradient(90deg, #1976d2 0%, #1565c0 100%)' }}>
        <Toolbar sx={{ py: 1 }}>
          <Box sx={{ flexGrow: 1, display: 'flex', alignItems: 'center', gap: 2 }}>
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
          <Typography variant="body1" sx={{ mr: 2, fontWeight: 500 }}>
            {username}
          </Typography>
          <Tooltip title="View Order History">
            <IconButton 
              color="inherit" 
              onClick={() => navigate('/orders-history')} 
              aria-label="order history"
            >
              <ReceiptIcon />
            </IconButton>
          </Tooltip>
          <Tooltip title="Shopping Cart">
            <IconButton color="inherit" onClick={handleCartOpen} aria-label="cart count icon">
              <Badge badgeContent={cartItemCount} color="error">
                <ShoppingCartIcon />
              </Badge>
            </IconButton>
          </Tooltip>
          <Tooltip title="Logout">
            <IconButton color="inherit" onClick={handleLogout}>
              <LogoutIcon />
            </IconButton>
          </Tooltip>
        </Toolbar>
      </AppBar>

      <Container maxWidth="md" sx={{ mt: 3, mb: 0, pb: 3 }}>
        <TextField
          fullWidth
          label="Search books"
          variant="outlined"
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          placeholder="Search by title..."
          sx={{ mb: 3 }}
        />
        
        {
          getBooksQuery.data
            ?.filter((book: Book) => 
              book.title?.toLowerCase().includes(searchQuery.toLowerCase())
            )
            .map((book: Book) => (
              <BookPanel key={book.id} {...book} onAddToCart={handleAddToCart} />
            ))
        }
        
        <Stack spacing={2} alignItems="center" sx={{ mt: 3, mb: 2 }} direction="row">
          <Button 
            variant="contained"
            onClick={handlePreviousPage}
            disabled={getBooksQuery.isLoading || pageOffset === 0}
          >
            Previous
          </Button>
          <Button 
            variant="contained"
            onClick={handleNextPage}
            disabled={getBooksQuery.isLoading || !getBooksQuery.data?.length}
          >
            Next
          </Button>
        </Stack>
      </Container>

      <Drawer anchor="right" open={showCart} onClose={() => setShowCart(false)}>
        <Box sx={{ width: 400 }} role="presentation">
          <Box sx={{ p: 2 }}>
            <Typography variant="h5" component="div">
              Shopping Cart
            </Typography>
          </Box>
          <Divider />
          <List>
            {
              cartQuery.data?.length ? (
                cartQuery.data.map((item: CartItemType) => (
                  <CartItem key={item.cartItemId} {...item} handleRemoveFromCart={handleRemoveFromCart} />
                ))
              ) : (
                <Box sx={{ p: 3, textAlign: 'center' }}>
                  <Typography variant="body1" color="text.secondary">
                    Your cart is empty
                  </Typography>
                </Box>
              )
            }
          </List>
          {cartQuery.data?.length > 0 && (
            <Box sx={{ p: 2 }}>
              <Divider sx={{ mb: 2 }} />
              <Button 
                variant="contained" 
                color="primary" 
                fullWidth
                onClick={() => navigate('/orders')}
              >
                Review Order
              </Button>
            </Box>
          )}
        </Box>
      </Drawer>

      <Snackbar 
        open={snackbarOpen} 
        autoHideDuration={3000} 
        onClose={() => setSnackbarOpen(false)}
        anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
        // Added top margin to the Snackbar so it appears properly below the AppBar/bookstore title instead of overlapping with it.
        sx={{ mt: 11 }}
      >
        <Alert 
          onClose={() => setSnackbarOpen(false)} 
          severity="success" 
          variant="filled"
          sx={{ width: '100%' }}
        >
          {snackbarMessage}
        </Alert>
      </Snackbar>

      <Dialog
        open={showLogoutDialog}
        onClose={() => setShowLogoutDialog(false)}
      >
        <DialogTitle>Confirm Logout</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Are you sure you want to log out?
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setShowLogoutDialog(false)} color="primary">
            Cancel
          </Button>
          <Button onClick={confirmLogout} color="primary" variant="contained">
            Logout
          </Button>
        </DialogActions>
      </Dialog>
      <Footer />
    </Box>
  );
}

export default BookStore;