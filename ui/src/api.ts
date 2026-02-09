import { API_CONFIG } from './config';

export function getBooks(page: number = 0){
   // Don't send token - /book is public
   return fetch(`${API_CONFIG.API_URL}/book?prevPageLastBookId=${page}`)
      .then((res) => res.json());
}

export function getCartItems(){
   const token = sessionStorage.getItem('authToken');
   return fetch(`${API_CONFIG.API_URL}/cart`, {
      headers: {
         'Authorization': `Bearer ${token}`
      }
   }).then((res) => res.json());
}

// Add item to cart (used when adding books from the store)
export function addToCart(bookId: number, quantity: number = 1){
   const token = sessionStorage.getItem('authToken');
   return fetch(`${API_CONFIG.API_URL}/cart`, {
      method: 'POST',
      headers: {
         'Authorization': `Bearer ${token}`,
         'Content-Type': 'application/json'
      },
      body: JSON.stringify({ bookId, quantity })
   });
}

// Update cart item quantity (used in orders page)
export function updateCartItemQuantity(cartItemId: number, quantity: number){
   const token = sessionStorage.getItem('authToken');
   return fetch(`${API_CONFIG.API_URL}/cart/${cartItemId}`, {
      method: 'PUT',
      headers: {
         'Authorization': `Bearer ${token}`,
         'Content-Type': 'application/json'
      },
      body: JSON.stringify({ quantity })
   });
}

export function removeCartItem(cartItemId: number){
   const token = sessionStorage.getItem('authToken');
   return fetch(`${API_CONFIG.API_URL}/cart/${cartItemId}`, {
      method: 'DELETE',
      headers: {
         'Authorization': `Bearer ${token}`
      }
   });
}

// Address API functions
export function getAddresses(){
   const token = sessionStorage.getItem('authToken');
   return fetch(`${API_CONFIG.API_URL}/address`, {
      headers: {
         'Authorization': `Bearer ${token}`
      }
   }).then((res) => res.json());
}

export function getDefaultAddress(){
   const token = sessionStorage.getItem('authToken');
   return fetch(`${API_CONFIG.API_URL}/address/default`, {
      headers: {
         'Authorization': `Bearer ${token}`
      }
   }).then((res) => {
      if (!res.ok) {
         if (res.status === 404) {
            return null;
         }
         throw new Error('Failed to fetch default address');
      }
      return res.json();
   });
}

export function createAddress(address: {
   streetAddress: string;
   city: string;
   state?: string;
   postalCode: string;
   country: string;
   isDefault?: boolean;
}){
   const token = sessionStorage.getItem('authToken');
   return fetch(`${API_CONFIG.API_URL}/address`, {
      method: 'POST',
      headers: {
         'Authorization': `Bearer ${token}`,
         'Content-Type': 'application/json'
      },
      body: JSON.stringify(address)
   }).then((res) => {
      if (!res.ok) {
         throw new Error(`Failed to create address: ${res.statusText}`);
      }
      return res.json();
   });
}

export function updateAddress(addressId: number, address: {
   streetAddress: string;
   city: string;
   state?: string;
   postalCode: string;
   country: string;
   isDefault?: boolean;
}){
   const token = sessionStorage.getItem('authToken');
   return fetch(`${API_CONFIG.API_URL}/address/${addressId}`, {
      method: 'PUT',
      headers: {
         'Authorization': `Bearer ${token}`,
         'Content-Type': 'application/json'
      },
      body: JSON.stringify(address)
   });
}

export function deleteAddress(addressId: number){
   const token = sessionStorage.getItem('authToken');
   return fetch(`${API_CONFIG.API_URL}/address/${addressId}`, {
      method: 'DELETE',
      headers: {
         'Authorization': `Bearer ${token}`
      }
   });
}

export function setDefaultAddress(addressId: number){
   const token = sessionStorage.getItem('authToken');
   return fetch(`${API_CONFIG.API_URL}/address/${addressId}/set-default`, {
      method: 'PATCH',
      headers: {
         'Authorization': `Bearer ${token}`
      }
   });
}

export function getOrders(){
   const token = sessionStorage.getItem('authToken');
   return fetch(`${API_CONFIG.API_URL}/orders`, {
      headers: {
         'Authorization': `Bearer ${token}`
      }
   }).then((res) => res.json());
}

export function createOrderFromCart(){
   const token = sessionStorage.getItem('authToken');
   return fetch(`${API_CONFIG.API_URL}/orders/create-from-cart`, {
      method: 'POST',
      headers: {
         'Authorization': `Bearer ${token}`
      }
   }).then((res) => {
      if (!res.ok) {
         throw new Error('Failed to create order from cart');
      }
      return res.json();
   });
}

export function createCheckoutSession(paymentRequest: {
   userId: number;
   orderId: number;
   totalAmount: number;
   items: Array<{
      bookId: number;
      title: string;
      price: number;
      quantity: number;
   }>;
}){
   const token = sessionStorage.getItem('authToken');
   return fetch(`${API_CONFIG.API_URL}/payment/create-checkout-session`, {
      method: 'POST',
      headers: {
         'Authorization': `Bearer ${token}`,
         'Content-Type': 'application/json'
      },
      body: JSON.stringify(paymentRequest)
   }).then((res) => {
      if (!res.ok) {
         throw new Error('Failed to create checkout session');
      }
      return res.json();
   });
}