
export function getBooks(){
   return fetch('http://localhost:8080/book').then((res) =>
        res.json()
    );
}