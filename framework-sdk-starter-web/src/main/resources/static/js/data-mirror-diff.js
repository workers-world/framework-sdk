(function (global) {
    'use strict';

    function esc(s) {
        if (s == null || s === undefined) return '';
        return String(s).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    }

    function filterDiffs(fieldDiffs, changedOnly) {
        if (!fieldDiffs) return [];
        if (!changedOnly) return fieldDiffs;
        return fieldDiffs.filter(function (d) { return d.changed; });
    }

    function formatSummary(changedFieldsJson) {
        if (!changedFieldsJson) return '—';
        try {
            var arr = JSON.parse(changedFieldsJson);
            if (!arr || !arr.length) return '—';
            return arr.join('、');
        } catch (e) {
            return changedFieldsJson;
        }
    }

    function renderSide(fieldDiffs, opts) {
        var rows = filterDiffs(fieldDiffs, opts.changedOnly);
        if (!rows.length) {
            return '<p class="muted">无差异字段</p>';
        }
        var html = '<div class="dm-table-wrap"><table class="dm-diff-table"><thead><tr>' +
            '<th>字段</th><th>变更前</th><th>变更后</th></tr></thead><tbody>';
        rows.forEach(function (d) {
            var beforeCls = d.changed ? 'dm-diff-before' : '';
            var afterCls = d.changed ? 'dm-diff-after' : '';
            html += '<tr><td class="dm-field-label">' + esc(d.label || d.fieldKey) + '</td>' +
                '<td class="' + beforeCls + '">' + esc(d.beforeDisplay) + '</td>' +
                '<td class="' + afterCls + '">' + esc(d.afterDisplay) + '</td></tr>';
        });
        return html + '</tbody></table></div>';
    }

    function renderUnified(fieldDiffs, opts) {
        var rows = filterDiffs(fieldDiffs, opts.changedOnly);
        if (!rows.length) {
            return '<p class="muted">无差异字段</p>';
        }
        var html = '<div class="dm-unified">';
        rows.forEach(function (d) {
            html += '<div class="dm-unified-block">';
            html += '<div class="dm-unified-head">' + esc(d.label || d.fieldKey) + '</div>';
            if (d.changed) {
                html += '<div class="dm-unified-line dm-diff-before"><span class="dm-sign">−</span> ' + esc(d.beforeDisplay) + '</div>';
                html += '<div class="dm-unified-line dm-diff-after"><span class="dm-sign">+</span> ' + esc(d.afterDisplay) + '</div>';
            } else {
                html += '<div class="dm-unified-line muted">' + esc(d.beforeDisplay) + '</div>';
            }
            html += '</div>';
        });
        return html + '</div>';
    }

    function renderStack(fieldDiffs, opts) {
        var rows = filterDiffs(fieldDiffs, opts.changedOnly);
        if (!rows.length) {
            return '<p class="muted">无差异字段</p>';
        }
        var html = '<div class="dm-stack">';
        rows.forEach(function (d) {
            html += '<div class="dm-stack-item' + (d.changed ? ' dm-stack-changed' : '') + '">';
            html += '<div class="dm-stack-label">' + esc(d.label || d.fieldKey) + '</div>';
            html += '<div class="dm-stack-row"><span class="muted">前</span><span class="' + (d.changed ? 'dm-diff-before' : '') + '">' + esc(d.beforeDisplay) + '</span></div>';
            html += '<div class="dm-stack-row"><span class="muted">后</span><span class="' + (d.changed ? 'dm-diff-after' : '') + '">' + esc(d.afterDisplay) + '</span></div>';
            html += '</div>';
        });
        return html + '</div>';
    }

    function render(fieldDiffs, options) {
        var opts = options || {};
        var mode = opts.mode || 'side';
        if (mode === 'unified') return renderUnified(fieldDiffs, opts);
        if (mode === 'stack') return renderStack(fieldDiffs, opts);
        return renderSide(fieldDiffs, opts);
    }

    global.DataMirrorDiff = {
        esc: esc,
        render: render,
        formatSummary: formatSummary
    };
})(typeof window !== 'undefined' ? window : this);
