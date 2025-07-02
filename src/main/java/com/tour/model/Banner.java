package com.tour.model;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author Abin
 * @Description 轮播图实体类
 * @DateTime 2025/5/9 13:41
 */
@Data
public class Banner implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 轮播图ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Long id;

    /**
     * 图片URL
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String imageUrl;

    /**
     * 标题
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String title;

    /**
     * 链接类型：1-活动详情 2-外部链接
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Integer linkType;

    /**
     * 链接值：活动ID或URL
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String linkValue;

    /**
     * 排序号
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Integer sortOrder;

    /**
     * 创建人
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String createdBy;

    /**
     * 更新人
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String updatedBy;
} 