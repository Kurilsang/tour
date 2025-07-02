package com.tour.enums;

import lombok.Getter;

/**
 * @Author Abin
 * @Description 媒体检测相关枚举
 */
public class MediaCheckEnum {

    /**
     * 业务类型枚举
     */
    @Getter
    public enum BusinessType {
        USER_AVATAR("user_avatar", "用户头像"),
        WISH_IMAGE("wish_image", "心愿图片"),
        COMMENT_IMAGE("comment_image", "评论图片");

        private final String code;
        private final String desc;

        BusinessType(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }

    /**
     * 检测结果枚举
     */
    @Getter
    public enum Result {
        PASS(0, "pass", "合规"),
        RISKY(1, "risky", "不合规"),
        REVIEW(2, "review", "疑似");

        private final int code;
        private final String suggest;
        private final String desc;

        Result(int code, String suggest, String desc) {
            this.code = code;
            this.suggest = suggest;
            this.desc = desc;
        }

        /**
         * 根据suggest值获取对应的结果码
         * 
         * @param suggest 建议值
         * @return 结果码
         */
        public static int getCodeBySuggest(String suggest) {
            for (Result result : Result.values()) {
                if (result.getSuggest().equals(suggest)) {
                    return result.getCode();
                }
            }
            return PASS.getCode(); // 默认通过
        }
    }

    /**
     * 检测状态枚举
     */
    @Getter
    public enum Status {
        CHECKING(0, "检测中"),
        FINISHED(1, "检测完成");

        private final int code;
        private final String desc;

        Status(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }

    /**
     * 消息处理状态枚举
     */
    @Getter
    public enum MessageStatus {
        PENDING(0, "待处理"),
        PROCESSED(1, "已处理"),
        FAILED(2, "处理失败");

        private final int code;
        private final String desc;

        MessageStatus(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }

    /**
     * 场景值枚举
     */
    @Getter
    public enum Scene {
        PROFILE(1, "资料"),
        COMMENT(2, "评论"),
        FORUM(3, "论坛"),
        SOCIAL(4, "社交日志");

        private final int code;
        private final String desc;

        Scene(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }
} 