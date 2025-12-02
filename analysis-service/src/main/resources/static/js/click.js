document.addEventListener("DOMContentLoaded", () => {

    document.querySelectorAll(".product").forEach(product => {
        product.addEventListener("click", () => {
            const productId = product.dataset.productId;

            fetch('/api/v1/track-click', {
                method: 'POST',
                credentials: 'include',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({productId})
            })
                .then(res => console.log('클릭된 상품Id : ', productId))
                .catch(err => console.error(err));
        });
    });

});