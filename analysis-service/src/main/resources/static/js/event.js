document.addEventListener("DOMContentLoaded", () => {

    // 모든 이벤트 공통 처리 함수
    function sendEvent(eventType, productId = null, meta = null) {
        fetch("/api/v1/user-event", {
            method: "POST",
            credentials: "include",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({productId, eventType, meta})
        }).catch(err => console.error(err));
    }

    // 1) 상품 클릭 이벤트
    document.querySelectorAll(".product").forEach(product => {
        product.addEventListener("click", () => {
            const productId = product.dataset.productId;
            sendEvent("CLICK", productId);
        });
    });

    // 2) 페이지 진입 이벤트
    sendEvent("PAGE_ENTER", null, JSON.stringify({path: location.pathname}));

    // 3) 페이지 이탈 이벤트
    window.addEventListener("beforeunload", (event) => {
        const payload = JSON.stringify({eventType: "PAGE_EXIT", meta: location.pathname});
        const blob = new Blob([payload], {type: "application/json"});
        navigator.sendBeacon("/api/v1/user-event", blob);
    });

});