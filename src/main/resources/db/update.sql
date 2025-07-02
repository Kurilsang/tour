-- 活动订单与出行人关联表添加票类型字段
ALTER TABLE `activity_order_traveler` 
ADD COLUMN `tick_type` tinyint NOT NULL DEFAULT 2 COMMENT '票类型 1-早鸟票 2-普通票';

-- 创建退款申请与出行人关联表
create table refund_apply_traveler
(
    id                         bigint unsigned auto_increment comment '主键ID'
        primary key,
    refund_apply_id            bigint unsigned                    not null comment '退款申请ID',
    traveler_id                bigint unsigned                    not null comment '出行人ID',
    activity_order_traveler_id bigint unsigned                    not null comment '活动订单出行人关联ID',
    order_no                   varchar(32)                        not null comment '订单号（冗余字段，便于查询）',
    tick_type                  tinyint                            not null comment '票类型 1-早鸟票 2-普通票',
    refund_amount              decimal(10, 2)                     not null comment '该出行人对应的退款金额',
    create_time                datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    constraint fk_rat_aot
        foreign key (activity_order_traveler_id) references activity_order_traveler (id)
            on delete cascade,
    constraint fk_rat_refund_apply
        foreign key (refund_apply_id) references refund_apply (id)
            on delete cascade,
    constraint fk_rat_traveler
        foreign key (traveler_id) references traveler (id)
)
    comment '退款申请与出行人关联表' collate = utf8mb4_unicode_ci;

create index idx_order_no
    on refund_apply_traveler (order_no);

create index idx_refund_apply_id
    on refund_apply_traveler (refund_apply_id);

create index idx_traveler_id
    on refund_apply_traveler (traveler_id);

-- 为refund_apply表添加退款金额字段
ALTER TABLE `refund_apply` ADD COLUMN `refund_amount` decimal(10,2) NULL COMMENT '退款金额';

-- 修改refund_apply表的status字段注释
ALTER TABLE `refund_apply` 
MODIFY COLUMN `status` tinyint NOT NULL DEFAULT '1' COMMENT '退款状态: 1-待审核, 2-退款中, 3-退款完成, 4-拒绝退款, 5-已取消';

-- 修改activity_order表的status字段注释，添加已免单状态
ALTER TABLE `activity_order` 
MODIFY COLUMN `status` tinyint NOT NULL COMMENT '1-待支付 2-已支付 3-已取消 4-已过期 5-已完成 6-已免单';

-- 活动订单与出行人关联表添加是否可退款字段
ALTER TABLE `activity_order_traveler` 
ADD COLUMN `refund_status` tinyint NOT NULL DEFAULT 0 COMMENT '是否可免单 0-可免单 1-不可退';

