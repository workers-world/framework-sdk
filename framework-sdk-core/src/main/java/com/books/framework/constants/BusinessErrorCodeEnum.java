package com.books.framework.constants;

import lombok.Getter;

/**
 * 业务错误码枚举示例（仅 demo，业务服务应自建 ErrorCode 枚举）
 */
@Deprecated
@Getter
public enum BusinessErrorCodeEnum implements ErrorCode {
    // 用户相关错误码
    USER_NOT_FOUND("USER_001", "用户不存在"),
    USER_ALREADY_EXISTS("USER_002", "用户已存在"),
    USER_PASSWORD_ERROR("USER_003", "密码错误"),
    USER_ACCOUNT_LOCKED("USER_004", "账号已被锁定"),
    USER_ACCOUNT_EXPIRED("USER_005", "账号已过期"),
    USER_CREDENTIALS_EXPIRED("USER_006", "凭证已过期"),
    USER_ACCOUNT_DISABLED("USER_007", "账号已被禁用"),
    USER_EMAIL_EXISTS("USER_008", "邮箱已存在"),
    USER_PHONE_EXISTS("USER_009", "手机号已存在"),
    USER_VERIFICATION_FAILED("USER_010", "用户验证失败"),

    // 图书相关错误码
    BOOK_NOT_FOUND("BOOK_001", "图书不存在"),
    BOOK_ALREADY_EXISTS("BOOK_002", "图书已存在"),
    BOOK_ISBN_INVALID("BOOK_003", "ISBN格式不正确"),
    BOOK_NAME_EMPTY("BOOK_004", "图书名称不能为空"),
    BOOK_AUTHOR_EMPTY("BOOK_005", "作者不能为空"),
    BOOK_PUBLISHER_EMPTY("BOOK_006", "出版社不能为空"),
    BOOK_PUBLISH_DATE_INVALID("BOOK_007", "出版日期格式不正确"),
    BOOK_PRICE_INVALID("BOOK_008", "价格格式不正确"),
    BOOK_STATUS_INVALID("BOOK_009", "图书状态不正确"),
    BOOK_BORROWED("BOOK_010", "图书已被借出"),
    BOOK_RESERVED("BOOK_011", "图书已被预约"),
    BOOK_DELETED("BOOK_012", "图书已删除"),
    BOOK_BUILD_ERROR("BOOK_013", "组装BOOK出错"),
    BOOK_INSERT_ERROR("BOOK_014", "插入条数不为1"),

    // 订单相关错误码
    ORDER_NOT_FOUND("ORDER_001", "订单不存在"),
    ORDER_ALREADY_EXISTS("ORDER_002", "订单已存在"),
    ORDER_STATUS_INVALID("ORDER_003", "订单状态不正确"),
    ORDER_CANCELLED("ORDER_004", "订单已取消"),
    ORDER_COMPLETED("ORDER_005", "订单已完成"),
    ORDER_PAYMENT_FAILED("ORDER_006", "订单支付失败"),
    ORDER_REFUND_FAILED("ORDER_007", "订单退款失败"),

    // 支付相关错误码
    PAYMENT_FAILED("PAYMENT_001", "支付失败"),
    PAYMENT_TIMEOUT("PAYMENT_002", "支付超时"),
    PAYMENT_AMOUNT_INVALID("PAYMENT_003", "支付金额无效"),
    PAYMENT_METHOD_NOT_SUPPORTED("PAYMENT_004", "支付方式不支持"),
    PAYMENT_REFUND_FAILED("PAYMENT_005", "退款失败"),

    // 库存相关错误码
    STOCK_INSUFFICIENT("STOCK_001", "库存不足"),
    STOCK_LOCKED("STOCK_002", "库存被锁定"),
    STOCK_UPDATE_FAILED("STOCK_003", "库存更新失败"),
    STOCK_RESERVATION_FAILED("STOCK_004", "库存预约失败"),

    // 活动相关错误码
    ACTIVITY_NOT_FOUND("ACTIVITY_001", "活动不存在"),
    ACTIVITY_EXPIRED("ACTIVITY_002", "活动已过期"),
    ACTIVITY_NOT_STARTED("ACTIVITY_003", "活动未开始"),
    ACTIVITY_QUOTA_EXCEEDED("ACTIVITY_004", "活动名额已满"),
    ACTIVITY_ALREADY_JOINED("ACTIVITY_005", "已参加该活动"),

    // 评论相关错误码
    COMMENT_NOT_FOUND("COMMENT_001", "评论不存在"),
    COMMENT_DELETED("COMMENT_002", "评论已删除"),
    COMMENT_CONTENT_EMPTY("COMMENT_003", "评论内容不能为空"),
    COMMENT_TOO_LONG("COMMENT_004", "评论内容过长"),
    COMMENT_SPAM("COMMENT_005", "评论被识别为垃圾信息");

    private final String code;
    private final String message;

    BusinessErrorCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 根据错误码获取错误码枚举
     *
     * @param code 错误码
     * @return 错误码枚举
     */
    public static BusinessErrorCodeEnum getByCode(String code) {
        for (BusinessErrorCodeEnum errorCode : values()) {
            if (errorCode.getCode().equals(code)) {
                return errorCode;
            }
        }
        return null;
    }

    /**
     * 检查错误码是否存在
     *
     * @param code 错误码
     * @return 是否存在
     */
    public static boolean exists(String code) {
        return getByCode(code) != null;
    }
}
