import {
  AppBar,
  Badge,
  Box,
  Card,
  CardContent,
  Container,
  Typography,
  CircularProgress,
  Alert,
  Chip,
  Divider,
  IconButton,
  List,
  ListItem,
  ListItemText,
  Toolbar,
  Tooltip,
} from "@mui/material";
import { 
  AutoStories as AutoStoriesIcon, 
  Logout as LogoutIcon, 
  ShoppingCart as ShoppingCartIcon,
  Receipt as ReceiptIcon 
} from "@mui/icons-material";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { getOrders, getCartItems } from "./api";
import { useNavigate } from "react-router-dom";
import Footer from "./Footer";

interface OrderBook {
  bookId: number;
  title: string;
  quantity: number;
  price: number;
}

interface Order {
  orderId: number;
  orderDate: string;
  totalAmount: number;
  status: string;
  books: OrderBook[];
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString);
  return date.toLocaleDateString("en-US", { 
    year: "numeric", 
    month: "long", 
    day: "numeric" 
  });
};

export default function OrderHistory() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const ordersQuery = useQuery({
    queryKey: ["orders"],
    queryFn: getOrders,
  });

  const cartQuery = useQuery({
    queryKey: ["cart"],
    queryFn: () => getCartItems(),
  });

  const handleLogout = () => {
    sessionStorage.removeItem('authToken');
    sessionStorage.removeItem('userId');
    sessionStorage.removeItem('username');
    queryClient.clear();
    navigate('/');
  };

  const cartItemCount = cartQuery.data?.length || 0;

  if (ordersQuery.isLoading) {
    return (
      <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
        <AppBar position="static" sx={{ background: 'linear-gradient(90deg, #1976d2 0%, #1565c0 100%)' }}>
          <Toolbar sx={{ py: 1 }}>
            <Box 
              sx={{ flexGrow: 1, display: 'flex', alignItems: 'center', gap: 2, cursor: 'pointer' }}
              onClick={() => navigate('/')}
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
        <Container maxWidth="md" sx={{ mt: 4, display: "flex", justifyContent: "center", flex: 1 }}>
          <CircularProgress />
        </Container>
        <Footer />
      </Box>
    );
  }

  if (ordersQuery.isError) {
    return (
      <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
        <AppBar position="static" sx={{ background: 'linear-gradient(90deg, #1976d2 0%, #1565c0 100%)' }}>
          <Toolbar sx={{ py: 1 }}>
            <Box 
              sx={{ flexGrow: 1, display: 'flex', alignItems: 'center', gap: 2, cursor: 'pointer' }}
              onClick={() => navigate('/')}
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
        <Container maxWidth="md" sx={{ mt: 4, flex: 1 }}>
          <Alert severity="error">Failed to load orders. Please try again later.</Alert>
        </Container>
        <Footer />
      </Box>
    );
  }

  const orders = ordersQuery.data || [];

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <AppBar position="static" sx={{ background: 'linear-gradient(90deg, #1976d2 0%, #1565c0 100%)' }}>
        <Toolbar sx={{ py: 1 }}>
          <Box 
            sx={{ flexGrow: 1, display: 'flex', alignItems: 'center', gap: 2, cursor: 'pointer' }}
            onClick={() => navigate('/')}
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
            <IconButton color="inherit" onClick={() => navigate('/orders')} aria-label="cart count icon">
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

      <Container maxWidth="md" sx={{ mt: 3, mb: 4 }}>
        {orders.length === 0 ? (
          <Alert severity="info">You haven't placed any orders yet.</Alert>
        ) : (
          <>
            <Typography variant="h4" component="h1" gutterBottom sx={{ mb: 3 }}>
              Order History
            </Typography>

      {orders.map((order: Order) => (
        <Card key={order.orderId} sx={{ mb: 3 }}>
          <CardContent>
            <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2 }}>
              <Typography variant="h6" component="h2">
                Order #{order.orderId}
              </Typography>
              <Chip 
                label={order.status || "Completed"} 
                color="success" 
                size="small" 
              />
            </Box>

            <Typography variant="body2" color="text.secondary" gutterBottom>
              Order Date: {order.orderDate ? formatDate(order.orderDate) : "N/A"}
            </Typography>

            <Divider sx={{ my: 2 }} />

            <Typography variant="subtitle2" gutterBottom sx={{ fontWeight: 600 }}>
              Items:
            </Typography>

            <List dense>
              {order.books?.map((book: OrderBook, index: number) => (
                <ListItem key={index} disableGutters>
                  <ListItemText
                    primary={book.title}
                    secondary={`Quantity: ${book.quantity} × $${book.price.toFixed(2)}`}
                  />
                  <Typography variant="body2" sx={{ fontWeight: 600 }}>
                    ${(book.quantity * book.price).toFixed(2)}
                  </Typography>
                </ListItem>
              ))}
            </List>

            <Divider sx={{ my: 2 }} />

            <Box sx={{ display: "flex", justifyContent: "flex-end" }}>
              <Typography variant="h6" component="div" sx={{ fontWeight: 600 }}>
                Total: ${order.totalAmount.toFixed(2)}
              </Typography>
            </Box>
          </CardContent>
        </Card>
      ))}
          </>
        )}
      </Container>
      <Footer />
    </Box>
  );
}
