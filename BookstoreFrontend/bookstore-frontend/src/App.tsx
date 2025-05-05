import { useQuery, useQueryClient } from "@tanstack/react-query";
import { getBooks } from "./api";
import "./App.css";
import BookPanel from "./BookPanel";
import { Book } from "./types";

function App() {
  const queryClient = useQueryClient();
  const getBooksQuery = useQuery({ queryKey: ["books"], queryFn: getBooks });

  return (
    <>
      <h1>Vite + React</h1>

      <button onClick={() => {}}>Show books?</button>
      
      <div className="bookPanel">
        {
          // Pass all of the books properties as seperate props
          getBooksQuery.data?.map((book: Book) => (
            <BookPanel {...book} />
          ))
        }
      </div>
    </>
  );
}

export default App;
