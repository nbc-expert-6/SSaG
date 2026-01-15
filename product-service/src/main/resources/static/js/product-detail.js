// URL에서 상품 ID 가져오기
const productId = window.location.pathname.split('/').pop();

// 전역 상태
let currentReviewPage = 0;
let currentReviewSort = 'latest';
let productData = null;
let reviewData = null;

// 페이지 로드
document.addEventListener('DOMContentLoaded', function () {
    loadProductDetail();
    loadReviews();
    initTabs();
});

// 탭 초기화
function initTabs() {
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.addEventListener('click', function () {
            const tabName = this.getAttribute('data-tab');
            switchTab(tabName);
        });
    });
}

// 탭 전환
function switchTab(tabName) {
    // 탭 버튼 active
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    document.querySelector(`[data-tab="${tabName}"]`).classList.add('active');

    // 탭 내용 active
    document.querySelectorAll('.tab-content').forEach(content => {
        content.classList.remove('active');
    });
    document.getElementById(`${tabName}Tab`).classList.add('active');
}

// 상품 상세 정보 로드
async function loadProductDetail() {
    try {
        const response = await fetch(`/api/v1/main-products/${productId}`);

        if (!response.ok) {
            throw new Error('상품 정보를 불러오는데 실패했습니다.');
        }

        const result = await response.json();

        if (result.success && result.data) {
            productData = result.data;
            renderProductDetail(result.data);
        } else {
            showError('상품 정보를 불러오는데 실패했습니다.');
        }

    } catch (error) {
        console.error('Error loading product:', error);
        showError(error.message);
    }
}

// 상품 정보 렌더링
function renderProductDetail(data) {
    const {mainProduct, priceComparison, reviews} = data;

    const container = document.getElementById('productDetail');
    container.dataset.productId = productId;

    // 브레드크럼
    document.getElementById('categoryPath').textContent =
        `${mainProduct.category.large.name} > ${mainProduct.category.medium.name}`;

    // 이미지
    const mainImage = document.getElementById('mainImage');
    mainImage.src = mainProduct.imageUrl || getPlaceholderImage();
    mainImage.alt = mainProduct.name;

    // 상품명
    document.getElementById('productName').textContent = mainProduct.name;

    // 최저가
    document.getElementById('lowestPrice').textContent = mainProduct.lowestPrice;

    // 구매 버튼
    const lowestProduct = priceComparison.allProducts.find(p => p.isLowest);
    if (lowestProduct) {
        document.getElementById('buyBtn').onclick = () => {
            window.open(lowestProduct.saleLink, '_blank');
        };
    }

    // 가격 비교 테이블 (플랫폼별 최저가)
    renderPriceTable(priceComparison.platformLowestPrices);

    // 온라인 쇼핑몰 리스트 (전체 상품)
    renderOnlineShopList(priceComparison.allProducts);

    // 리뷰 요약
    renderReviewSummary(mainProduct, reviews);

    // 카운트 표시
    document.getElementById('priceCompareCount').textContent = `${priceComparison.totalCount}`;
}

// 가격 테이블 렌더링
function renderPriceTable(platformPrices) {
    const html = platformPrices.slice(0, 5).map(item => `
        <div class="price-row ${item.isGlobalLowest ? 'lowest' : ''}">
            <div class="mall-name">${item.platformName}</div>
            <div>
                <div class="product-price-text">${item.price}</div>
                <div class="shipping-fee ${item.shippingFee === '무료배송' ? '' : 'paid'}">
                    ${item.shippingFee}
                </div>
            </div>
            <div class="product-price-text">${item.totalPrice}</div>
            <button class="go-shop-btn" onclick="window.open('${item.saleLink}', '_blank')">
                구매하기
            </button>
        </div>
    `).join('');

    document.getElementById('priceTable').innerHTML = html;
}

// 온라인 쇼핑몰 리스트 렌더링
function renderOnlineShopList(products) {
    const html = products.map(item => `
        <div class="shop-item">
            <div class="mall-name">${item.platformName}</div>
            <div>${item.productName}</div>
            <div>
                <div class="product-price-text">${item.price}</div>
                <div class="shipping-fee">${item.shippingFee}</div>
            </div>
            <div class="product-price-text">${item.totalPrice}</div>
            <button class="go-shop-btn" onclick="window.open('${item.saleLink}', '_blank')">
                구매하기
            </button>
        </div>
    `).join('');

    document.getElementById('onlineShopList').innerHTML = html;
}

// 리뷰 요약 렌더링
function renderReviewSummary(mainProduct, reviews) {
    // 평균 평점
    const avgRating = reviews.length > 0 ?
        (reviews.reduce((sum, r) => sum + parseFloat(r.rating), 0) / reviews.length).toFixed(1) :
        '0.0';

    document.getElementById('avgRating').textContent = avgRating;

    // 별점
    const stars = renderStars(parseFloat(avgRating));
    document.getElementById('avgStars').innerHTML = stars;

    // 리뷰 개수
    document.getElementById('reviewCount').textContent = `(${reviews.length})`;

    // 평점 분포
    const distribution = calculateRatingDistribution(reviews);
    renderRatingDistribution(distribution);
}

// 별점 렌더링
function renderStars(rating) {
    let html = '';
    for (let i = 1; i <= 5; i++) {
        html += i <= rating ?
            '<span class="star">★</span>' :
            '<span class="star empty">★</span>';
    }
    return html;
}

// 평점 분포 계산
function calculateRatingDistribution(reviews) {
    const dist = {5: 0, 4: 0, 3: 0, 2: 0, 1: 0};
    reviews.forEach(review => {
        const rating = Math.floor(parseFloat(review.rating));
        if (dist[rating] !== undefined) dist[rating]++;
    });

    const total = reviews.length || 1;
    return Object.entries(dist).reverse().map(([rating, count]) => ({
        rating,
        count,
        percent: Math.round((count / total) * 100)
    }));
}

// 평점 분포 렌더링
function renderRatingDistribution(distribution) {
    const html = distribution.map(item => `
        <div class="rating-bar">
            <div class="rating-label">${item.rating}점</div>
            <div class="bar-container">
                <div class="bar-fill" style="width: ${item.percent}%"></div>
            </div>
            <div class="rating-percent">${item.percent}%</div>
        </div>
    `).join('');

    document.getElementById('ratingDistribution').innerHTML = html;
}

// 리뷰 로드
async function loadReviews() {
    try {
        const response = await fetch(
            `/api/v1/reviews?mainProductId=${productId}&page=${currentReviewPage}&size=SIZE_30`
        );

        if (!response.ok) {
            throw new Error('리뷰를 불러오는데 실패했습니다.');
        }

        const result = await response.json();

        if (result.success && result.data) {
            reviewData = result.data;
            renderReviews(result.data);
        }

    } catch (error) {
        console.error('Error loading reviews:', error);
    }
}

// 리뷰 렌더링
function renderReviews(data) {
    const {summary, reviews} = data;

    // 리뷰 탭 카운트
    document.getElementById('reviewTabCount').textContent = summary.totalCount;

    // 리뷰 리스트
    if (!reviews.content || reviews.content.length === 0) {
        document.getElementById('reviewList').innerHTML = `
            <div style="text-align: center; padding: 60px 20px; color: #999;">
                <p>아직 작성된 리뷰가 없습니다.</p>
            </div>
        `;
        return;
    }

    const html = reviews.content.map(review => `
        <div class="review-item">
            <div class="review-header">
                <div class="review-rating">
                    ${renderStars(parseFloat(review.rating))}
                </div>
                <div class="review-platform">${review.platformType}</div>
            </div>
            
            <div class="review-meta">
                <span>${review.authorName}</span>
                <span>${formatDate(review.createdAt)}</span>
            </div>
            
            ${review.title ? `<div class="review-title">${escapeHtml(review.title)}</div>` : ''}
            
            <div class="review-content">${escapeHtml(review.content)}</div>
            
            ${review.images && review.images.length > 0 ? `
                <div class="review-images">
                    ${review.images.map(img => `
                        <img src="${img}" class="review-image" alt="리뷰 이미지">
                    `).join('')}
                </div>
            ` : ''}
        </div>
    `).join('');

    document.getElementById('reviewList').innerHTML = html;

    // 페이지네이션
    renderReviewPagination(reviews);
}

// 리뷰 페이지네이션 (10개 블록 단위, 무한 스크롤 가능)
function renderReviewPagination(pageData) {
    const totalPages = pageData.totalPages;
    const currentPage = pageData.number;

    // 현재 페이지가 속한 10개 블록 계산
    const blockSize = 10;
    const currentBlock = Math.floor(currentPage / blockSize);
    const startPage = currentBlock * blockSize;
    const endPage = startPage + blockSize - 1; // 무조건 10개 블록

    // 실제 렌더링할 마지막 페이지 (totalPages 넘지 않도록)
    const renderEndPage = Math.min(endPage, totalPages - 1);

    let html = '';

    // 이전 블록 버튼 (◀)
    const hasPrevBlock = startPage > 0;
    html += `
        <button class="page-btn" onclick="goToReviewPage(${startPage - 1})" 
                ${!hasPrevBlock ? 'disabled' : ''}>
            ◀
        </button>
    `;

    // 페이지 번호 (현재 블록의 10개, 단 totalPages 이내만)
    for (let i = startPage; i <= renderEndPage; i++) {
        html += `
            <button class="page-btn ${i === currentPage ? 'active' : ''}" 
                    onclick="goToReviewPage(${i})">
                ${i + 1}
            </button>
        `;
    }

    // 다음 블록 버튼 (▶) - 항상 활성화
    html += `
        <button class="page-btn" onclick="goToReviewPage(${endPage + 1})">
            ▶
        </button>
    `;

    document.getElementById('reviewPagination').innerHTML = html;
}

// 리뷰 페이지 이동
function goToReviewPage(page) {
    currentReviewPage = page;
    loadReviews();

    // 리뷰 탭으로 스크롤
    document.getElementById('reviewTab').scrollIntoView({behavior: 'smooth'});
}

// 리뷰 정렬 변경
function changeReviewSort() {
    currentReviewSort = document.getElementById('reviewSort').value;
    currentReviewPage = 0;
    loadReviews();
}

// 검색
function searchProducts() {
    const keyword = document.getElementById('searchInput').value.trim();
    if (keyword) {
        window.location.href = `/products?keyword=${encodeURIComponent(keyword)}`;
    }
}

// 이미지 에러 핸들러
function handleImageError(img) {
    if (!img.dataset.errorHandled) {
        img.dataset.errorHandled = 'true';
        img.src = getPlaceholderImage();
    }
}

// Placeholder 이미지
function getPlaceholderImage() {
    return "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='400' height='400'%3E%3Crect fill='%23f0f0f0' width='400' height='400'/%3E%3Ctext x='50%25' y='50%25' dominant-baseline='middle' text-anchor='middle' font-family='Arial' font-size='16' fill='%23999'%3E이미지 없음%3C/text%3E%3C/svg%3E";
}

// 에러 표시
function showError(message) {
    alert(message);
    window.location.href = '/products';
}

// 날짜 포맷
function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return `${date.getFullYear()}.${String(date.getMonth() + 1).padStart(2, '0')}.${String(date.getDate()).padStart(2, '0')}`;
}

// HTML 이스케이프
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
