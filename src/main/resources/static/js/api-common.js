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
    dto: (msg, name) => msg?.data?.[name] || null,

    // 检查 BaseMessage 响应是否成功
    ok: (msg) => msg?.code === 'SUCCESS',

    // 高层调用：post + ok 检查 + dto 提取，失败抛异常
    call: async (url, inputName, input, outputName) => {
        const msg = await api.post(url, api.wrap(inputName, input));
        if (!api.ok(msg)) throw new Error(msg?.message || '请求失败');
        return outputName ? api.dto(msg, outputName) : msg;
    }
};

function esc(s) {
    if (s == null || s === undefined) return '';
    return String(s).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

function escAttr(s) {
    return esc(s).replace(/'/g, '&#39;');
}

function renderPager(page, total, limit, prevBtn, nextBtn, pageInfoEl, totalEl) {
    const pages = Math.max(1, Math.ceil(total / limit));
    if (pageInfoEl) pageInfoEl.textContent = page + ' / ' + pages;
    if (totalEl) totalEl.textContent = '共 ' + total + ' 条';
    if (prevBtn) prevBtn.disabled = page <= 1;
    if (nextBtn) nextBtn.disabled = page >= pages;
}

function focusModalFirst(el) {
    setTimeout(() => {
        try { el?.focus(); } catch (e) { /* ignore */ }
    }, 100);
}

// 加载渠道选项到 <select>，调 /api/options/channels
function downloadCsv(filename, headers, rows) {
    const b = 'data:text/csv;charset=utf-8,﻿';
    const csv = [headers.join(','), ...rows.map(r => r.map(c => '"' + String(c||'').replace(/"/g,'""') + '"').join(','))].join('\n');
    const a = document.createElement('a');
    a.href = b + encodeURIComponent(csv);
    a.download = filename;
    a.click();
}

async function loadChannelOptions(sel) {
    try {
        const data = await api.call('/api/options/channels');
        const list = data?.data?.ChannelOptionZ1 || [];
        sel.innerHTML = list.map(c => `<option value="${c.code || ''}">${c.name || c.code || ''}</option>`).join('');
    } catch (e) { console.warn('加载渠道列表失败', e); }
}
