document.addEventListener("DOMContentLoaded", () => {

    console.log("DOMContentLoaded fired");


    // 모든 이벤트 공통 처리 함수
    function sendEvent(eventType, productId = null, meta = null) {
        fetch("http://k8s-ssagalb-682aa72d54-916392037.ap-northeast-2.elb.amazonaws.com/api/v1/user-event", {
            method: "POST",
            credentials: "include",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({productId, eventType, meta})
        }).catch(err => console.error(err));
    }

    // 1) 상품 클릭 이벤트
    const productList = document.getElementById("productList");
    productList.addEventListener("click", (e) => {
        // 클릭된 요소가 product-item인지 확인
        const productItem = e.target.closest(".product-item");
        if (!productItem) return;

        const productId = productItem.dataset.productId;
        sendEvent("CLICK", productId);

        e.stopPropagation(); // 중복 이벤트 방지
        setTimeout(() => {
            // 기존 onclick 함수 호출 유지
            const onclickAttr = productItem.getAttribute("onclick");
            if (onclickAttr) {
                eval(onclickAttr);
            }
        }, 300);
    });

    // 2) 페이지 진입 이벤트
    sendEvent("PAGE_ENTER", null, JSON.stringify({path: location.pathname}));

    // 3) 페이지 이탈 이벤트
    window.addEventListener("beforeunload", (event) => {
        const payload = JSON.stringify({eventType: "PAGE_EXIT", meta: location.pathname});
        const blob = new Blob([payload], {type: "application/json"});
        navigator.sendBeacon("http://k8s-ssagalb-682aa72d54-916392037.ap-northeast-2.elb.amazonaws.com/api/v1/user-event", blob);
    });

});
