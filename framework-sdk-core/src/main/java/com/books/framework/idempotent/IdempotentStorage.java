package com.books.framework.idempotent;

/**
 * 幂等记录存储抽象——业务方可注入自定义实现（如 Redis）。
 */
public interface IdempotentStorage {

    /**
     * 尝试获取幂等锁（首次请求返回 true，重复返回 false）。
     *
     * @param key        幂等 key
     * @param ttlSeconds 有效时间
     * @return true = 首次请求，false = 重复请求
     */
    boolean tryAcquire(String key, long ttlSeconds);

    /**
     * 释放幂等记录（一般不需要，失败时可清理）。
     */
    void release(String key);
}
