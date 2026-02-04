
export interface Book {
    id?: number;
    authorId: number;
    title: string;
    price?: number;
    description?: string;
    firstName?: string;
    lastName?: string;
}

export interface CartItem {
    cartItemId: number;
    userId?: number;
    bookId: number;
    bookQuantity: number;
    authorId: number;
    title: string;
    price?: number;
    description?: string;
    firstName?: string;
    lastName?: string;
}

export interface Address {
    id?: number;
    userId?: number;
    streetAddress: string;
    city: string;
    state?: string;
    postalCode: string;
    country: string;
    isDefault?: boolean;
}