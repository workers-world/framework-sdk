package com.books.framework.id_generator;

/**
 * 按 prefix + 业务日期分配递增序号的存储抽象。
 */
public interface IdSequenceStore {

    /**
     * 获取并递增序号（从 1 开始）。
     *
     * @param prefix  业务前缀，如 EV、P
     * @param bizDate 业务日期 yyyyMMdd
     * @return 本次分配的序号
     */
    long nextSeq(String prefix, String bizDate);
}
