import { Card, CardContent, CardMedia, CardActions, Typography, Button, Box } from "@mui/material";
import { ShoppingCart } from "@mui/icons-material";
import { Book } from "./types";
import placeholderImage from "./assets/placeholder.png";

interface BookPanelProps extends Book {
  onAddToCart: (bookId: number) => void;
}

function BookPanel(props: BookPanelProps) {
  const handleAddToCart = () => {
    if (props.id) {
      props.onAddToCart(props.id);
    }
  };

  return (
    <Card 
   data-book-id={props.id}

      sx={{ 
        display: 'flex', 
        mb: 2,
        transition: 'transform 0.2s, box-shadow 0.2s',
        '&:hover': {
          transform: 'translateY(-4px)',
          boxShadow: 4
        }
      }}
    >
      <CardMedia
        component="img"
        sx={{ width: 140 }}
        image={placeholderImage}
        alt={String(props.title)}
      />
      <Box sx={{ display: 'flex', flexDirection: 'column', flex: 1 }}>
        <CardContent sx={{ flex: '1 0 auto' }}>
          <Typography component="div" variant="h6">
            {props.title}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {props.firstName && props.lastName ? `by ${props.firstName} ${props.lastName}` : `Author ID: ${props.authorId}`}
          </Typography>
          {props.description && (
            <Typography variant="body2" color="text.secondary" sx={{ mt: 1, fontStyle: 'italic' }}>
              {props.description}
            </Typography>
          )}
          {props.price !== undefined && (
            <Typography variant="h6" color="primary" sx={{ mt: 1 }}>
              ${props.price.toFixed(2)}
            </Typography>
          )}
        </CardContent>
        <CardActions sx={{ justifyContent: 'flex-end' }}>
          <Button 
            variant="contained" 
            color="primary"
            startIcon={<ShoppingCart />}
            onClick={handleAddToCart}
            disabled={!props.id}
            size="small"
          >
            Add to Cart
          </Button>
        </CardActions>
      </Box>
    </Card>
  );
}

export default BookPanel;
