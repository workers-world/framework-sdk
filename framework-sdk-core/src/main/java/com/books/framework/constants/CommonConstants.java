package com.books.framework.constants;

/** 框架通用常量。 */
public class CommonConstants {

    /**
     * 成功状态码
     */
    public static final String SUCCESS_CODE = "SUCCESS";

    /**
     * 成功消息
     */
    public static final String SUCCESS_MESSAGE = "操作成功";

    /**
     * 失败状态码
     */
    public static final String FAIL_CODE = "FAIL";

    /**
     * 默认版本号
     */
    public static final String DEFAULT_VERSION = "1.0";

    /**
     * 默认时间格式
     */
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 紧凑时间格式
     */
    public static final String COMPACT_DATE_FORMAT = "yyyyMMddHHmmss";

    /**
     * 紧凑日期格式
     */
    public static final String COMPACT_DATE_ONLY_FORMAT = "yyyyMMdd";

    /**
     * 紧凑时间格式（包含毫秒）
     */
    public static final String COMPACT_DATE_WITH_MILLIS_FORMAT = "yyyyMMddHHmmssSSS";

    /**
     * 默认分页大小
     */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 最大分页大小
     */
    public static final int MAX_PAGE_SIZE = 1000;

    /**
     * 默认编码
     */
    public static final String DEFAULT_ENCODING = "UTF-8";

    /**
     * 私有构造函数，防止实例化
     */
    private CommonConstants() {
        throw new UnsupportedOperationException("常量类不能实例化");
    }
}
