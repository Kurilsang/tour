-- 删除表（如果存在）注意顺序，因为有外键约束
DROP TABLE IF EXISTS `refund_apply`;
DROP TABLE IF EXISTS `wxpayment_product_order`;
DROP TABLE IF EXISTS `wx_refund_record`;
DROP TABLE IF EXISTS `wxpayment_activity_order`;
DROP TABLE IF EXISTS `wish_comment`;
DROP TABLE IF EXISTS `wish_vote`;
DROP TABLE IF EXISTS `wish`;
DROP TABLE IF EXISTS `user_achievement`;
DROP TABLE IF EXISTS `enrollment_traveler`;
DROP TABLE IF EXISTS `enrollment`;
DROP TABLE IF EXISTS `comment`;
DROP TABLE IF EXISTS `activity_order_traveler`;
DROP TABLE IF EXISTS `activity_order`;
DROP TABLE IF EXISTS `product_order`;
DROP TABLE IF EXISTS `activity_detail`;
DROP TABLE IF EXISTS `activity`;
DROP TABLE IF EXISTS `traveler`;
DROP TABLE IF EXISTS `user`;
DROP TABLE IF EXISTS `achievement`;
DROP TABLE IF EXISTS `banner`;
DROP TABLE IF EXISTS `product`;
DROP TABLE IF EXISTS `location`;
DROP TABLE IF EXISTS `bus_location`;
DROP TABLE IF EXISTS `info`;

-- 用户表（核心身份信息）
CREATE TABLE `user` (
                        `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                        `openid` varchar(128) NOT NULL COMMENT '微信openid',
                        `unionid` varchar(128) DEFAULT NULL COMMENT '微信unionid',
                        `nickname` varchar(64) NOT NULL DEFAULT '' COMMENT '用户昵称',
                        `avatar` varchar(256) NOT NULL DEFAULT '' COMMENT '头像URL',
                        `role` varchar(32) NOT NULL DEFAULT 'user' COMMENT '用户角色',
                        `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `udx_openid` (`openid`),
                        KEY `idx_unionid` (`unionid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 位置表
CREATE TABLE `location` (
                            `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '地址信息id',
                            `name` varchar(100) NOT NULL COMMENT '位置名称',
                            `address` varchar(255) NOT NULL COMMENT '详细地址',
                            `latitude` decimal(10,7) NOT NULL COMMENT '纬度',
                            `longitude` decimal(10,7) NOT NULL COMMENT '经度',
                            `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            `location_activity_id` bigint unsigned DEFAULT '0',
                            `location_product_id` bigint unsigned DEFAULT '0',
                            PRIMARY KEY (`id`),
                            KEY `location_activity_id_fk` (`location_activity_id`),
                            KEY `location_product_id_fk` (`location_product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 商品表
CREATE TABLE `product` (
                           `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                           `name` varchar(128) NOT NULL,
                           `description` text NULL,
                           `price` decimal(10, 2) NOT NULL,
                           `stock` int unsigned DEFAULT '0' NOT NULL,
                           `status` int unsigned DEFAULT '1' NOT NULL COMMENT '商品状态 1- 下架 2-上架',
                           `reserve_stock` int unsigned DEFAULT '0' NOT NULL COMMENT '预留库存',
                           `cover_image` varchar(256) NOT NULL,
                           `created_by` varchar(128) NOT NULL,
                           `updated_by` varchar(128) NOT NULL,
                           `create_time` datetime DEFAULT CURRENT_TIMESTAMP NOT NULL,
                           `update_time` datetime DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
                           PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 活动主表
CREATE TABLE `activity` (
                            `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
                            `title` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '活动标题',
                            `cover_image` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '封面图URL',
                            `activity_position` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT '无' COMMENT '集合地点',
                            `early_bird_price` decimal(10,2) NOT NULL COMMENT '早鸟价',
                            `normal_price` decimal(10,2) NOT NULL COMMENT '普通价',
                            `early_bird_quota` int(10) unsigned NOT NULL COMMENT '早鸟价库存',
                            `normal_quota` int(10) unsigned NOT NULL COMMENT '普通价库存',
                            `reserved_early_bird` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '预留早鸟库存',
                            `reserved_normal` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '预留普通库存',
                            `total_sold` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '总销量',
                            `sign_end_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报名截止时间',
                            `start_time` datetime NOT NULL COMMENT '活动开始时间',
                            `end_time` datetime NOT NULL COMMENT '活动结束时间',
                            `end_refund_time` datetime NOT NULL COMMENT '可退款截止时间',
                            `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            `created_by` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '创建人ID',
                            `updated_by` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '最后修改人ID',
                            `description` text COLLATE utf8mb4_unicode_ci COMMENT '简介，用于部分较短的介绍',
                            `status` tinyint(3) unsigned NOT NULL DEFAULT '0' COMMENT '0 - 未发布, 1- 已发布 2- 已关闭 3- 进行中',
                            `bus_locations` json DEFAULT NULL COMMENT '上车点信息，JSON格式的数组',
                            `group_qrcode` varchar(256) NOT NULL COMMENT '活动群二维码URL',
                            `minicode` varchar(256) DEFAULT NULL COMMENT '活动小程序码URL',
                            PRIMARY KEY (`id`),
                            KEY `idx_creator` (`created_by`),
                            KEY `idx_updater` (`updated_by`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 活动详情表
CREATE TABLE `activity_detail` (
                                   `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                                   `activity_id` bigint unsigned NOT NULL,
                                   `content` text NOT NULL COMMENT 'HTML格式内容',
                                   `sort_order` smallint unsigned NOT NULL DEFAULT '0' COMMENT '排序权重',
                                   PRIMARY KEY (`id`),
                                   KEY `idx_activity` (`activity_id`),
                                   CONSTRAINT `fk_detail_activity` FOREIGN KEY (`activity_id`) REFERENCES `activity` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 出行人信息表
CREATE TABLE `traveler` (
                            `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                            `openid` varchar(128) NOT NULL COMMENT '关联用户的openid',
                            `name` varchar(32) NOT NULL COMMENT '姓名',
                            `phone` varchar(20) NOT NULL COMMENT '手机号',
                            `gender` tinyint NOT NULL COMMENT '1-男 2-女',
                            `id_card` varchar(64) NOT NULL COMMENT '加密后的身份证号',
                            `birthday` date NOT NULL COMMENT '根据身份证号解析',
                            `emergency_name` varchar(32) NOT NULL COMMENT '紧急联系人',
                            `emergency_phone` varchar(20) NOT NULL COMMENT '紧急联系电话',
                            `nickname` varchar(32) DEFAULT NULL COMMENT '称呼方式',
                            `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除 0-未删除 1-已删除',
                            `delete_time` datetime DEFAULT NULL COMMENT '删除时间',
                            PRIMARY KEY (`id`),
                            KEY `idx_openid` (`openid`),
                            CONSTRAINT `fk_traveler_user` FOREIGN KEY (`openid`) REFERENCES `user` (`openid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 活动订单表
CREATE TABLE `activity_order` (
                                  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                                  `order_no` varchar(32) NOT NULL COMMENT '订单号（日期+随机数）',
                                  `openid` varchar(128) NOT NULL COMMENT '用户openid',
                                  `activity_id` bigint unsigned NOT NULL,
                                  `early_bird_num` int unsigned DEFAULT '0' NOT NULL,
                                  `normal_num` int unsigned DEFAULT '0' NOT NULL,
                                  `total_amount` decimal(10, 2) NOT NULL,
                                  `status` tinyint NOT NULL COMMENT '1-待支付 2-已支付 3-已取消 4-已过期 5-已完成',
                                  `payment_time` datetime NULL,
                                  `bus_location_id` bigint unsigned DEFAULT '0' NOT NULL COMMENT '上车点id',
                                  `expire_time` datetime NOT NULL COMMENT '订单过期时间（创建时间+15分钟）',
                                  `create_time` datetime DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                  `refund_status` tinyint DEFAULT 0 NOT NULL COMMENT '退款状态 0-未申请退款 1-已申请退款 2- 不可退款',
                                  PRIMARY KEY (`id`),
                                  UNIQUE KEY `udx_orderno` (`order_no`),
                                  KEY `idx_activity` (`activity_id`),
                                  KEY `idx_openid` (`openid`),
                                  CONSTRAINT `fk_order_user` FOREIGN KEY (`openid`) REFERENCES `user` (`openid`),
                                  CONSTRAINT `fk_order_activity` FOREIGN KEY (`activity_id`) REFERENCES `activity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 活动订单与出行人关联表
CREATE TABLE `activity_order_traveler` (
                                           `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                                           `order_no` varchar(32) NOT NULL COMMENT '订单号',
                                           `traveler_id` bigint unsigned NOT NULL COMMENT '出行人ID',
                                           `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关联创建时间',
                                           PRIMARY KEY (`id`),
                                           UNIQUE KEY `uniq_order_traveler` (`order_no`,`traveler_id`),
                                           KEY `idx_order_no` (`order_no`),
                                           KEY `idx_traveler_id` (`traveler_id`),
                                           CONSTRAINT `fk_order_traveler_order` FOREIGN KEY (`order_no`) REFERENCES `activity_order` (`order_no`) ON DELETE CASCADE,
                                           CONSTRAINT `fk_order_traveler_traveler` FOREIGN KEY (`traveler_id`) REFERENCES `traveler` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 商品订单表
CREATE TABLE `product_order` (
                                 `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                                 `openid` varchar(128) NOT NULL COMMENT '用户openid',
                                 `product_id` bigint unsigned NOT NULL,
                                 `order_no` varchar(32) NOT NULL,
                                 `quantity` int unsigned NOT NULL,
                                 `total_amount` decimal(10, 2) NOT NULL,
                                 `pickup_location` varchar(256) NOT NULL COMMENT '自提点地址',
                                 `status` tinyint NOT NULL COMMENT '1-待提货 2-已完成 3-已取消 4-待支付',
                                 `create_time` datetime DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                 `expire_time` datetime DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '订单过期时间',
                                 `refund_status` tinyint DEFAULT 0 NOT NULL COMMENT '是否退款状态 0-没退款 1-已退款',
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `product_order_order_no_index` (`order_no`),
                                 KEY `idx_openid` (`openid`),
                                 KEY `idx_product` (`product_id`),
                                 CONSTRAINT `fk_porder_user` FOREIGN KEY (`openid`) REFERENCES `user` (`openid`),
                                 CONSTRAINT `fk_porder_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 报名表
CREATE TABLE `enrollment` (
                              `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                              `user_id` varchar(128) NOT NULL COMMENT '用户ID',
                              `activity_id` bigint unsigned NOT NULL COMMENT '活动ID',
                              `price` decimal(10, 2) NOT NULL COMMENT '支付价格',
                              `create_time` datetime DEFAULT CURRENT_TIMESTAMP NOT NULL,
                              `update_time` datetime DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
                              `order_no` varchar(32) NULL COMMENT '订单编号',
                              PRIMARY KEY (`id`),
                              KEY `idx_activity_id` (`activity_id`),
                              KEY `idx_user_id` (`user_id`),
                              CONSTRAINT `fk_enrollment_activity` FOREIGN KEY (`activity_id`) REFERENCES `activity` (`id`) ON DELETE CASCADE,
                              CONSTRAINT `fk_enrollment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`openid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 报名与出行人关联表
CREATE TABLE `enrollment_traveler` (
                                       `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                                       `enrollment_id` bigint unsigned NOT NULL COMMENT '报名表ID',
                                       `traveler_id` bigint unsigned NOT NULL COMMENT '出行人ID',
                                       `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                       PRIMARY KEY (`id`),
                                       UNIQUE KEY `udx_enrollment_traveler` (`enrollment_id`,`traveler_id`),
                                       CONSTRAINT `enrollment_traveler_traveler_id_fk` FOREIGN KEY (`traveler_id`) REFERENCES `traveler` (`id`),
                                       CONSTRAINT `fk_et_enrollment` FOREIGN KEY (`enrollment_id`) REFERENCES `enrollment` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 评论表
CREATE TABLE `comment` (
                           `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                           `openid` varchar(128) NOT NULL COMMENT '用户openid',
                           `activity_id` bigint unsigned NOT NULL,
                           `order_no` varchar(32) NOT NULL COMMENT '关联已完成的订单，订单号',
                           `status` int unsigned DEFAULT '1' NOT NULL COMMENT '评论状态 1-待审核 2-可见 3-不可见',
                           `content` text NOT NULL,
                           `create_time` datetime DEFAULT CURRENT_TIMESTAMP NOT NULL,
                           PRIMARY KEY (`id`),
                           KEY `idx_activity` (`activity_id`),
                           CONSTRAINT `comment_activity_order_order_no_fk` FOREIGN KEY (`order_no`) REFERENCES `activity_order` (`order_no`),
                           CONSTRAINT `fk_comment_activity` FOREIGN KEY (`activity_id`) REFERENCES `activity` (`id`),
                           CONSTRAINT `fk_comment_user` FOREIGN KEY (`openid`) REFERENCES `user` (`openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 成就表
CREATE TABLE `achievement` (
                               `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                               `title` varchar(255) NOT NULL,
                               `icon_url` varchar(256) NOT NULL,
                               `description` varchar(256) DEFAULT NULL,
                               `sign_in_code` char(36) NOT NULL,
                               `sign_in_url` varchar(256) DEFAULT NULL COMMENT '签到二维码链接',
                               `activity_id` bigint DEFAULT NULL,
                               `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
                               PRIMARY KEY (`id`),
                               UNIQUE KEY `sign_in_code` (`sign_in_code`),
                               KEY `idx_sign_code` (`sign_in_code`),
                               KEY `idx_activity_id` (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 用户成就表
CREATE TABLE `user_achievement` (
                                    `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                                    `openid` varchar(128) NOT NULL COMMENT '用户openid',
                                    `achievement_id` bigint unsigned NOT NULL,
                                    `obtain_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    PRIMARY KEY (`id`),
                                    UNIQUE KEY `udx_user_achv` (`openid`,`achievement_id`),
                                    CONSTRAINT `fk_ua_achv` FOREIGN KEY (`achievement_id`) REFERENCES `achievement` (`id`),
                                    CONSTRAINT `fk_ua_user` FOREIGN KEY (`openid`) REFERENCES `user` (`openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 轮播图表
CREATE TABLE `banner` (
                          `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                          `image_url` varchar(256) NOT NULL,
                          `title` varchar(64) NOT NULL,
                          `link_type` tinyint NOT NULL COMMENT '1-活动详情 2-外部链接',
                          `link_value` varchar(256) NOT NULL COMMENT '活动ID或URL',
                          `sort_order` smallint NOT NULL DEFAULT '0',
                          `created_by` varchar(128) NOT NULL,
                          `updated_by` varchar(128) NOT NULL,
                          PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 心愿表
CREATE TABLE `wish` (
                        `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
                        `user_openid` varchar(128) NOT NULL COMMENT '发起人openid（关联用户表openid）',
                        `title` varchar(255) NOT NULL COMMENT '心愿标题',
                        `description` text COMMENT '详细描述',
                        `location_id` bigint unsigned NOT NULL COMMENT '目的地',
                        `image_urls` json DEFAULT NULL COMMENT '图片链接（JSON数组格式，如 ["url1", "url2"]）',
                        `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0-待成团，1-已成团，2-已关闭',
                        `vote_count` bigint NOT NULL DEFAULT '0' COMMENT '投票数',
                        `comment_count` bigint NOT NULL DEFAULT '0' COMMENT '评论数',
                        `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        PRIMARY KEY (`id`),
                        KEY `idx_user_openid` (`user_openid`),
                        KEY `idx_status` (`status`),
                        KEY `idx_location_id` (`location_id`),
                        CONSTRAINT `wish_ibfk_1` FOREIGN KEY (`user_openid`) REFERENCES `user` (`openid`) ON DELETE CASCADE,
                        CONSTRAINT `wish_ibfk_2` FOREIGN KEY (`location_id`) REFERENCES `location` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 心愿投票表
CREATE TABLE `wish_vote` (
                             `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
                             `wish_vote_id` bigint unsigned NOT NULL COMMENT '关联心愿路线ID',
                             `user_openid` varchar(128) NOT NULL COMMENT '投票用户openid',
                             `vote_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '投票时间',
                             PRIMARY KEY (`id`),
                             CONSTRAINT `wish_vote_ibfk_1` FOREIGN KEY (`wish_vote_id`) REFERENCES `wish` (`id`) ON DELETE CASCADE,
                             CONSTRAINT `wish_vote_ibfk_2` FOREIGN KEY (`user_openid`) REFERENCES `user` (`openid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 心愿评论表
CREATE TABLE `wish_comment` (
                                `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
                                `wish_id` bigint unsigned NOT NULL COMMENT '关联心愿路线ID',
                                `user_openid` varchar(128) NOT NULL COMMENT '评论用户openid',
                                `content` text NOT NULL COMMENT '评论内容',
                                `image_urls` json DEFAULT NULL COMMENT '评论图片链接（JSON数组格式）',
                                `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                PRIMARY KEY (`id`),
                                KEY `idx_wish_id` (`wish_id`),
                                KEY `idx_user_openid` (`user_openid`),
                                CONSTRAINT `wish_comment_ibfk_1` FOREIGN KEY (`wish_id`) REFERENCES `wish` (`id`) ON DELETE CASCADE,
                                CONSTRAINT `wish_comment_ibfk_2` FOREIGN KEY (`user_openid`) REFERENCES `user` (`openid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 大巴上车地点表
CREATE TABLE `bus_location` (
                                `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                                `bus_location` varchar(256) NOT NULL COMMENT '字符串类型的大巴上车地',
                                `location_id` bigint unsigned DEFAULT '0' NOT NULL COMMENT '关联地址表id',
                                `create_time` datetime DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 微信支付商品订单关联表
CREATE TABLE `wxpayment_product_order` (
                                           `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                                           `order_no` varchar(32) NOT NULL COMMENT '商户订单号',
                                           `time_stamp` varchar(32) NOT NULL COMMENT '时间戳',
                                           `nonce_str` text NOT NULL COMMENT '随机字符串',
                                           `package_str` text NOT NULL COMMENT '订单详情扩展字符串',
                                           `sign_type` varchar(16) NOT NULL COMMENT '签名方式',
                                           `pay_sign` text NOT NULL COMMENT '签名',
                                           `prepay_id` text NULL COMMENT '预支付ID',
                                           `create_time` datetime DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
                                           PRIMARY KEY (`id`),
                                           UNIQUE KEY `udx_order_no` (`order_no`),
                                           CONSTRAINT `fk_wx_payment_order` FOREIGN KEY (`order_no`) REFERENCES `product_order` (`order_no`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='微信支付商品订单关联信息表';

-- 微信退款记录表
CREATE TABLE `wx_refund_record` (
                                    `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                                    `out_trade_no` varchar(32) NOT NULL COMMENT '商户订单号',
                                    `out_refund_no` varchar(64) NOT NULL COMMENT '商户退款单号',
                                    `refund_id` varchar(64) DEFAULT NULL COMMENT '微信退款单号',
                                    `total_amount` decimal(10, 2) NOT NULL COMMENT '订单总金额',
                                    `refund_amount` decimal(10, 2) NOT NULL COMMENT '退款金额',
                                    `refund_status` varchar(32) NOT NULL COMMENT '退款状态',
                                    `reason` varchar(256) DEFAULT NULL COMMENT '退款原因',
                                    `success_time` datetime DEFAULT NULL COMMENT '退款成功时间',
                                    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                    PRIMARY KEY (`id`),
                                    UNIQUE KEY `udx_out_refund_no` (`out_refund_no`),
                                    KEY `idx_out_trade_no` (`out_trade_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='微信退款记录表';

-- 微信支付活动订单关联表
CREATE TABLE `wxpayment_activity_order` (
                                            `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                                            `order_no` varchar(32) NOT NULL COMMENT '商户订单号',
                                            `time_stamp` varchar(32) NOT NULL COMMENT '时间戳',
                                            `nonce_str` text NOT NULL COMMENT '随机字符串',
                                            `package_str` text NOT NULL COMMENT '订单详情扩展字符串',
                                            `sign_type` varchar(16) NOT NULL COMMENT '签名方式',
                                            `pay_sign` text NOT NULL COMMENT '签名',
                                            `prepay_id` text NULL COMMENT '预支付ID',
                                            `create_time` datetime DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
                                            PRIMARY KEY (`id`),
                                            UNIQUE KEY `udx_order_no` (`order_no`),
                                            CONSTRAINT `fk_wx_payment_activity_order` FOREIGN KEY (`order_no`) REFERENCES `activity_order` (`order_no`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='微信支付活动订单关联信息表';

-- 信息存储表（模拟Redis功能）
CREATE TABLE `info` (
                        `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                        `info_key` varchar(128) NOT NULL COMMENT '键名',
                        `info_value` json DEFAULT NULL COMMENT 'JSON格式的值',
                        `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `udx_info_key` (`info_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='信息存储表';

-- 退款申请表
CREATE TABLE `refund_apply` (
                               `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                               `out_trade_order` varchar(32) NOT NULL COMMENT '订单号(对应商品订单或活动订单)',
                               `openid` varchar(128) NOT NULL COMMENT '申请退款人openid',
                               `status` tinyint NOT NULL DEFAULT '1' COMMENT '退款状态: 1-待审核, 2-退款中, 3-退款完成, 4-拒绝退款',
                               `reason` varchar(500) NOT NULL COMMENT '退款理由',
                               `order_type` tinyint NOT NULL COMMENT '订单类型: 1-活动订单, 2-商品订单',
                               `admin_remark` varchar(500) DEFAULT NULL COMMENT '管理员备注',
                               `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               PRIMARY KEY (`id`),
                               KEY `idx_out_trade_order` (`out_trade_order`),
                               KEY `idx_openid` (`openid`),
                               CONSTRAINT `fk_refund_openid` FOREIGN KEY (`openid`) REFERENCES `user` (`openid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='退款申请表';