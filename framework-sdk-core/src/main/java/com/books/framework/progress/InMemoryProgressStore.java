package com.books.framework.progress;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存进度存储。
 * <p>终态(COMPLETE/FAILED)快照保留一段时间供前端兜底轮询,之后驱逐,
 * 否则每个任务永久占一条内存(Redis 实现有 TTL,内存版此前没有)。</p>
 */
public class InMemoryProgressStore implements ProgressStore {

    /** 终态快照保留时长(默认 1 小时,与 Redis 实现的 TTL 语义对齐但更短) */
    private final long terminalRetentionMs;
    /** 终态保留上限:超过后无论新旧驱逐最旧终态条目,防突发任务风暴撑爆内存 */
    private final int maxTerminalEntries;

    private final Map<String, ProgressSnapshot> snapshots = new ConcurrentHashMap<>();

    public InMemoryProgressStore() {
        this(60 * 60 * 1000L, 1000);
    }

    public InMemoryProgressStore(long terminalRetentionMs, int maxTerminalEntries) {
        this.terminalRetentionMs = terminalRetentionMs;
        this.maxTerminalEntries = maxTerminalEntries;
    }

    @Override
    public void save(ProgressSnapshot snapshot) {
        if (snapshot == null || snapshot.getTaskId() == null) {
            return;
        }
        evictExpiredTerminal();
        snapshots.put(snapshot.getTaskId(), snapshot);
    }

    @Override
    public Optional<ProgressSnapshot> get(String taskId) {
        if (taskId == null || taskId.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(snapshots.get(taskId));
    }

    private void evictExpiredTerminal() {
        long now = System.currentTimeMillis();
        int terminalCount = 0;
        for (Map.Entry<String, ProgressSnapshot> entry : snapshots.entrySet()) {
            ProgressSnapshot snapshot = entry.getValue();
            if (!isTerminal(snapshot)) {
                continue;
            }
            terminalCount++;
            if (now - snapshot.getUpdatedAtMs() > terminalRetentionMs) {
                snapshots.remove(entry.getKey());
                terminalCount--;
            }
        }
        // 终态条目超限时按入库时间驱逐最旧者
        while (terminalCount > maxTerminalEntries) {
            String oldestKey = null;
            long oldestAt = Long.MAX_VALUE;
            for (Map.Entry<String, ProgressSnapshot> entry : snapshots.entrySet()) {
                if (isTerminal(entry.getValue()) && entry.getValue().getUpdatedAtMs() < oldestAt) {
                    oldestAt = entry.getValue().getUpdatedAtMs();
                    oldestKey = entry.getKey();
                }
            }
            if (oldestKey == null) {
                break;
            }
            snapshots.remove(oldestKey);
            terminalCount--;
        }
    }

    private static boolean isTerminal(ProgressSnapshot snapshot) {
        ProgressStatus status = snapshot.getStatus();
        return status == ProgressStatus.COMPLETED || status == ProgressStatus.FAILED;
    }
}
