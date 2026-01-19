async function fetchData(url) {
    try {
        const response = await fetch(url);
        const data = await response.json();
        return data;
    } catch (error) {
        console.error('Error:', error);
        throw error;
    }
}

fetchData('https://api.example.com/data')
    .then(data => console.log(data))
    .catch(err => console.error(err));
