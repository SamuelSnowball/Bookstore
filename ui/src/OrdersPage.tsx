import {
  Add as AddIcon,
  Delete as DeleteIcon,
  Home as HomeIcon,
  AutoStories as AutoStoriesIcon,
} from "@mui/icons-material";
import {
  Alert,
  AppBar,
  Box,
  Button,
  Card,
  CardContent,
  Checkbox,
  Container,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Divider,
  FormControl,
  FormControlLabel,
  Grid,
  IconButton,
  List,
  ListItem,
  ListItemText,
  Paper,
  Radio,
  RadioGroup,
  Stack,
  TextField,
  Toolbar,
  Typography,
} from "@mui/material";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import {
  createAddress,
  deleteAddress,
  getAddresses,
  getCartItems,
  removeCartItem,
  setDefaultAddress,
  updateCartItemQuantity,
} from "./api";
import Footer from "./Footer";
import { Address, CartItem } from "./types";

function OrdersPage() {
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const [selectedAddressId, setSelectedAddressId] = useState<number | null>(null);
  const [showAddressDialog, setShowAddressDialog] = useState(false);
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);
  const [addressToDelete, setAddressToDelete] = useState<number | null>(null);
  const [newAddress, setNewAddress] = useState<Partial<Address>>({
    streetAddress: "",
    city: "",
    state: "",
    postalCode: "",
    country: "",
  });

  // Fetch cart items
  const cartQuery = useQuery({
    queryKey: ["cart"],
    queryFn: () => getCartItems(),
  });

  // Fetch addresses
  const addressesQuery = useQuery({
    queryKey: ["addresses"],
    queryFn: () => getAddresses(),
  });

  // Set default address when addresses load
  useEffect(() => {
    if (addressesQuery.data && !selectedAddressId) {
      const defaultAddr = addressesQuery.data.find((addr: Address) => addr.isDefault);
      if (defaultAddr?.id) {
        setSelectedAddressId(defaultAddr.id);
      } else if (addressesQuery.data.length > 0 && addressesQuery.data[0].id) {
        setSelectedAddressId(addressesQuery.data[0].id);
      }
    }
  }, [addressesQuery.data, selectedAddressId]);

  // Mutations
  const updateQuantityMutation = useMutation({
    mutationFn: ({ cartItemId, quantity }: { cartItemId: number; quantity: number }) =>
      updateCartItemQuantity(cartItemId, quantity),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["cart"] });
    },
  });

  const removeItemMutation = useMutation({
    mutationFn: (cartItemId: number) => removeCartItem(cartItemId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["cart"] });
    },
  });

  const createAddressMutation = useMutation({
    mutationFn: (address: Partial<Address>) => createAddress(address as any),
    onSuccess: (data) => {
      console.log("Address created successfully:", data);
      queryClient.invalidateQueries({ queryKey: ["addresses"] });
      setShowAddressDialog(false);
      setNewAddress({
        streetAddress: "",
        city: "",
        state: "",
        postalCode: "",
        country: "",
      });
      if (data?.id) {
        setSelectedAddressId(data.id);
      }
    },
    onError: (error) => {
      console.error("Failed to create address:", error);
      alert(`Failed to create address: ${error.message}`);
    },
  });

  const deleteAddressMutation = useMutation({
    mutationFn: (addressId: number) => deleteAddress(addressId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["addresses"] });
      if (selectedAddressId && addressesQuery.data) {
        const remainingAddresses = addressesQuery.data.filter(
          (addr: Address) => addr.id !== selectedAddressId
        );
        if (remainingAddresses.length > 0 && remainingAddresses[0].id) {
          setSelectedAddressId(remainingAddresses[0].id);
        } else {
          setSelectedAddressId(null);
        }
      }
    },
  });

  const setDefaultMutation = useMutation({
    mutationFn: (addressId: number) => setDefaultAddress(addressId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["addresses"] });
    },
  });

  // Handlers
  const handleQuantityChange = (cartItemId: number, newQuantity: number) => {
    if (newQuantity > 0) {
      updateQuantityMutation.mutate({ cartItemId, quantity: newQuantity });
    }
  };

  const handleRemoveItem = (cartItemId: number) => {
    removeItemMutation.mutate(cartItemId);
  };

  const handleAddAddress = () => {
    if (
      newAddress.streetAddress &&
      newAddress.city &&
      newAddress.postalCode &&
      newAddress.country
    ) {
      createAddressMutation.mutate(newAddress);
    }
  };

  const handleDeleteAddress = (addressId: number) => {
    setAddressToDelete(addressId);
    setShowDeleteDialog(true);
  };

  const confirmDeleteAddress = () => {
    if (addressToDelete) {
      deleteAddressMutation.mutate(addressToDelete);
      setShowDeleteDialog(false);
      setAddressToDelete(null);
    }
  };

  const handleSelectAddress = (addressId: number) => {
    setSelectedAddressId(addressId);
    setDefaultMutation.mutate(addressId);
  };

  const handleProceedToCheckout = () => {
    if (!selectedAddressId) {
      alert("Please select a delivery address before proceeding to checkout");
      return;
    }
    if (!cartQuery.data || cartQuery.data.length === 0) {
      alert("Your cart is empty");
      return;
    }
    navigate("/checkout");
  };

  const cartItems: CartItem[] = cartQuery.data || [];
  const addresses: Address[] = addressesQuery.data || [];

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
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

      <Container maxWidth="md" sx={{ mt: 3, mb: 0, pb: 3, flex: 1 }}>
        {/* Header */}
        <Stack direction="row" alignItems="center" spacing={2} sx={{ mb: 3 }}>
          <Typography variant="h4" component="h1">
            Review Your Order
          </Typography>
        </Stack>

      <Grid container spacing={3}>
        {/* Address Section */}
        <Grid item xs={12}>
          <Paper sx={{ p: 3, bgcolor: 'rgba(255, 255, 255, 0.95)' }}>
            <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 2 }}>
              <Typography variant="h5">Delivery Address</Typography>
              <Button
                variant="outlined"
                startIcon={<AddIcon />}
                size="small"
                onClick={() => setShowAddressDialog(true)}
              >
                Add New
              </Button>
            </Stack>
            <Divider sx={{ mb: 2 }} />

            {addresses.length === 0 ? (
              <Alert severity="warning" sx={{ mb: 2 }}>
                Please add a delivery address
              </Alert>
            ) : (
              <FormControl component="fieldset" fullWidth>
                <RadioGroup
                  value={selectedAddressId?.toString() || ""}
                  onChange={(e) => handleSelectAddress(Number(e.target.value))}
                >
                  {addresses.map((address) => (
                    <Card
                      key={address.id}
                      variant="outlined"
                      sx={{
                        mb: 2,
                        backgroundColor:
                          selectedAddressId === address.id ? "rgba(237, 242, 247, 0.95)" : "rgba(255, 255, 255, 0.95)",
                        border: selectedAddressId === address.id ? 2 : 1,
                        borderColor: selectedAddressId === address.id ? "primary.main" : "divider",
                      }}
                    >
                      <CardContent sx={{ pb: 2 }}>
                        <Stack direction="row" spacing={2} alignItems="flex-start">
                          <Radio
                            value={address.id?.toString()}
                            sx={{ mt: 0.5 }}
                          />
                          <Box sx={{ flex: 1 }}>
                            <Typography variant="body1" fontWeight="medium" gutterBottom sx={{ textAlign: "left" }}>
                              {address.streetAddress}
                            </Typography>
                            <Typography variant="body2" color="text.secondary" sx={{ textAlign: "left" }}>
                              {[address.city, address.state, address.postalCode].filter(Boolean).join(', ')}
                            </Typography>
                            <Typography variant="body2" color="text.secondary" sx={{ textAlign: "left" }}>
                              {address.country}
                            </Typography>
                            {address.isDefault && (
                              <Box
                                component="span"
                                sx={{
                                  display: "block",
                                  mt: 1,
                                  px: 1,
                                  py: 0.5,
                                  bgcolor: "primary.main",
                                  color: "primary.contrastText",
                                  borderRadius: 1,
                                  fontSize: "0.75rem",
                                  fontWeight: "medium",
                                  width: "fit-content",
                                }}
                              >
                                Default
                              </Box>
                            )}
                          </Box>
                          <IconButton
                            size="small"
                            color="error"
                            onClick={() => address.id && handleDeleteAddress(address.id)}
                          >
                            <DeleteIcon fontSize="small" />
                          </IconButton>
                        </Stack>
                      </CardContent>
                    </Card>
                  ))}
                </RadioGroup>
              </FormControl>
            )}
          </Paper>
        </Grid>

        {/* Cart Items Section */}
        <Grid item xs={12}>
          <Paper sx={{ p: 3, bgcolor: 'rgba(255, 255, 255, 0.95)' }}>
            <Typography variant="h5" gutterBottom>
              Cart Items ({cartItems.length} items)
            </Typography>
            <Divider sx={{ mb: 2 }} />

            {cartItems.length === 0 ? (
              <Box sx={{ textAlign: "center", py: 4 }}>
                <Typography variant="body1" color="text.secondary">
                  Your cart is empty
                </Typography>
                <Button
                  variant="contained"
                  startIcon={<HomeIcon />}
                  onClick={() => navigate("/")}
                  sx={{ mt: 2 }}
                >
                  Continue Shopping
                </Button>
              </Box>
            ) : (
              <List>
                {cartItems.map((item: CartItem) => (
                  <ListItem
                    key={item.cartItemId}
                    sx={{
                      mb: 2,
                      border: "1px solid",
                      borderColor: "divider",
                      borderRadius: 1,
                      flexDirection: "column",
                      alignItems: "flex-start",
                    }}
                  >
                    <Box sx={{ display: "flex", width: "100%", justifyContent: "space-between", alignItems: "flex-start" }}>
                      <ListItemText
                        primary={item.title}
                        secondary={
                          <Box component="span">
                            {item.firstName && item.lastName ? `by ${item.firstName} ${item.lastName}` : `Author ID: ${item.authorId}`}
                            {item.description && (
                              <Typography component="div" variant="body2" color="text.secondary" sx={{ mt: 0.5, fontStyle: 'italic' }}>
                                {item.description}
                              </Typography>
                            )}
                            {item.price && (
                              <Typography component="div" variant="body2" color="text.primary" sx={{ mt: 0.5 }}>
                                ${item.price.toFixed(2)} × {item.bookQuantity} = ${(item.price * item.bookQuantity).toFixed(2)}
                              </Typography>
                            )}
                          </Box>
                        }
                        sx={{ flex: 1 }}
                      />
                    <Stack direction="row" spacing={2} alignItems="center">
                      <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                        <Button
                          size="small"
                          variant="outlined"
                          onClick={() =>
                            handleQuantityChange(item.cartItemId, item.bookQuantity - 1)
                          }
                          disabled={item.bookQuantity <= 1}
                        >
                          -
                        </Button>
                        <Typography sx={{ minWidth: 30, textAlign: "center" }}>
                          {item.bookQuantity}
                        </Typography>
                        <Button
                          size="small"
                          variant="outlined"
                          onClick={() =>
                            handleQuantityChange(item.cartItemId, item.bookQuantity + 1)
                          }
                        >
                          +
                        </Button>
                      </Box>
                      <IconButton
                        edge="end"
                        aria-label="delete"
                        onClick={() => handleRemoveItem(item.cartItemId)}
                        color="error"
                      >
                        <DeleteIcon />
                      </IconButton>
                    </Stack>
                    </Box>
                  </ListItem>
                ))}
              </List>
            )}
            
            {cartItems.length > 0 && (
              <Box sx={{ mt: 2, pt: 2, borderTop: "2px solid", borderColor: "divider" }}>
                <Typography variant="h6" align="right">
                  Total: ${cartItems.reduce((sum, item) => sum + ((item.price || 0) * item.bookQuantity), 0).toFixed(2)}
                </Typography>
              </Box>
            )}
          </Paper>
        </Grid>

        {/* Checkout Button */}
        <Grid item xs={12}>
          <Button
            variant="contained"
            color="primary"
            fullWidth
            size="large"
            onClick={handleProceedToCheckout}
            disabled={!selectedAddressId || cartItems.length === 0}
          >
            Proceed to Checkout
          </Button>
        </Grid>
      </Grid>

      {/* Add Address Dialog */}
      <Dialog open={showAddressDialog} onClose={() => setShowAddressDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Add New Address</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField
              label="Street Address"
              fullWidth
              required
              value={newAddress.streetAddress}
              onChange={(e) =>
                setNewAddress({ ...newAddress, streetAddress: e.target.value })
              }
            />
            <TextField
              label="City"
              fullWidth
              required
              value={newAddress.city}
              onChange={(e) => setNewAddress({ ...newAddress, city: e.target.value })}
            />
            <TextField
              label="State/Province"
              fullWidth
              value={newAddress.state}
              onChange={(e) => setNewAddress({ ...newAddress, state: e.target.value })}
            />
            <TextField
              label="Postal Code"
              fullWidth
              required
              value={newAddress.postalCode}
              onChange={(e) =>
                setNewAddress({ ...newAddress, postalCode: e.target.value })
              }
            />
            <TextField
              label="Country"
              fullWidth
              required
              value={newAddress.country}
              onChange={(e) => setNewAddress({ ...newAddress, country: e.target.value })}
            />
            <FormControlLabel
              control={
                <Checkbox
                  checked={newAddress.isDefault || false}
                  onChange={(e) => setNewAddress({ ...newAddress, isDefault: e.target.checked })}
                />
              }
              label="Set as default address"
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setShowAddressDialog(false)}>Cancel</Button>
          <Button
            onClick={handleAddAddress}
            variant="contained"
            disabled={
              !newAddress.streetAddress ||
              !newAddress.city ||
              !newAddress.postalCode ||
              !newAddress.country
            }
          >
            Add Address
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Address Confirmation Dialog */}
      <Dialog open={showDeleteDialog} onClose={() => setShowDeleteDialog(false)} maxWidth="xs" fullWidth>
        <DialogTitle>Delete Address</DialogTitle>
        <DialogContent>
          <Typography>Are you sure you want to delete this address?</Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setShowDeleteDialog(false)}>Cancel</Button>
          <Button onClick={confirmDeleteAddress} variant="contained" color="error">
            Delete
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
    <Footer />
    </Box>
  );
}

export default OrdersPage;
