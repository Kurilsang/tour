DROP TABLE IF EXISTS `wx_message`;
DROP TABLE IF EXISTS `media_check_record`;

-- 创建微信消息表
CREATE TABLE IF NOT EXISTS `wx_message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `message_type` varchar(50) DEFAULT NULL COMMENT '消息类型',
  `event_type` varchar(50) DEFAULT NULL COMMENT '事件类型',
  `user_openid` varchar(64) DEFAULT NULL COMMENT '关联的用户openid',
  `appid` varchar(64) DEFAULT NULL COMMENT '关联的小程序appid',
  `status` tinyint(4) DEFAULT '0' COMMENT '处理状态：0-待处理，1-已处理，2-处理失败',
  `result` tinyint(4) DEFAULT NULL COMMENT '处理结果',
  `raw_content` json DEFAULT NULL COMMENT '原始消息内容',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注信息',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_openid` (`user_openid`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='微信消息记录表';

-- 创建媒体检测记录表
-- 注意：不再为业务类型和业务ID创建联合唯一约束，允许同一业务ID多次检测
CREATE TABLE IF NOT EXISTS `media_check_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `trace_id` varchar(64) DEFAULT NULL COMMENT '微信平台追踪ID',
  `media_type` tinyint(4) DEFAULT NULL COMMENT '媒体类型：1-音频，2-图片，3-视频',
  `media_url` varchar(255) DEFAULT NULL COMMENT '媒体URL',
  `business_type` varchar(50) DEFAULT NULL COMMENT '业务类型：user_avatar-用户头像，wish_image-心愿图片，comment_image-评论图片',
  `business_id` varchar(64) DEFAULT NULL COMMENT '业务ID',
  `user_openid` varchar(64) DEFAULT NULL COMMENT '用户openid',
  `status` tinyint(4) DEFAULT '0' COMMENT '检测状态：0-检测中，1-检测完成',
  `result` tinyint(4) DEFAULT NULL COMMENT '检测结果：0-合规，1-不合规，2-疑似',
  `detail` varchar(512) DEFAULT NULL COMMENT '检测详情',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_trace_id` (`trace_id`),
  -- 不再使用联合唯一约束，而是使用普通索引
  KEY `idx_business` (`business_type`,`business_id`),
  KEY `idx_user_openid` (`user_openid`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='媒体检测记录表';

-- 添加测试数据，用于验证回调处理
INSERT INTO `media_check_record` (`id`, `trace_id`, `media_type`, `media_url`, `business_type`, `business_id`, `user_openid`, `status`, `result`, `detail`, `create_time`, `update_time`) 
VALUES (1, '68270352-37769d14-77f86ef4', 2, 'http://localhost:8080/upload/avatar/2025/05/16/a554974162e74daa9137cafc9f845999.jpeg', 'user_avatar', 'ow6Cw7Yo8GqLDEACsmrubVO8iW_s', 'ow6Cw7Yo8GqLDEACsmrubVO8iW_s', 0, NULL, NULL, '2025-05-16 17:20:17', '2025-05-16 17:20:19'); 