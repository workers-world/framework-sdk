// 通用 JS 工具 — 所有页面共享，通过 <script src="/js/api-common.js"></script> 引入

const api = {
    post: (url, data) => fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data || {})
    }).then(r => r.json()),

    // 包装 DTO 到 BaseMessage
    wrap: (dtoName, obj) => {
        const r = { messageId: 'm' + Date.now(), timestamp: String(Date.now()),
            source: 'ui', version: '1.0', data: {} };
        if (dtoName && obj) r.data[dtoName] = obj;
        return r;
    },

    // 从 BaseMessage 中提取 DTO
    dto: (msg, name) => msg?.data?.[name] || null
};

function esc(s) {
    return (s || '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

function renderPager(page, total, limit, prevBtn, nextBtn, pageInfoEl, totalEl) {
    const pages = Math.max(1, Math.ceil(total / limit));
    if (pageInfoEl) pageInfoEl.textContent = page + ' / ' + pages;
    if (totalEl) totalEl.textContent = '共 ' + total + ' 条';
    if (prevBtn) prevBtn.disabled = page <= 1;
    if (nextBtn) nextBtn.disabled = page >= pages;
}
