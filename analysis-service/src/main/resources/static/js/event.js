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
    sendEvent("PAGE_VIEW", null, location.pathname);

    // 3) 스크롤 이벤트
    let scrolled = false;
    window.addEventListener("scroll", () => {
        if (!scrolled && window.scrollY / (document.body.scrollHeight - window.innerHeight) > 0.5) {
            scrolled = true;
            sendEvent("SCROLL_HALF", null);
        }
    });


    // 4) 페이지에서 특정 영역 머문 시간 측정
    setTimeout(() => {
        sendEvent("TIME_ON_PAGE", null, "30_seconds");
    }, 30000);


});