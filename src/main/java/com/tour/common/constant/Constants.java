package com.tour.common.constant;

/**
 * @Author Abin
 * @Description 系统常量类
 */
public class Constants {
//    默认初始页码
    public static final Integer defaultPageNo = 1;
//    默认早鸟价预留库存
    public static final Integer defaultReserveEarlyBird = 0;
//    默认普通价预留库存
    public static final Integer defaultReserveNormal= 0;

//    默认未支付订单过期时间（单位分钟）
    public static final Integer defaultOrderExpireTime= 15;
//    默认报名截止时间往前多久（单位小时）
    public static long defaultSignEndTime = 72;

//    结束活动是否重置销量
    public static boolean defaultEndActivitySold = true;

//    商品订单号前缀
    public static final String productOrderPrefix = "PO";

//    商品预留库存过期时间（单位分钟）
    public static final Integer productOrderExpireTime = 15;

//    处理超时订单批次大小
    public static final Integer timeoutOrderBatchSize = 100;

    // 用户相关
    public static final String DEFAULT_IMAGE_URL = "https://7072-prod-3goiaaiadf5a04cf-1358688773.tcb.qcloud.la/static/logo/logo.jpg";
    public static final String DEFAULT_NICKNAME = "微信用户";
    // 微信API相关
    public static final String WX_EVENT_MEDIA_CHECK = "wxa_media_check";  // 媒体检测事件类型
    public static final String WX_MESSAGE_TYPE_EVENT = "event";  // 事件消息类型
    public static final Integer WX_MEDIA_CHECK_API_VERSION = 2;  // 媒体检测API版本号
    public static final Integer WX_MSG_CHECK_API_VERSION = 2;    // 文本检测API版本号

    // 媒体类型
    public static final Integer MEDIA_TYPE_AUDIO = 1;  // 音频类型
    public static final Integer MEDIA_TYPE_IMAGE = 2;  // 图片类型
    public static final Integer MEDIA_TYPE_VIDEO = 3;  // 视频类型（非标准，自定义）

    // 媒体检测结果类型
    public static final Integer MEDIA_CHECK_RESULT_PASS = 0;   // 合规
    public static final Integer MEDIA_CHECK_RESULT_RISKY = 1;  // 不合规
    public static final Integer MEDIA_CHECK_RESULT_REVIEW = 2; // 疑似

    // 文本检测场景类型
    public static final Integer TEXT_CHECK_SCENE_PROFILE = 1;  // 资料场景
    public static final Integer TEXT_CHECK_SCENE_COMMENT = 2;  // 评论场景
    public static final Integer TEXT_CHECK_SCENE_FORUM = 3;    // 论坛场景
    public static final Integer TEXT_CHECK_SCENE_SOCIAL = 4;   // 社交日志场景

    // 文本检测结果类型
    public static final String TEXT_CHECK_RESULT_PASS = "pass";    // 通过
    public static final String TEXT_CHECK_RESULT_REVIEW = "review";  // 待审核
    public static final String TEXT_CHECK_RESULT_RISKY = "risky";  // 不通过

    // 文本检测默认值
    public static final Integer TEXT_CHECK_DEFAULT_SCENE = 1;  // 默认使用资料场景
    public static final boolean TEXT_CHECK_DEFAULT_RESULT = true;  // 默认结果为通过

    // 微信支付相关常量
    /** 微信支付查询订单API响应成功状态码 */
    public static final Integer WX_PAY_QUERY_SUCCESS = 200;
    
    /** 微信订单查询失败错误码 - 订单不存在 */
    public static final String WX_PAY_ORDER_NOT_EXISTS = "ORDER_NOT_EXISTS";
    
    /** 微信订单查询默认结果 - 失败 */
    public static final boolean WX_PAY_QUERY_DEFAULT_RESULT = false;

//    用户默认退款理由
    public static String deafultUserRefundReason = "用户取消订单";

    public static String MINICODE_VERSION = "release"; // 小程序码版本

    // 票类型相关常量
    public static final Integer DEFAULT_TICK_TYPE = 2; // 默认票类型为普通票
}
