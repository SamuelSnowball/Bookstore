import { Box, Stack } from "@mui/material";
import { Book } from "./types";
import { Item } from "./components/MuiExtras";
import placeholderImage from "./assets/placeholder.png";

function BookPanel(props: Book) {



  return (
    <Box sx={{}}>
      <Stack direction={"row"}>
        <Item style={{ width: "33%" }}>
          <img src={placeholderImage} />
        </Item>
        <Item style={{ width: "33%" }}>
        
            {props.title}
        
        </Item>
        <Item style={{ width: "33%" }}>Item 3</Item>
      </Stack>
    </Box>
  );
}

export default BookPanel;
