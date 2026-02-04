import {
  ListItem,
  ListItemAvatar,
  ListItemText,
  Avatar,
  IconButton,
  Divider,
} from "@mui/material";
import { Delete } from "@mui/icons-material";
import { CartItem as CartItemType } from "./types";
import placeholderImage from "./assets/placeholder.png";

interface CartItemProps extends CartItemType {
  handleRemoveFromCart: (cartItemId: number) => void;
}

function CartItem(props: CartItemProps) {
  const handleRemoveFromCart = () => {
    if (props.cartItemId) {
      props.handleRemoveFromCart(props.cartItemId);
    }
  };

  console.log("Rendering CartItem for cart item ID:", props.cartItemId);

  return (
    <div data-book-id={props.bookId}>
      <ListItem
        secondaryAction={
          <IconButton
            edge="end"
            aria-label="delete"
            onClick={handleRemoveFromCart}
            disabled={!props.cartItemId}
            color="error"
          >
            <Delete />
          </IconButton>
        }
      >
        <ListItemAvatar>
          <Avatar
            variant="rounded"
            src={placeholderImage}
            alt={String(props.title)}
            sx={{ width: 56, height: 56 }}
          />
        </ListItemAvatar>
        <ListItemText
          primary={props.title}
          secondary={
            <>
              {props.firstName && props.lastName ? `by ${props.firstName} ${props.lastName}` : `Author ID: ${props.authorId}`}
              <br />
              <span style={{ fontWeight: 'bold' }}>
                Quantity: {props.bookQuantity} × ${props.price?.toFixed(2) || '0.00'} = ${((props.bookQuantity * (props.price || 0)).toFixed(2))}
              </span>
              {props.description && (
                <>
                  <br />
                  <span style={{ fontStyle: 'italic', fontSize: '0.875rem' }}>{props.description}</span>
                </>
              )}
            </>
          }
          sx={{ ml: 2 }}
        />
      </ListItem>
      <Divider variant="inset" component="li" />
    </div>
  );
}

export default CartItem;
