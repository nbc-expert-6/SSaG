// 전역 상태
let currentState = {
    page: 0,
    size: 'SIZE_30',
    sort: 'POPULAR',
    searchKeyword: '',
    includeShipping: false,
    viewMode: 'list',
    categoryIds: []
};

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function () {
    initializePage();
    loadProducts();

    // 검색 입력 처리
    const searchInput = document.getElementById('searchInput');
    const clearBtn = document.getElementById('clearBtn');

    searchInput.addEventListener('keypress', function (e) {
        if (e.key === 'Enter') {
            searchProducts();
        }
    });

    searchInput.addEventListener('input', function () {
        clearBtn.style.display = this.value ? 'flex' : 'none';
    });
});

// 페이지 초기화
function initializePage() {
    const urlParams = new URLSearchParams(window.location.search);
    const keyword = urlParams.get('keyword');
    if (keyword) {
        document.getElementById('searchInput').value = keyword;
        currentState.searchKeyword = keyword;
    }
}

// 검색어 클리어
function clearSearch() {
    document.getElementById('searchInput').value = '';
    document.getElementById('clearBtn').style.display = 'none';
    currentState.searchKeyword = '';
    currentState.page = 0;
    loadProducts();
}

// 상품 검색
function searchProducts() {
    const keyword = document.getElementById('searchInput').value.trim();
    currentState.searchKeyword = keyword;
    currentState.page = 0;
    loadProducts();
}

// 정렬 타입 변경
function changeSortType(button) {
    document.querySelectorAll('.sort-btn').forEach(btn => {
        btn.classList.remove('active');
    });

    button.classList.add('active');
    currentState.sort = button.getAttribute('data-sort');
    currentState.page = 0;
    loadProducts();
}

// 뷰 모드 변경
function changeView(mode) {
    currentState.viewMode = mode;

    document.querySelectorAll('.view-icon').forEach(btn => {
        btn.classList.remove('active');
    });
    event.target.closest('.view-icon').classList.add('active');

    const productList = document.getElementById('productList');
    if (mode === 'grid') {
        productList.classList.add('grid-view');
    } else {
        productList.classList.remove('grid-view');
    }
}

// 페이지 사이즈 변경
function changePageSize() {
    const sizeSelect = document.getElementById('pageSizeSelect');
    currentState.size = 'SIZE_' + sizeSelect.value;
    currentState.page = 0;
    loadProducts();
}

// 배송비 토글
function toggleShipping() {
    const toggle = document.getElementById('shippingToggle');
    const text = document.querySelector('.toggle-text');

    currentState.includeShipping = toggle.checked;
    text.textContent = toggle.checked ? 'ON' : 'OFF';

    currentState.page = 0;
    loadProducts();
}

// 상품 목록 로드
async function loadProducts() {
    showLoading();

    try {
        const requestBody = {
            productName: currentState.searchKeyword || null,
            categoryIds: currentState.categoryIds.length > 0 ? currentState.categoryIds : null,
            page: currentState.page,
            size: currentState.size,
            sort: currentState.sort
        };

        const response = await fetch('/api/v1/main-products/search', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(requestBody)
        });

        if (!response.ok) {
            throw new Error('상품 목록을 불러오는데 실패했습니다.');
        }

        const result = await response.json();

        if (result.success && result.data) {
            renderProducts(result.data.products);
            renderPagination(result.data.products);
            updateTotalCount(result.data.products.totalElements);
        } else {
            showError('상품 목록을 불러오는데 실패했습니다.');
        }

    } catch (error) {
        console.error('Error loading products:', error);
        showError(error.message);
    }
}

// 로딩 표시
function showLoading() {
    const productList = document.getElementById('productList');
    productList.innerHTML = `
        <div class="loading">
            <div class="spinner"></div>
            <p>상품을 불러오는 중...</p>
        </div>
    `;
}

// 에러 표시
function showError(message) {
    const productList = document.getElementById('productList');
    productList.innerHTML = `
        <div class="empty-state">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="12" y1="8" x2="12" y2="12"></line>
                <line x1="12" y1="16" x2="12.01" y2="16"></line>
            </svg>
            <h3>${message}</h3>
        </div>
    `;
}

// 상품 렌더링
function renderProducts(pageData) {
    const productList = document.getElementById('productList');

    if (!pageData.content || pageData.content.length === 0) {
        productList.innerHTML = `
            <div class="empty-state">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="11" cy="11" r="8"></circle>
                    <path d="m21 21-4.35-4.35"></path>
                </svg>
                <h3>검색 결과가 없습니다</h3>
                <p>다른 검색어로 시도해보세요</p>
            </div>
        `;
        return;
    }

    productList.innerHTML = pageData.content.map(product => createProductItem(product)).join('');
}

// 이미지 로드 에러 핸들러 (한 번만 시도)
function handleImageError(img) {
    if (!img.dataset.errorHandled) {
        img.dataset.errorHandled = 'true';
        img.src = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='200' height='200'%3E%3Crect fill='%23f0f0f0' width='200' height='200'/%3E%3Ctext x='50%25' y='50%25' dominant-baseline='middle' text-anchor='middle' font-family='Arial' font-size='14' fill='%23999'%3E이미지 없음%3C/text%3E%3C/svg%3E";
    }
}

// 상품 아이템 생성 (다나와 스타일)
function createProductItem(product) {
    const isNew = isNewProduct(product.createdAt);
    const imageUrl = product.imageUrl || "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='200' height='200'%3E%3Crect fill='%23f0f0f0' width='200' height='200'/%3E%3Ctext x='50%25' y='50%25' dominant-baseline='middle' text-anchor='middle' font-family='Arial' font-size='14' fill='%23999'%3E이미지 없음%3C/text%3E%3C/svg%3E";

    return `
        <div class="product-item" data-product-id="${product.id}" onclick="goToDetail('${product.id}')">
            <div class="product-image-wrap">
                <img src="${imageUrl}" alt="${escapeHtml(product.name)}" class="product-image" 
                     onerror="handleImageError(this)">
            </div>
            
            <div class="product-info-wrap">
                <div class="product-title">
                    <span class="platform">맨투맨</span>
                    ${escapeHtml(product.name)}
                </div>
                
                ${product.brand ? `
                    <div class="product-subtitle">
                        <span class="brand">${escapeHtml(product.brand)}</span>
                    </div>
                ` : ''}
                
                <div class="product-meta">
                    <span class="product-date">등록월 ${formatDate(product.createdAt)}</span>
                    ${product.rating > 0 ? `
                        <div class="product-rating">
                            <span class="star">★</span>
                            <span class="rating-value">${product.rating.toFixed(1)}</span>
                            <span class="review-count">(${formatNumber(product.reviewCount)})</span>
                        </div>
                    ` : ''}
                    ${product.clickCount > 0 ? `
                        <span class="product-like">♡ 관심</span>
                    ` : ''}
                </div>
                
                <div class="product-breadcrumb">
                    ${product.categoryId ? `카테고리 > ${product.categoryId}` : ''}
                </div>
            </div>
            
            <div class="product-price-wrap">
                <div class="product-price">
                    ${formatPrice(product.lowestPrice)}
                    ${isNew ? '<span class="price-badge">New</span>' : ''}
                </div>
                
                ${product.productCount > 1 ? `
                    <div class="product-stores">
                        ${product.productCount}개몰
                    </div>
                    <div class="product-compare">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M3 3h7v7H3zM14 3h7v7h-7zM14 14h7v7h-7zM3 14h7v7H3z"/>
                        </svg>
                        가격비교
                    </div>
                ` : ''}
            </div>
        </div>
    `;
}

// 페이지네이션 렌더링 (10개 블록 단위, 무한 스크롤 가능)
function renderPagination(pageData) {
    const pagination = document.getElementById('pagination');
    const totalPages = pageData.totalPages;
    const currentPage = pageData.page;

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
        <button class="page-btn" onclick="goToPage(${startPage - 1})" 
                ${!hasPrevBlock ? 'disabled' : ''}>
            ◀
        </button>
    `;

    // 페이지 번호 (현재 블록의 10개, 단 totalPages 이내만)
    for (let i = startPage; i <= renderEndPage; i++) {
        html += `
            <button class="page-btn ${i === currentPage ? 'active' : ''}" onclick="goToPage(${i})">
                ${i + 1}
            </button>
        `;
    }

    // 다음 블록 버튼 (▶) - 항상 활성화
    html += `
        <button class="page-btn" onclick="goToPage(${endPage + 1})">
            ▶
        </button>
    `;

    pagination.innerHTML = html;
}

// 페이지 이동
function goToPage(page) {
    currentState.page = page;
    loadProducts();
    window.scrollTo({top: 0, behavior: 'smooth'});
}

// 상세 페이지로 이동
function goToDetail(productId) {
    window.location.href = `/products/${productId}`;
}

// 전체 개수 업데이트
function updateTotalCount(total) {
    document.getElementById('totalCount').textContent = `(${formatNumber(total)})`;
}

// 유틸리티 함수들
function formatPrice(price) {
    if (!price) return '가격 정보 없음';
    return price.toLocaleString('ko-KR') + '원';
}

function formatNumber(num) {
    if (!num) return '0';
    return num.toLocaleString('ko-KR');
}

function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return `${date.getFullYear()}.${String(date.getMonth() + 1).padStart(2, '0')}.`;
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function isNewProduct(createdAt) {
    if (!createdAt) return false;
    const created = new Date(createdAt);
    const now = new Date();
    const diffDays = (now - created) / (1000 * 60 * 60 * 24);
    return diffDays <= 7;
}
